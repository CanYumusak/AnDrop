package de.canyumusak.androiddrop

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.canyumusak.androiddrop.databinding.ListItemClientBinding
import de.canyumusak.androiddrop.databinding.RootFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransferActivity : AppCompatActivity() {

    private lateinit var viewModel: DiscoveryViewModel

    private val dataUris: Array<Uri>?
        get() {
            return if (intent.action == Intent.ACTION_SEND) {
                (intent.extras?.get(TransferService.DATA) as Uri?)?.let {
                    arrayOf(it)
                }
            } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
                (intent.extras?.get(TransferService.DATA) as List<Uri?>?)?.let {
                    it.filterNotNull().toTypedArray()
                }
            } else {
                null
            }
        }

    private val needsStoragePermission: Boolean
        get() = viewModel.needsStoragePermission(dataUris)

    var binding: RootFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = RootFragmentBinding.inflate(layoutInflater, null, false)

        viewModel = ViewModelProviders.of(this).get(DiscoveryViewModel::class.java)

        with(inflate.clientList) {
            val clientListAdapter = NsdServiceInfoListAdapter()
            adapter = clientListAdapter
            layoutManager = LinearLayoutManager(context)

            if (dataUris != null) {
                viewModel.clients.observe(this@TransferActivity, Observer {
                    updateEmptyLayout(inflate)
                    clientListAdapter.submitList(it)
                })
            }

            viewModel.wifiState.observe(this@TransferActivity, Observer {
                updateEmptyLayout(inflate)
            })

            clientListAdapter.submitList(viewModel.clients.value)
        }

        inflate.buttonStoragePermission.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        updatePermissionRequired(inflate)

        binding = inflate

        setContentView(inflate.root)
    }

    private fun updateEmptyLayout(inflate: RootFragmentBinding) {
        if (dataUris == null) {
            val emptyLayout = inflate.emptyLayout
            emptyLayout.textView.text = getString(R.string.unsupoorted_file_type)
            emptyLayout.emptyLayoutRoot.visibility = View.VISIBLE
            emptyLayout.progressBar.visibility = View.GONE
            emptyLayout.noWifiIcon.visibility = View.GONE
        } else {
            updateEmptyLayoutForWifiState(inflate)
        }
    }

    private fun updateEmptyLayoutForWifiState(inflate: RootFragmentBinding) {
        val emptyLayout = inflate.emptyLayout

        when (viewModel.wifiState.value) {
            WifiState.Disabled -> {
                emptyLayout.textView.text = getString(R.string.not_connected_to_wifi)
                emptyLayout.emptyLayoutRoot.visibility = View.VISIBLE
                emptyLayout.progressBar.visibility = View.GONE
                emptyLayout.noWifiIcon.visibility = View.VISIBLE
            }
            is WifiState.Enabled -> {
                if (viewModel.clients.value?.isNotEmpty() == true) {
                    emptyLayout.emptyLayoutRoot.visibility = View.INVISIBLE
                } else {
                    emptyLayout.emptyLayoutRoot.visibility = View.VISIBLE
                    emptyLayout.textView.text = getString(R.string.searching_clients)
                    emptyLayout.progressBar.visibility = View.VISIBLE
                    emptyLayout.noWifiIcon.visibility = View.GONE
                }
            }
            null -> {
                if (viewModel.clients.value?.isNotEmpty() == true) {
                    emptyLayout.emptyLayoutRoot.visibility = View.INVISIBLE
                } else {
                    emptyLayout.emptyLayoutRoot.visibility = View.VISIBLE
                    emptyLayout.textView.text = getString(R.string.searching_clients)
                    emptyLayout.progressBar.visibility = View.VISIBLE
                    emptyLayout.noWifiIcon.visibility = View.GONE
                }
            }
        }
    }

    fun updatePermissionRequired(binding: RootFragmentBinding) {
        binding.descriptionStoragePermission.isVisible = needsStoragePermission
        binding.buttonStoragePermission.isVisible = needsStoragePermission
        binding.clientList.adapter?.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        binding?.let {
            updatePermissionRequired(it)
        }
    }

    inner class NsdServiceInfoListAdapter : ListAdapter<NsdServiceInfo, NsdServiceInfoViewHolder>(
            object : DiffUtil.ItemCallback<NsdServiceInfo>() {
                override fun areItemsTheSame(oldItem: NsdServiceInfo, newItem: NsdServiceInfo) = newItem.serviceName == oldItem.serviceName
                override fun areContentsTheSame(oldItem: NsdServiceInfo, newItem: NsdServiceInfo) = newItem.serviceName == oldItem.serviceName
            }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NsdServiceInfoViewHolder {
            val view = LayoutInflater.from(this@TransferActivity).inflate(R.layout.list_item_client, parent, false)
            return NsdServiceInfoViewHolder(view)
        }

        override fun onBindViewHolder(holder: NsdServiceInfoViewHolder, position: Int) {
            return holder.bind(getItem(position))
        }
    }

    inner class NsdServiceInfoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val listItemBinding = ListItemClientBinding.bind(view)
        fun bind(client: NsdServiceInfo) {
            listItemBinding.textView.text = client.serviceName
            listItemBinding.root.isEnabled = !needsStoragePermission
            val textColor = if (needsStoragePermission) {
                Color.GRAY
            } else {
                Color.BLACK
            }

            listItemBinding.textView.setTextColor(textColor)
            listItemBinding.root.setOnClickListener {
                viewModel.endDiscovery()

                GlobalScope.launch {
                    client.host?.canonicalHostName?.let { ipaddress ->
                        val intent = Intent(this@TransferActivity, TransferService::class.java)
                        intent.putExtra(TransferService.CLIENT_NAME, client.serviceName)
                        intent.putExtra(TransferService.IP_ADDRESS, ipaddress)
                        intent.putExtra(TransferService.PORT, client.port)
                        intent.putExtra(TransferService.DATA, dataUris)
                        dataUris?.forEach {
                            grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                    }

                    launch(Dispatchers.Main) {
                        delay(200)
                        supportFinishAfterTransition()

                    }

                }
            }

        }
    }
}
