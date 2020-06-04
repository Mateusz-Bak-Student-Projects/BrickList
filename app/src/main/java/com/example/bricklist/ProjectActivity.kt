package com.example.bricklist

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bricklist.adapter.BrickListAdapter
import com.example.bricklist.model.InventoryItem
import com.example.bricklist.model.Project
import com.example.bricklist.utility.BrickDbHelper
import kotlinx.android.synthetic.main.activity_project.*


class ProjectActivity : AppCompatActivity() {

    private lateinit var db: BrickDbHelper
    private lateinit var items: MutableList<InventoryItem>
    private var project: Project? = null

    private enum class OrderBy { CODE, COLOR }

    private var orderBy = OrderBy.CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        project = Project(
            intent.getIntExtra("id", -1),
            intent.getStringExtra("name")!!,
            intent.getBooleanExtra("active", true),
            0
        )

        title = project?.name

        db = BrickDbHelper(this, null)
        items = if (project != null) db.getInventory(project!!.id) else mutableListOf()

        brickList.apply {
            adapter = BrickListAdapter(items)
            layoutManager = LinearLayoutManager(this@ProjectActivity)
            itemAnimator?.changeDuration = 0
        }
    }

    override fun onPause() {
        super.onPause()
        if (project != null) {
            for (item in items.filter { it.dirty }) {
                db.updateItemQuantity(item.id, item.inStore)
            }
            db.updateProject(project!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        when (orderBy) {
            OrderBy.CODE -> {
                menu?.findItem(R.id.orderByCode)?.isVisible = false
                menu?.findItem(R.id.orderByColor)?.isVisible = true
            }
            OrderBy.COLOR -> {
                menu?.findItem(R.id.orderByColor)?.isVisible = false
                menu?.findItem(R.id.orderByCode)?.isVisible = true
            }
        }
        when (project?.active) {
            true -> {
                menu?.findItem(R.id.unarchive)?.isVisible = false
                menu?.findItem(R.id.archive)?.isVisible = true
            }
            false -> {
                menu?.findItem(R.id.archive)?.isVisible = false
                menu?.findItem(R.id.unarchive)?.isVisible = true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.orderByCode -> {
                orderBy = OrderBy.CODE
                val sorted = items.sortedBy { it.code }
                items.clear()
                items.addAll(sorted)
                brickList.adapter?.notifyDataSetChanged()
            }
            R.id.orderByColor -> {
                orderBy = OrderBy.COLOR
                val sorted = items.sortedBy { it.color }
                items.clear()
                items.addAll(sorted)
                brickList.adapter?.notifyDataSetChanged()
            }
            R.id.export -> {

            }
            R.id.archive -> {
                project?.active = false
            }
            R.id.unarchive -> {
                project?.active = true
            }
            R.id.delete -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Delete project")
                builder.setMessage("Are you sure you want to delete this project?")
                builder.setCancelable(false)
                builder.setPositiveButton(
                    "Delete"
                ) { dialog, _ ->
                    project?.let { db.deleteProject(it) }
                    project = null
                    dialog.dismiss()
                    finish()
                }

                builder.setNegativeButton(
                    "Cancel"
                ) { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
