package com.example.bricklist.utility

import android.content.Context
import com.example.bricklist.model.Inventory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream

class InventoryLoader(context: Context) {

    private val db = BrickDbHelper(context, null)

    private val xmlMapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }).registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private inline fun <reified T : Any> parse(inputStream: InputStream): T {
        return xmlMapper.readValue(inputStream)
    }

    fun load(inputStream: InputStream, inventoryId: Int): Boolean {
        val inventory = parse<Inventory>(inputStream)
        return db.createInventory(inventoryId, inventory).count() > 0
    }
}
