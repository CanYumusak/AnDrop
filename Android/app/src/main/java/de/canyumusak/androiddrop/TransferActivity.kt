package de.canyumusak.androiddrop

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.canyumusak.androiddrop.databinding.ListItemClientBinding
import de.canyumusak.androiddrop.databinding.RootFragmentBinding
import de.canyumusak.androiddrop.ui.root.DiscoveryViewModel
import de.mannodermaus.rxbonjour.BonjourService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TransferActivity : AppCompatActivity() {

    private lateinit var viewModel: DiscoveryViewModel

    private val dataUri: Uri?
        get() = intent.extras?.get(TransferService.DATA) as Uri?

    private val needsStoragePermission: Boolean
        get() = viewModel.needsStoragePermission(dataUri)

    var binding: RootFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = DataBindingUtil.inflate<RootFragmentBinding>(layoutInflater, R.layout.root_fragment, null, false)

        viewModel = ViewModelProviders.of(this).get(DiscoveryViewModel::class.java)
        viewModel.discoverClients()

        inflate.viewModel = viewModel

        with(inflate.clientList) {
            val clientListAdapter = BonjourServiceListAdapter()
            adapter = clientListAdapter
            layoutManager = LinearLayoutManager(context)

            viewModel.clients.observe(this@TransferActivity, Observer {

                if (it.isNotEmpty()) {
                    inflate.emptyLayout.emptyLayoutRoot.visibility = View.INVISIBLE
                } else {
                    inflate.emptyLayout.emptyLayoutRoot.visibility = View.VISIBLE
                }

                clientListAdapter.submitList(it)
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

    inner class BonjourServiceListAdapter : ListAdapter<BonjourService, BonjourServiceViewHolder>(
            object : DiffUtil.ItemCallback<BonjourService>() {
                override fun areItemsTheSame(oldItem: BonjourService, newItem: BonjourService) = newItem.name == oldItem.name
                override fun areContentsTheSame(oldItem: BonjourService, newItem: BonjourService) = newItem.name == oldItem.name
            }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BonjourServiceViewHolder {
            val view = LayoutInflater.from(this@TransferActivity).inflate(R.layout.list_item_client, parent, false)
            return BonjourServiceViewHolder(view)
        }

        override fun onBindViewHolder(holder: BonjourServiceViewHolder, position: Int) {
            return holder.bind(getItem(position))
        }
    }

    inner class BonjourServiceViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val listItemBinding = DataBindingUtil.bind<ListItemClientBinding>(view)
        fun bind(client: BonjourService) {
            listItemBinding?.client = client
            listItemBinding?.root?.isEnabled = !needsStoragePermission
            val textColor = if (needsStoragePermission) {
                Color.GRAY
            } else {
                Color.BLACK
            }

            listItemBinding?.textView?.setTextColor(textColor)
            listItemBinding?.root?.setOnClickListener {
                viewModel.endDiscovery()

                GlobalScope.launch {
                    client.v4Host?.canonicalHostName?.let { ipaddress ->
                        val intent = Intent(this@TransferActivity, TransferService::class.java)
                        intent.putExtra(TransferService.CLIENT_NAME, client.name)
                        intent.putExtra(TransferService.IP_ADDRESS, ipaddress)
                        intent.putExtra(TransferService.PORT, client.port)
                        intent.putExtra(TransferService.DATA, dataUri)
                        grantUriPermission(packageName, dataUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

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
