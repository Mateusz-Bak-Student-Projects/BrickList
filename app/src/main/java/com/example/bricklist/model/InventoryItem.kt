package com.example.bricklist.model

import android.graphics.Bitmap

data class InventoryItem(
    val id: Int,
    val itemID: Int,
    val name: String,
    val color: String,
    val inSet: Int,
    val code: String
) {
    var inStore: Int = 0
    var image: Bitmap? = null
}
