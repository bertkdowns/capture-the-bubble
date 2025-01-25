package com.example.capturethebubble
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import java.util.Objects
import android.os.AsyncTask
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var isTagInProximity = mutableStateOf(false)
    private var tagHash = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Log.d("NFC", "Tag detected: ${it.id}")
                handleTagDetected(it)
            }
        }

        setContent {
            MaterialTheme {
                NfcScreen(isTagInProximity = isTagInProximity.value, tagHash = tagHash.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


    private fun handleTagDetected(tag: Tag) {
        if (!isTagInProximity.value) {
            isTagInProximity.value = true
            tagHash.value = generateHash(tag.id)
            Log.i("TAG", "HASH")
            Log.i("TAG", tagHash.value)
            sendGetRequest(tagHash.value)

        }
    }

    private fun generateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun sendGetRequest(hash: String): String? {
        val url = "https://capture_the_bubble.letsgo.hs.vc/api/scan/$hash"
        val urlObj = URL(url)
        var connection: HttpURLConnection? = null
        var response: String? = null

        try {
            connection = urlObj.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // Set timeout in milliseconds
            connection.readTimeout = 5000 // Set read timeout in milliseconds

            // Get the response code and handle accordingly
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) { // Success
                response = connection.inputStream.bufferedReader().readText()
            } else {
                response = "Error: $responseCode"
            }
        } catch (e: Exception) {
            response = "Exception: ${e.message}"
        } finally {
            connection?.disconnect()
        }

        return response
    }

}

@Composable
fun NfcScreen(isTagInProximity: Boolean, tagHash: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isTagInProximity) "Tag detected: $tagHash" else "Waiting for a tag...")
    }
}
