package com.example.bricklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bricklist.adapter.BrickListAdapter
import com.example.bricklist.model.InventoryItem
import com.example.bricklist.utility.BrickDbHelper
import kotlinx.android.synthetic.main.activity_project.*

class ProjectActivity : AppCompatActivity() {

    private lateinit var db: BrickDbHelper
    private lateinit var items: List<InventoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        title = intent.getStringExtra("name")

        db = BrickDbHelper(this, null)
        items = db.getInventory(intent.getIntExtra("id", -1))

        brickList.apply {
            adapter = BrickListAdapter(items)
            layoutManager = LinearLayoutManager(this@ProjectActivity)
            itemAnimator?.changeDuration = 0
        }
    }

    override fun onPause() {
        super.onPause()
        for (item in items.filter { it.dirty }) {
            db.updateItemQuantity(item.id, item.inStore)
        }
    }
}
