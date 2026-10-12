// SPDX-License-Identifier: Apache-2.0
package dev.openautoai.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.openautoai.app.design.*

@Composable internal fun LicenseDialog(close:()->Unit) {
    val assets=LocalContext.current.assets
    val licenses=remember { listOf("OFL-DM-Sans.txt","OFL-DM-Serif-Display.txt","MIT-PatternFly-Icons.txt","CC-BY-4.0-Font-Awesome.txt").map {
        assets.open("licenses/$it").bufferedReader().use { reader -> reader.readText() }
    } }
    AzDialog(close,title={Text(stringResource(R.string.az_licenses))},text={
        Column(verticalArrangement=Arrangement.spacedBy(24.dp)) { licenses.forEach { Text(it,style=AzType.body) } }
    },confirmButton={AzPrimaryButton(close) { Text(stringResource(R.string.az_close)) }},dismissButton={})
}
