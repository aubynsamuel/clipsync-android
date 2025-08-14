package com.aubynsamuel.clipsync.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ClipSyncWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClipSyncWidget()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onEnabled(context: Context) {
        try {
            super.onEnabled(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDisabled(context: Context) {
        try {
            super.onDisabled(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}