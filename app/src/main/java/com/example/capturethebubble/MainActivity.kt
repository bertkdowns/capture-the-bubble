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
        }
    }

    private fun generateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
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
