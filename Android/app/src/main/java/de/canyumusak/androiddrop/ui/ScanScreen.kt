package de.canyumusak.androiddrop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.canyumusak.androiddrop.AnDropClient
import de.canyumusak.androiddrop.DiscoveryViewModel
import de.canyumusak.androiddrop.R
import de.canyumusak.androiddrop.theme.Alphas
import de.canyumusak.androiddrop.theme.AnDropTheme
import de.canyumusak.androiddrop.theme.Spacings

@Composable
fun ScanScreen(
    discoveryViewModel: DiscoveryViewModel,
    permissionRequested: () -> Unit,
    clientSelected: (AnDropClient) -> Unit,
) {
    val list by discoveryViewModel.clients.collectAsState()
    val permissionMissing by discoveryViewModel.needsStoragePermission.collectAsState()
    ScanScreen(
        list = list,
        permissionRequested,
        clientSelected,
        permissionMissing,
    )
}

@Composable
private fun ScanScreen(
    list: List<AnDropClient>,
    permissionRequested: () -> Unit,
    clientSelected: (AnDropClient) -> Unit,
    permissionMissing: Boolean,
) {
    Surface(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(all = Spacings.s)
            .fillMaxWidth()
    ) {
        Column {
            Title()
            if (list.isEmpty()) {
                Empty()
            } else {
                Clients(list, onClick = clientSelected, permissionMissing = permissionMissing)
            }
            if (permissionMissing) {
                PermissionIndicator(permissionRequested = permissionRequested)
            }
        }
    }
}

@Composable
private fun Title() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = Spacings.m,
            end = Spacings.m,
            top = Spacings.s,
            bottom = Spacings.xs
        )
    ) {
        Image(
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.primary,
                BlendMode.SrcAtop,
            ),
            painter = painterResource(R.drawable.ic_share_20),
            contentDescription = "Icon"
        )
        Text(
            stringResource(id = R.string.discovery_fragment_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Spacings.s)
        )
    }
}

@Composable
private fun Clients(list: List<AnDropClient>, onClick: (AnDropClient) -> Unit, permissionMissing: Boolean) {
    Column {
        list.forEach {
            key(it) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .alpha(if (permissionMissing) Alphas.disabled else 1.0f)
                        .clickable(enabled = !permissionMissing, onClick = { onClick(it) })
                        .padding(horizontal = Spacings.m, vertical = Spacings.xxs)
                        .fillMaxWidth()
                ) {
                    ClientInformation(it.name)
                }
            }
        }
    }
}

@Composable
private fun Empty() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(all = Spacings.m)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(24.dp),
            strokeWidth = 2.dp,
        )
        Box(modifier = Modifier.size(Spacings.s))
        Text(
            text = stringResource(id = R.string.searching_clients),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun ClientInformation(name: String) {
    Column(modifier = Modifier.padding(vertical = Spacings.xs)) {
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun PermissionIndicator(permissionRequested: () -> Unit) {
    Column(Modifier.padding(top = Spacings.xs, start = Spacings.m, end = Spacings.m)) {
        Text(
            text = stringResource(id = R.string.storage_access_permission),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        TextButton(
            onClick = permissionRequested,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.grant_storage_permission_button_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Preview
@Composable
fun ScanScreenPreview() {
    AnDropTheme {
        ScanScreen(
            listOf(
                AnDropClient("Can's MacBook Pro"),
                AnDropClient("Adrian's MacBook Pro"),
            ),
            permissionMissing = false,
            permissionRequested = {},
            clientSelected = {},
        )
    }
}

@Preview
@Composable
fun ScanScreenPreviewPermission() {
    AnDropTheme {
        ScanScreen(
            listOf(
                AnDropClient("Can's MacBook Pro"),
                AnDropClient("Adrian's MacBook Pro"),
            ),
            permissionMissing = true,
            permissionRequested = {},
            clientSelected = {},
        )
    }
}

@Preview
@Composable
fun ScanScreenEmptyPreview() {
    AnDropTheme {
        ScanScreen(
            listOf(),
            permissionMissing = true,
            permissionRequested = {},
            clientSelected = {},
        )
    }
}
