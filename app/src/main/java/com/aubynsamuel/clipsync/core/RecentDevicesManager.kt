package com.aubynsamuel.clipsync.core

import android.content.Context
import androidx.core.content.edit

class RecentDevicesManager(context: Context) {
    private val prefs = context.getSharedPreferences("recent_items", Context.MODE_PRIVATE)
    private val key = "recent_list"

    private val maxSize = 4
    private var recentItems = LinkedHashSet<String>()

    init {
        load()
    }

    private fun load() {
        val saved = prefs.getStringSet(key, emptySet())
        // Convert to LinkedHashSet to maintain order
        recentItems = LinkedHashSet(saved ?: emptySet())
    }

    private fun save() {
        prefs.edit { putStringSet(key, recentItems) }
    }

    fun getAll(): List<String> {
        return recentItems.toList()
    }

    fun add(item: String) {
        // Remove if exists to move it to end
        recentItems.remove(item)
        recentItems.add(item)

        // Enforce max size
        if (recentItems.size > maxSize) {
            val toRemove = recentItems.first()
            recentItems.remove(toRemove)
        }
        save()
    }
}