package com.example.bricklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bricklist.adapter.ProjectListAdapter
import com.example.bricklist.model.Project
import com.example.bricklist.utility.BrickDbHelper
import com.example.bricklist.utility.loadBrickDb
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: BrickDbHelper
    private val projects = mutableListOf<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadBrickDb(this)
        db = BrickDbHelper(this, null)

        projectList.apply {
            setHasFixedSize(true)
            adapter = ProjectListAdapter(projects)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        addProjectButton.setOnClickListener {
            startActivity(Intent(this, CreateProjectActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val showArchived = preferences.getBoolean("show_archived", false)
        projects.clear()
        projects.addAll(db.getProjects()
            .filter { showArchived || it.active }
            .sortedByDescending { it.lastAccessed })
        projectList.adapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
