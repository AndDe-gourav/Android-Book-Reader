package com.timepass.bookreader.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri

@Composable
fun InfoOptionsDialog(
    onDismiss: () -> Unit,
    onFeedbackClick: () -> Unit,
    onLicenseClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onFeedbackClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                    ) {
                        Text(
                            text = "Give Feedback",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Help us improve the app",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onLicenseClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp)
                    ) {
                        Text(
                            text = "Open Source Licenses",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Third-party library notices",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LicenseScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        MuPdfLicenseCard(context = context)
    }
}

@Composable
fun MuPdfLicenseCard(context: Context) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MuPDF",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Artifex Software, Inc.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Text(
                        text = "AGPL-3.0",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }

            Text(
                text = "Used for rendering and displaying PDF documents within the app.",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://git.ghostscript.com/?p=mupdf.git".toUri()
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "View Source",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://artifex.com/licensing/".toUri()
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Artifex Licensing",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "License Summary",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(5.dp)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = MUPDF_LICENSE_SUMMARY,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

private val MUPDF_LICENSE_SUMMARY = """
This application uses MuPDF by Artifex Software, Inc.

MuPDF is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).

Under the AGPL, if you distribute this application or modified versions of MuPDF,
you must make the complete corresponding source code available under the same license.

Source code for this application is available at:
https://github.com/AndDe-gourav/Android-Book-Reader

MuPDF source code:
https://git.ghostscript.com/?p=mupdf.git

Full AGPL-3.0 license:
https://www.gnu.org/licenses/agpl-3.0.html

Commercial licensing for MuPDF is available from Artifex Software:
https://artifex.com/licensing/
""".trimIndent()
