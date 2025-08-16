package com.aubynsamuel.clipsync.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text


@Composable
fun DeviceItem(
    onChecked: () -> Unit, checked: Boolean, name: String,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp).clickable { onChecked() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        CheckBox(checked = checked, onCheckedChange = { onChecked() })

        Column(
            modifier = GlanceModifier.padding(start = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name)
        }
    }
}