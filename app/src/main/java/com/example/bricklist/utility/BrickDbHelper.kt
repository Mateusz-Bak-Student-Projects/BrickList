package com.example.bricklist.utility

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.database.getBlobOrNull
import androidx.core.database.getIntOrNull
import com.example.bricklist.model.Inventory
import com.example.bricklist.model.InventoryItem
import com.example.bricklist.model.Project
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

fun loadBrickDb(context: Context) {
    val path = "data/data/com.example.bricklist/databases"
    val directory = File(path)
    if (!directory.exists()) {
        directory.mkdir()
    }
    val dbFile = File(directory, "BrickList.db")
    if (!dbFile.exists()) {
        dbFile.createNewFile()
        val input = context.assets.open("BrickList.db")
        val output = dbFile.outputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (input.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
        output.flush()
        output.close()
        input.close()
    }
}

class BrickDbHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "BrickList.db"
    }

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    fun createProject(name: String): Project {
        val db = writableDatabase
        val query = "select coalesce(max(id), 0) + 1, strftime('%s', 'now') from Inventories"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            val now = cursor.getInt(1)
            cursor.close()
            if (db.insert("Inventories", null, ContentValues().apply {
                    put("id", id)
                    put("Name", name)
                    put("LastAccessed", now)
                }) >= 0) {
                return Project(id, name, true, now)
            }
            throw RuntimeException("The project could not be inserted to database")
        }
        cursor.close()
        throw RuntimeException("Unable to get an id for the project")
    }

    fun updateProject(project: Project) {
        val db = writableDatabase
        if (db.update("Inventories", ContentValues().apply {
                put("Name", project.name)
                put("Active", project.active)
                put("LastAccessed", project.lastAccessed)
            }, "id=?", arrayOf(project.id.toString())) == 0
        ) {
            throw RuntimeException("The project could not be updated")
        }
    }

    fun deleteProject(project: Project) {
        val db = writableDatabase
        db.beginTransaction()
        db.delete("InventoriesParts", "InventoryID=?", arrayOf(project.id.toString()))
        if (db.delete("Inventories", "id=?", arrayOf(project.id.toString())) == 0) {
            throw RuntimeException("The project could not be deleted")
        }
        db.endTransaction()
    }

    fun getProjects(): MutableList<Project> {
        val projects = ArrayList<Project>()
        val db = readableDatabase
        val query = "select id, Name, Active, LastAccessed from Inventories"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val active = cursor.getInt(2) > 0
            val lastAccessed = cursor.getInt(3)
            projects.add(Project(id, name, active, lastAccessed))
        }
        cursor.close()
        return projects
    }

    fun createInventory(inventoryID: Int, inventory: Inventory): List<InventoryItem> {
        val items = ArrayList<InventoryItem>()
        for (item in inventory.items.filter { it.alternate == "N" }) {
            try {
                val inventoryItem = createInventoryItem(inventoryID, item)
                items.add(inventoryItem)
            } catch (e: RuntimeException) {
            }
        }
        return items
    }

    private fun createInventoryItem(inventoryID: Int, item: Inventory.Item): InventoryItem {
        val quantity = item.quantity.toIntOrNull()
            ?: throw IllegalArgumentException("Quantity is not a valid number")
        val db = writableDatabase
        var query = "select coalesce(max(id), 0) + 1 from InventoriesParts"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            cursor.close()
            query = "select id from ItemTypes where Code=?"
            db.rawQuery(query, arrayOf(item.itemType)).use { typesCursor ->
                if (!typesCursor.moveToFirst()) throw RuntimeException("Invalid type code")
                val typeID = typesCursor.getInt(0)
                query = "select id, coalesce(NamePL, Name) from Parts where Code=? and TypeID=?"
                db.rawQuery(query, arrayOf(item.itemID, typeID.toString())).use { partsCursor ->
                    if (!partsCursor.moveToFirst()) throw RuntimeException("Invalid part code")
                    val itemID = partsCursor.getInt(0)
                    val name = partsCursor.getString(1)
                    query = "select id, coalesce(NamePL, Name) from Colors where Code=?"
                    db.rawQuery(query, arrayOf(item.color)).use { colorsCursor ->
                        if (!colorsCursor.moveToFirst()) throw RuntimeException("Invalid color code")
                        val colorID = colorsCursor.getInt(0)
                        val color = colorsCursor.getString(1)
                        if (db.insert("InventoriesParts", null, ContentValues().apply {
                                put("id", id)
                                put("InventoryID", inventoryID)
                                put("TypeID", typeID)
                                put("ItemID", itemID)
                                put("QuantityInSet", quantity)
                                put("QuantityInStore", 0)
                                put("ColorID", colorID)
                                put("Extra", 0)
                            }) >= 0) {
                            return InventoryItem(
                                id,
                                itemID,
                                name,
                                color,
                                quantity,
                                item.itemID
                            ).apply {
                                image = getImage(itemID, item.itemID, colorID, item.color)
                            }
                        }
                    }
                }
            }
            throw RuntimeException("Inventory item could not be inserted to database")
        }
        cursor.close()
        throw RuntimeException("Unable to get an id for inventory item")
    }

    fun getInventory(projectID: Int): List<InventoryItem> {
        val items = ArrayList<InventoryItem>()
        val db = readableDatabase
        val query =
            "select distinct items.id, items.itemID, coalesce(Parts.NamePL, Parts.Name), " +
                    "coalesce(Colors.NamePL, Colors.Name), items.QuantityInSet, items.QuantityInStore, " +
                    "Parts.Code, Codes.Image " +
                    "from InventoriesParts items " +
                    "left join Parts on items.ItemID=Parts.id " +
                    "left join Colors on items.ColorID=Colors.id " +
                    "left join Codes on items.ItemID=Codes.ItemID and items.ColorID=Codes.ColorID " +
                    "where items.InventoryID=?"
        val cursor = db.rawQuery(query, arrayOf(projectID.toString()))
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val itemID = cursor.getInt(1)
            val name = cursor.getString(2)
            val color = cursor.getString(3)
            val inSet = cursor.getInt(4)
            val inStore = cursor.getInt(5)
            val code = cursor.getString(6)
            val imageBytes = cursor.getBlobOrNull(7)
            items.add(InventoryItem(id, itemID, name, color, inSet, code).apply {
                this.inStore = inStore
                if (imageBytes != null) {
                    image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.count())
                }
            })
        }
        cursor.close()
        return items
    }

    fun updateItemQuantity(id: Int, quantity: Int) {
        val db = writableDatabase
        if (db.update("InventoriesParts", ContentValues().apply {
                put("QuantityInStore", quantity)
            }, "id=?", arrayOf(id.toString())) == 0) {
            throw RuntimeException("Item could not be updated")
        }
    }

    private fun getImage(itemID: Int, itemCode: String, colorID: Int, colorCode: String): Bitmap? {
        val urls = ArrayList<String>()
        val db = writableDatabase
        val query = "select Code, Image from Codes where ItemID=? and ColorID=?"
        db.rawQuery(query, arrayOf(itemID.toString(), colorID.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val image = cursor.getBlobOrNull(1)
                if (image != null) return BitmapFactory.decodeByteArray(image, 0, image.count())
                val code = cursor.getIntOrNull(0) ?: 0
                urls.add("https://www.lego.com/service/bricks/5/2/$code")
            } else {
                db.rawQuery("select coalesce(max(id), 0) + 1 from Codes", null).use {
                    if (it.moveToFirst()) {
                        val id = it.getInt(0)
                        db.insert("Codes", null, ContentValues().apply {
                            put("id", id)
                            put("ItemID", itemID)
                            put("ColorID", colorID)
                        })
                    }
                }
            }
        }
        urls.add("http://img.bricklink.com/P/$colorCode/$itemCode.gif")
        urls.add("https://www.bricklink.com/PL/$itemCode.jpg")
        for (url in urls) {
            val stream = downloadUrl(url)
            if (stream != null) {
                val bytes = stream.readBytes()
                db.update("Codes", ContentValues().apply {
                    put("Image", bytes)
                }, "ItemID=? and ColorID=?", arrayOf(itemID.toString(), colorID.toString()))
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
            }
        }
        return null
    }
}
