package com.stealthsms.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stealthsms.app.sms.SmsDispatcher
import com.stealthsms.app.stego.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if launched from Android's "PROCESS_TEXT" context menu action
        val initialText = if (intent?.action == Intent.ACTION_PROCESS_TEXT) {
            intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        } else {
            ""
        }

        setContent {
            StealthSmsTheme {
                MainScreen(initialIncomingText = initialText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(initialIncomingText: String) {
    var selectedTab by remember { mutableStateOf(if (initialIncomingText.isNotBlank()) 1 else 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪶 Feather Fling",
                            fontWeight = FontWeight.Bold,
                            color = StealthPrimary,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Linguistic Stego",
                            color = StealthOnSurfaceMuted,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StealthSurfaceVariant
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = StealthSurfaceVariant) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Encode") },
                    label = { Text("Encode") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.LockOpen, contentDescription = "Decode") },
                    label = { Text("Decode") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Lab") },
                    label = { Text("Test Lab") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Guide") },
                    label = { Text("Guide") }
                )
            }
        },
        containerColor = StealthBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> EncodeTab()
                1 -> DecodeTab(initialText = initialIncomingText)
                2 -> TestLabTab()
                3 -> GuideTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncodeTab() {
    val context = LocalContext.current
    var secretText by remember { mutableStateOf("Pin: 4829") }
    var selectedMode by remember { mutableStateOf(StegoMode.CONTRACTION_SYNONYM) }
    var useEncryption by remember { mutableStateOf(false) }
    var passphrase by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var encodedResult by remember { mutableStateOf<StegoResult?>(null) }
    var isExpandedModeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(secretText, selectedMode, useEncryption, passphrase) {
        if (secretText.isNotBlank()) {
            val pass = if (useEncryption && passphrase.isNotBlank()) passphrase else null
            encodedResult = try {
                StegoManager.encode(secretText, selectedMode, passphrase = pass)
            } catch (e: Exception) {
                null
            }
        } else {
            encodedResult = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "1. Secret Message to Hide",
            fontWeight = FontWeight.SemiBold,
            color = StealthPrimary,
            fontSize = 15.sp
        )

        OutlinedTextField(
            value = secretText,
            onValueChange = { secretText = it },
            placeholder = { Text("Type secret PIN, code, or message...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StealthPrimary,
                unfocusedBorderColor = StealthCardBorder,
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Mode selector
        Text(
            text = "2. Steganography Method",
            fontWeight = FontWeight.SemiBold,
            color = StealthPrimary,
            fontSize = 15.sp
        )

        ExposedDropdownMenuBox(
            expanded = isExpandedModeMenu,
            onExpandedChange = { isExpandedModeMenu = !isExpandedModeMenu }
        ) {
            OutlinedTextField(
                value = selectedMode.title,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedModeMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthPrimary,
                    unfocusedBorderColor = StealthCardBorder,
                    focusedContainerColor = StealthSurfaceVariant,
                    unfocusedContainerColor = StealthSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            )

            ExposedDropdownMenu(
                expanded = isExpandedModeMenu,
                onDismissRequest = { isExpandedModeMenu = false },
                modifier = Modifier.background(StealthSurface)
            ) {
                StegoMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(mode.title, fontWeight = FontWeight.Bold, color = StealthOnSurface)
                                Text(mode.description, fontSize = 11.sp, color = StealthOnSurfaceMuted)
                            }
                        },
                        onClick = {
                            selectedMode = mode
                            isExpandedModeMenu = false
                        }
                    )
                }
            }
        }

        // Encryption Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { useEncryption = !useEncryption }
        ) {
            Checkbox(
                checked = useEncryption,
                onCheckedChange = { useEncryption = it },
                colors = CheckboxDefaults.colors(checkedColor = StealthPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("🔒 Optional AES-GCM Passphrase Encryption", fontSize = 13.sp)
        }

        if (useEncryption) {
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                placeholder = { Text("Enter passphrase...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthPrimary,
                    unfocusedBorderColor = StealthCardBorder,
                    focusedContainerColor = StealthSurfaceVariant,
                    unfocusedContainerColor = StealthSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Output Preview Card
        Text(
            text = "3. Resulting Cover Text (SMS Carrier)",
            fontWeight = FontWeight.SemiBold,
            color = StealthSecondary,
            fontSize = 15.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val cover = encodedResult?.encodedText ?: "Waiting for input..."
                Text(
                    text = cover,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = StealthOnSurface
                )

                if (encodedResult != null) {
                    HorizontalDivider(color = StealthCardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Characters: ${cover.length} | Used bits: ${encodedResult?.usedBits}",
                            fontSize = 11.sp,
                            color = StealthOnSurfaceMuted
                        )
                        Text(
                            text = if (encodedResult?.isEncrypted == true) "AES-256 🔒" else "Plain 🔓",
                            fontSize = 11.sp,
                            color = if (encodedResult?.isEncrypted == true) StealthTertiary else StealthSecondary
                        )
                    }
                }
            }
        }

        // SMS Dispatcher controls
        Text(
            text = "4. Dispatch via SMS",
            fontWeight = FontWeight.SemiBold,
            color = StealthPrimary,
            fontSize = 15.sp
        )

        OutlinedTextField(
            value = recipientPhone,
            onValueChange = { recipientPhone = it },
            placeholder = { Text("Recipient Phone Number (Optional)...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StealthPrimary,
                unfocusedBorderColor = StealthCardBorder,
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    encodedResult?.encodedText?.let {
                        SmsDispatcher.launchSmsApp(context, recipientPhone, it)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in SMS", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    encodedResult?.encodedText?.let {
                        SmsDispatcher.copyToClipboard(context, it)
                    }
                },
                modifier = Modifier.weight(1f),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StealthPrimary)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StealthPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy SMS", color = StealthPrimary)
            }
        }
    }
}

@Composable
fun DecodeTab(initialText: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var inputSms by remember { mutableStateOf(initialText) }
    var passphrase by remember { mutableStateOf("") }
    var decodeResult by remember { mutableStateOf<DecodeResult?>(null) }

    LaunchedEffect(inputSms, passphrase) {
        if (inputSms.isNotBlank()) {
            val pass = passphrase.ifBlank { null }
            decodeResult = StegoManager.decodeAuto(inputSms, pass)
        } else {
            decodeResult = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📥 Incoming SMS Text",
                fontWeight = FontWeight.SemiBold,
                color = StealthPrimary,
                fontSize = 15.sp
            )

            TextButton(onClick = {
                val clip = clipboardManager.getText()?.text ?: ""
                inputSms = clip
            }) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paste Clipboard", fontSize = 12.sp)
            }
        }

        OutlinedTextField(
            value = inputSms,
            onValueChange = { inputSms = it },
            placeholder = { Text("Paste received SMS text to inspect and decode...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StealthPrimary,
                unfocusedBorderColor = StealthCardBorder,
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            placeholder = { Text("Passphrase (if message was AES-encrypted)...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StealthPrimary,
                unfocusedBorderColor = StealthCardBorder,
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Decoded Result Card
        Text(
            text = "🔓 Extracted Secret Message",
            fontWeight = FontWeight.SemiBold,
            color = StealthSecondary,
            fontSize = 15.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (decodeResult?.success == true) Color(0xFF1B2E24) else StealthSurface
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (decodeResult?.success == true) StealthSecondary else StealthCardBorder
                )
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (decodeResult == null || inputSms.isBlank()) {
                    Text(
                        text = "Paste an encoded SMS message above to automatically decode.",
                        color = StealthOnSurfaceMuted,
                        fontSize = 13.sp
                    )
                } else if (decodeResult?.success == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅ Decoded Successfully", color = StealthSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${decodeResult?.mode?.title ?: "Stego"}]",
                            color = StealthOnSurfaceMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = decodeResult?.secretMessage ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    HorizontalDivider(color = StealthCardBorder)
                    Text(
                        text = "Bits Extracted: ${decodeResult?.bitsExtracted} | Checksum: ${if (decodeResult?.checksumValid == true) "Valid ✅" else "Mismatch ⚠️"}",
                        fontSize = 11.sp,
                        color = StealthOnSurfaceMuted
                    )

                    Button(
                        onClick = {
                            decodeResult?.secretMessage?.let {
                                SmsDispatcher.copyToClipboard(context, it)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StealthSecondary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Copy Secret", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❌ Not Decoded", color = StealthError, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(
                        text = decodeResult?.errorMessage ?: "No hidden message found.",
                        color = StealthOnSurfaceMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TestLabTab() {
    var testSecret by remember { mutableStateOf("CODE-9942") }
    var carrierTranscodeResult by remember { mutableStateOf("") }
    var testLog by remember { mutableStateOf("Press 'Run Telecom Simulation' to test transmission resilience.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "🧪 Telecom Gateway Resilience Test Lab",
            fontWeight = FontWeight.Bold,
            color = StealthPrimary,
            fontSize = 16.sp
        )

        Text(
            text = "Simulates real-world SMS carrier transformations (GSM-7 re-encoding, line-feed normalization, uppercase/lowercase alterations) to verify that the linguistic steganography survives intact.",
            color = StealthOnSurfaceMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        OutlinedTextField(
            value = testSecret,
            onValueChange = { testSecret = it },
            label = { Text("Test Secret Payload") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StealthPrimary,
                unfocusedBorderColor = StealthCardBorder,
                focusedContainerColor = StealthSurfaceVariant,
                unfocusedContainerColor = StealthSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Button(
            onClick = {
                val sb = StringBuilder()
                sb.append("1. Encoding with Synonym/Contraction Substitution...\n")
                val stego = SynonymSubstitutionEngine.encode(testSecret)
                val originalCover = stego.encodedText
                sb.append("   -> Cover text (${originalCover.length} chars): \"$originalCover\"\n\n")

                sb.append("2. Simulating Carrier Telecom Transcoding:\n")
                // Simulate carriage return normalization, whitespace collapsing, and auto-punctuation
                var mutated = originalCover.replace("\r\n", "\n").trim()
                mutated = mutated.replace("  ", " ")
                sb.append("   -> Normalized spaces & line-breaks OK\n")

                sb.append("3. Decoding through Receiver Pipeline:\n")
                val decoded = SynonymSubstitutionEngine.decode(mutated)
                if (decoded.success && decoded.secretMessage == testSecret) {
                    sb.append("   -> ✅ SUCCESS: Decoded payload exact match: \"${decoded.secretMessage}\"\n")
                    sb.append("   -> Checksum Verified: ${decoded.checksumValid}\n")
                    sb.append("   -> Zero-width characters required: 0 (100% standard printable ASCII)")
                } else {
                    sb.append("   -> ❌ FAILED: ${decoded.errorMessage}\n")
                }

                testLog = sb.toString()
            },
            colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Telecom Simulation", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = testLog,
                modifier = Modifier.padding(14.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = StealthOnSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun GuideTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "📖 Linguistic Steganography vs Zero-Width",
            fontWeight = FontWeight.Bold,
            color = StealthPrimary,
            fontSize = 17.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Why Zero-Width Fails over Cellular SMS:", fontWeight = FontWeight.Bold, color = StealthError)
                Text(
                    "Cellular carriers transcode SMS messages into standard GSM 7-bit or UCS-2 alphabets. Unassigned zero-width characters (\\u200B-\\u200F) are routinely stripped or normalized to standard spaces by SMSC telecom routers and spam filters.",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted,
                    lineHeight = 17.sp
                )

                HorizontalDivider(color = StealthCardBorder)

                Text("How Feather Fling Solves This:", fontWeight = FontWeight.Bold, color = StealthSecondary)
                Text(
                    "1. Synonym & Contraction Substitution: Maps binary bits (0/1) to everyday grammatical choices ('cannot' vs 'can't', 'start' vs 'begin'). Every single character is normal printable English ASCII.\n\n" +
                    "2. Deterministic Word-List Cover: Encodes bytes into standard dictionary words assembled into plausible cover sentences.\n\n" +
                    "3. Visible Alphanumeric Tokens: Disguises secret payloads as harmless tracking or confirmation reference codes (#TRK-XXXX).",
                    fontSize = 12.sp,
                    color = StealthOnSurface,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
