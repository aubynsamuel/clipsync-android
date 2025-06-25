package com.aubynsamuel.clipsync.ui.viewModel

import androidx.lifecycle.ViewModel
import com.aubynsamuel.clipsync.core.RecentListManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecentViewModel(private val manager: RecentListManager) : ViewModel() {
    private val _recentItems = MutableStateFlow<List<String>>(manager.getAll())
    val recentItems: StateFlow<List<String>> = _recentItems

    fun addRecent(item: String) {
        manager.add(item)
        _recentItems.value = manager.getAll()
    }

//    fun removeRecent(item: String) {
//        manager.remove(item)
//        _recentItems.value = manager.getAll()
//    }

//    fun clearAll() {
//        manager.clear()
//        _recentItems.value = emptyList()
//    }
}