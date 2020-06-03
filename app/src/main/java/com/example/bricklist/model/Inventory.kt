package com.example.bricklist.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@Suppress("SpellCheckingInspection")
@JsonRootName("INVENTORY")
data class Inventory(
    @set:JsonProperty("ITEM")
    var items: List<Item> = ArrayList()
) {
    @Suppress("SpellCheckingInspection")
    @JsonRootName("ITEM")
    data class Item(
        @set:JsonProperty("ITEMTYPE")
        var itemType: String = "",
        @set:JsonProperty("ITEMID")
        var itemID: String = "",
        @set:JsonProperty("QTY")
        var quantity: String = "0",
        @set:JsonProperty("COLOR")
        var color: String = "",
        @set:JsonProperty("EXTRA")
        var extra: String = "",
        @set:JsonProperty("ALTERNATE")
        var alternate: String = "",
        @set:JsonProperty("MATCHID")
        var matchID: String? = null,
        @set:JsonProperty("COUNTERPART")
        var counterpart: String? = null
    )
}
