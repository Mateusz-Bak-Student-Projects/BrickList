package com.example.bricklist.model

import android.graphics.Bitmap

data class InventoryItem(
    val id: Int,
    val itemID: Int,
    val name: String,
    val color: String,
    val inSet: Int,
    val code: String,
    val extra: Boolean = false
) {
    var inStore: Int = 0
        set(value) {
            field = value
            dirty = true
        }
    var image: Bitmap? = null
    var dirty = false
        private set
}
