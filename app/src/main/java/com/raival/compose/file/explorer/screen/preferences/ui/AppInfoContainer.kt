package com.raival.compose.file.explorer.screen.preferences.ui

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.screen.logs.LogsActivity
import com.raival.compose.file.explorer.screen.preferences.misc.exportPreferences
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AppInfoContainer() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Container(title = stringResource(R.string.other)) {
        PreferenceItem(
            label = stringResource(R.string.export_preferences),
            supportingText = stringResource(R.string.export_preferences_desc),
            icon = Icons.Rounded.Upload,
            onClick = {
                scope.launch {
                    val data = exportPreferences()
                    File(globalClass.appFiles.file, "preferences.prismPrefs").writeText(data)
                    showMsg(globalClass.getString(R.string.exported_prism_preferences))
                }
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.title_activity_logs),
            supportingText = stringResource(R.string.view_application_logs),
            icon = Icons.Rounded.BugReport,
            onClick = {
                context.startActivity(Intent(context, LogsActivity::class.java))
            }
        )
    }
}