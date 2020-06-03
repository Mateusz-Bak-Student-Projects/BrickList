package com.example.bricklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bricklist.adapter.ProjectListAdapter
import com.example.bricklist.model.Project
import com.example.bricklist.utility.BrickDbHelper
import com.example.bricklist.utility.loadBrickDb
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: BrickDbHelper
    private lateinit var projects: MutableList<Project>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadBrickDb(this)
        db = BrickDbHelper(this, null)
        projects = db.getProjects()

        projectList.apply {
            setHasFixedSize(true)
            adapter = ProjectListAdapter(projects)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        addProjectButton.setOnClickListener {
            startActivity(Intent(this, CreateProjectActivity::class.java))
        }
    }

    override fun onRestart() {
        super.onRestart()
        projects.clear()
        projects.addAll(db.getProjects())
        projectList.adapter?.notifyDataSetChanged()
    }
}
