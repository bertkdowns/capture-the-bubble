import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val nfcViewModel: NfcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Log.e("NFC", "NFC is not supported on this device.")
        }

        setContent {
            MaterialTheme {
                NfcScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(android.content.IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                nfcViewModel.handleTagDetected(it)
            }
        }
    }
}

@Composable
fun NfcScreen(viewModel: NfcViewModel = viewModel()) {
    val isTagInProximity by viewModel.isTagInProximity.collectAsState()
    val tagHash by viewModel.tagHash.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isTagInProximity) "Tag detected: $tagHash" else "Waiting for a tag...")
    }
}

class NfcViewModel : ViewModel() {

    private val _isTagInProximity = mutableStateOf(false)
    val isTagInProximity: State<Boolean> get() = _isTagInProximity

    private val _tagHash = mutableStateOf("")
    val tagHash: State<String> get() = _tagHash

    fun handleTagDetected(tag: Tag) {
        if (!_isTagInProximity.value) {
            _isTagInProximity.value = true
            _tagHash.value = generateHash(tag.id)

            // Simulate tag removal after a delay
            simulateTagRemoval()
        }
    }

    private fun generateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun simulateTagRemoval() {
        androidx.lifecycle.viewModelScope.launch {
            delay(5000L) // 5 seconds
            _isTagInProximity.value = false
            _tagHash.value = ""
        }
    }
}
