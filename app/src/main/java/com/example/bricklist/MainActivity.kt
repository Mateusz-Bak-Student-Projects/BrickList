package com.example.bricklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bricklist.utility.loadBrickDb

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadBrickDb(this)
    }
}
