package com.stealthsms.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stealthsms.app.R
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.stego.StegoManager
import com.stealthsms.app.stego.StegoMode

@Composable
fun KeyboardAssistantTab() {
    val context = LocalContext.current
    val prefs = remember { FeatherFlingPreferences(context) }

    var selectedMode by remember { mutableStateOf(prefs.defaultMode) }
    var passphrase by remember { mutableStateOf(prefs.defaultPassphrase) }
    var showOnlyWhenTyping by remember { mutableStateOf(prefs.showOnlyWhenTyping) }

    // Test Sandbox state
    var sandboxMessage by remember { mutableStateOf("Hey, let's meet at 9 PM tonight at the docks.") }
    var sandboxChatHistory by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) }
    var lastDecodedSecret by remember { mutableStateOf<String?>(null) }

    val isAccessibilityEnabled = remember(context) { isAccessibilityServiceEnabled(context) }
    val isKeyboardEnabled = remember(context) { isInputMethodEnabled(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card with Cartoon Bird Icon
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_bird_avatar),
                    contentDescription = "Feather Fling Cartoon Bird",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "🔒 Virtual Keyboard & Floating Lock",
                        fontWeight = FontWeight.Bold,
                        color = StealthPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Type in Messenger, tap 🔒 Lock, and instantly replace your text with carrier-proof cover text ready to send!",
                        fontSize = 12.sp,
                        color = StealthOnSurfaceMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 1: Floating Lock Assistant (Accessibility & Overlay)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = StealthPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Floating Lock Assistant",
                            fontWeight = FontWeight.Bold,
                            color = StealthOnSurface,
                            fontSize = 15.sp
                        )
                    }

                    Surface(
                        color = if (isAccessibilityEnabled) Color(0xFF00C853).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isAccessibilityEnabled) "Active" else "Needs Permission",
                            color = if (isAccessibilityEnabled) Color(0xFF00E676) else Color(0xFFFFAB40),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "A floating 🔒 button that appears right over your keyboard when you type in Messenger, WhatsApp, or any app. Tap it once to encode your typed text into innocent cover words!",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted,
                    lineHeight = 17.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open settings: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAccessibilityEnabled) StealthSurfaceVariant else StealthPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Accessibility,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) StealthPrimary else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAccessibilityEnabled) "Accessibility (ON)" else "Enable Assistant",
                            color = if (isAccessibilityEnabled) StealthPrimary else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open overlay settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Allow Overlay", fontSize = 12.sp, color = StealthPrimary)
                        }
                    }
                }
            }
        }

        // Section 2: Feather Fling Virtual Keyboard (IME)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = StealthSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Feather Fling Keyboard",
                            fontWeight = FontWeight.Bold,
                            color = StealthOnSurface,
                            fontSize = 15.sp
                        )
                    }

                    Surface(
                        color = if (isKeyboardEnabled) Color(0xFF00C853).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isKeyboardEnabled) "Enabled" else "Not Enabled",
                            color = if (isKeyboardEnabled) Color(0xFF00E676) else Color(0xFFFFAB40),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "A full virtual keyboard equipped with a dedicated 🔒 [Fling & Lock] action bar. Type directly in Messenger and tap 🔒 on the keyboard toolbar to replace your message with cover text.",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted,
                    lineHeight = 17.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open keyboard settings", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StealthSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enable Keyboard", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = StealthSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Input", fontSize = 12.sp, color = StealthSecondary)
                    }
                }
            }
        }

        // Section 3: Lock Button Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚙️ Lock Button Default Preferences",
                    fontWeight = FontWeight.Bold,
                    color = StealthPrimary,
                    fontSize = 15.sp
                )

                Text(
                    text = "Steganography Mode to use when 🔒 is tapped:",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted
                )

                val modes = listOf(
                    StegoMode.WORDLIST_GENERATOR to "Word-List Mnemonic (Recommended)",
                    StegoMode.CONTRACTION_SYNONYM to "Synonym Substitution",
                    StegoMode.TOKEN_CHAFFING to "Chaffed Order/Tracking Token"
                )

                modes.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedMode = mode
                                prefs.defaultMode = mode
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMode == mode,
                            onClick = {
                                selectedMode = mode
                                prefs.defaultMode = mode
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = StealthPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, fontSize = 13.sp, color = StealthOnSurface)
                    }
                }

                OutlinedTextField(
                    value = passphrase,
                    onValueChange = {
                        passphrase = it
                        prefs.defaultPassphrase = it
                    },
                    label = { Text("Optional AES-256 Passphrase for Lock Button") },
                    placeholder = { Text("Leave blank for plain steganography") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StealthPrimary,
                        unfocusedBorderColor = StealthCardBorder
                    ),
                    singleLine = true
                )
            }
        }

        // Section 4: Interactive Live Messenger Sandbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💬 Interactive Messenger Simulator",
                        fontWeight = FontWeight.Bold,
                        color = StealthPrimary,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Test the exact workflow: Type your secret message in the box below, tap the 🔒 Lock button to fling it into carrier-proof cover text, then tap Send to simulate Messenger dispatch!",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted,
                    lineHeight = 16.sp
                )

                // Mock Chat Window
                if (sandboxChatHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F111D), RoundedCornerShape(10.dp))
                            .border(1.dp, StealthCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sandboxChatHistory.forEach { (_, msg) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    color = Color(0xFF262C49),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = msg,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Sent via Messenger Simulator",
                                            fontSize = 9.sp,
                                            color = StealthOnSurfaceMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Messenger Text Box with Lock Button on right
                OutlinedTextField(
                    value = sandboxMessage,
                    onValueChange = { sandboxMessage = it },
                    label = { Text("Messenger Text Box") },
                    placeholder = { Text("Type your secret message here...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (sandboxMessage.isBlank()) {
                                    Toast.makeText(context, "Type a secret message first!", Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }
                                try {
                                    val res = StegoManager.encode(
                                        secretText = sandboxMessage,
                                        mode = selectedMode,
                                        passphrase = passphrase.takeIf { it.isNotBlank() }
                                    )
                                    sandboxMessage = res.encodedText
                                    Toast.makeText(context, "🪶 Flung & Locked! Message replaced.", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Fling & Lock",
                                tint = StealthPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StealthPrimary,
                        unfocusedBorderColor = StealthCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (sandboxMessage.isNotBlank()) {
                                sandboxChatHistory = sandboxChatHistory + (true to sandboxMessage)
                                sandboxMessage = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send in Messenger", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val lastMsg = sandboxChatHistory.lastOrNull()?.second
                            if (lastMsg == null) {
                                Toast.makeText(context, "Send a message first to decode!", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val dec = StegoManager.decodeAuto(lastMsg, passphrase.takeIf { it.isNotBlank() })
                            if (dec.success) {
                                lastDecodedSecret = dec.secretMessage
                            } else {
                                Toast.makeText(context, "Could not decode: ${dec.errorMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = StealthSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decode Last", color = StealthSecondary, fontSize = 12.sp)
                    }
                }

                if (lastDecodedSecret != null) {
                    Surface(
                        color = Color(0xFF00E676).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔓 Decoded Secret:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lastDecodedSecret!!,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}

private fun isInputMethodEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val enabledMethods = imm.enabledInputMethodList
    return enabledMethods.any { it.packageName == context.packageName }
}
