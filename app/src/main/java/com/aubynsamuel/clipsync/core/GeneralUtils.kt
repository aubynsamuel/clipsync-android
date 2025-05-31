package com.aubynsamuel.clipsync.core

import android.content.Context
import android.widget.Toast

fun showToast(msg: String, context: Context) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}