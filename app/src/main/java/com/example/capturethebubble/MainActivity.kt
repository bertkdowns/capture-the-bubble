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

        Log.i("TAG","Oncreate Found Intent tag")
        Log.i("TAG", intent.toString())

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Log.e("NFC", "NFC is not supported on this device.")
        }

        setContent {
            MaterialTheme {
                NfcScreen(isTagInProximity = isTagInProximity.value, tagHash = tagHash.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(android.content.IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
        Log.i("TAG","Starting to listen for tags")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        Log.i("TAG","Pausing tag listening")
    }

    override fun onNewIntent(intent: Intent) {
        Log.i("TAG","Found Intent tag")
        Log.i("TAG", intent.toString())
        super.onNewIntent(intent)

        Log.d("NFC", "Intent action: ${intent.action}")
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            var data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_DATA)
            Log.i("NFC",data.toString())
        }
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Log.d("NFC", "Tag detected: ${it.id}")
                handleTagDetected(it)
            }
        } else {
            Log.e("NFC", "Unexpected intent action: ${intent.action}")
        }


        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                handleTagDetected(it)
            }
        }
    }

    private fun handleTagDetected(tag: Tag) {
        if (!isTagInProximity.value) {
            isTagInProximity.value = true
            tagHash.value = generateHash(tag.id)

            // Simulate tag removal after a delay using Handler
            simulateTagRemoval()
        }
    }

    private fun generateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun simulateTagRemoval() {
        // Use Handler to post a delayed task on the main thread
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            isTagInProximity.value = false
            tagHash.value = ""
        }, 5000L) // 5 seconds
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
