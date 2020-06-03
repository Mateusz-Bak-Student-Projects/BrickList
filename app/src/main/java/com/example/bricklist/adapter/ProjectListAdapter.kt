package com.example.bricklist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bricklist.ProjectActivity
import com.example.bricklist.R
import com.example.bricklist.model.Project

class ProjectListAdapter(private val projectList: List<Project>) :
    RecyclerView.Adapter<ProjectListAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_list_item, parent, false) as TextView
        return ProjectViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projectList[position]
        holder.textView.apply {
            text = project.name
            setOnClickListener {
                context.startActivity(
                    Intent(context, ProjectActivity::class.java)
                        .putExtra("id", project.id)
                )
            }
        }
    }

    override fun getItemCount() = projectList.size
}
