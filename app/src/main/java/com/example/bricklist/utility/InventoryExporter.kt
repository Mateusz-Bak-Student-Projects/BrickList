package com.example.bricklist.utility

import android.content.Context
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class InventoryExporter(private val context: Context) {

    private val db = BrickDbHelper(context, null)

    enum class Option(val value: Int) {
        ALL(0), NEW(1), USED(2);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    data class Item(val itemID: String, val type: String, val color: String, val quantity: Int)

    @Suppress("SpellCheckingInspection")
    fun export(projectId: Int, filename: String, option: Option) {
        val items = db.getMissingItems(projectId)
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.newDocument()
        val root = document.createElement("INVENTORY")
        for (item in items) {
            val element = document.createElement("ITEM").apply {
                appendChild(getNode(document, "ITEMTYPE", item.type))
                appendChild(getNode(document, "ITEMID", item.itemID))
                appendChild(getNode(document, "COLOR", item.color))
                appendChild(getNode(document, "QTYFILLED", item.quantity.toString()))
                if (option != Option.ALL) {
                    appendChild(
                        getNode(
                            document, "CONDITION",
                            when (option) {
                                Option.NEW -> "N"
                                Option.USED -> "U"
                                else -> ""
                            }
                        )
                    )
                }
            }
            root.appendChild(element)
        }
        document.appendChild(root)
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val path = context.filesDir
        val outDir = File(path, "MissingParts")
        outDir.mkdir()
        val file = File(outDir, "$filename.xml")
        val fileStream = FileOutputStream(file)
        transformer.transform(DOMSource(document), StreamResult(fileStream))
        fileStream.close()
    }

    private fun getNode(document: Document, tag: String, value: String): Element? {
        return document.createElement(tag).apply {
            appendChild(
                document.createTextNode(value)
            )
        }
    }
}
