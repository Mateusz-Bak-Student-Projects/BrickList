package com.example.bricklist.utility

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun downloadUrl(urlString: String): InputStream? {
    return try {
        val url = URL(urlString)
        (url.openConnection() as? HttpURLConnection)?.run {
            readTimeout = 10000
            connectTimeout = 15000
            requestMethod = "GET"
            doInput = true
            connect()
            inputStream
        }
    } catch (e: FileNotFoundException) {
        null
    }
}
