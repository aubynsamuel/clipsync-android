package com.aubynsamuel.clipsync.ui.navigation

import androidx.navigation.NavController

fun NavController.safePopBackStack() {
    if (this.previousBackStackEntry != null) {
        this.popBackStack()
    }
}
