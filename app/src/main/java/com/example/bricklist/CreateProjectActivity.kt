package com.example.bricklist

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bricklist.utility.BrickDbHelper
import com.example.bricklist.utility.InventoryLoader
import com.example.bricklist.utility.downloadUrl
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_project.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class CreateProjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        val db = BrickDbHelper(this, null)

        addButton.setOnClickListener {
            val context = this as Context
            GlobalScope.launch {
                try {
                    val setID = setIdText.text.toString().toInt()
                    val project = db.createProject(nameText.text.toString())
                    val url = "http://fcds.cs.put.poznan.pl/MyWeb/BL/$setID.xml"
                    val inputStream = downloadUrl(url)
                    if (inputStream != null && InventoryLoader(context).load(
                            inputStream,
                            project.id
                        )
                    ) {
                        startActivity(
                            Intent(context, ProjectActivity::class.java)
                                .putExtra("id", project.id)
                        )
                    } else {
                        db.deleteProject(project)
                        Snackbar.make(
                            findViewById(R.id.addButton),
                            R.string.error_set_not_found,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    setIdText.text.clear()
                }
            }
        }
    }
}
