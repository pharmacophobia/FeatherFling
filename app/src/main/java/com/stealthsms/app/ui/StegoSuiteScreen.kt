package com.stealthsms.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stealthsms.app.MobileAppUpdater
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.security.BiometricAuthHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StegoSuiteScreen(
    initialIncomingText: String = "",
    onBackToMessenger: () -> Unit,
    onRelockApp: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (initialIncomingText.isNotBlank()) 1 else 0) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackToMessenger) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Messenger", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "🪶 Stego Suite & Vault",
                            fontWeight = FontWeight.Bold,
                            color = StealthPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Linguistic Steganography Tools",
                            color = StealthOnSurfaceMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onBackToMessenger) {
                        Text("Messenger", color = StealthPrimary, fontWeight = FontWeight.Bold)
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
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
                    label = { Text("Security") }
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
                4 -> SecuritySettingsTab(onRelockApp = onRelockApp)
            }
        }
    }
}

@Composable
fun SecuritySettingsTab(
    onRelockApp: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { FeatherFlingPreferences(context) }
    val scope = rememberCoroutineScope()

    var gameDisguise by remember { mutableStateOf(prefs.isGameMenuEnabled) }
    var biometricsRequired by remember { mutableStateOf(prefs.isBiometricsEnabled) }
    val isBiometricsSupported = remember { BiometricAuthHelper.isBiometricAvailable(context) }

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.stealthsms.app.AppUpdateInfo?>(null) }
    val updater = remember { MobileAppUpdater(context, "pharmacophobia/FeatherFling") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🛡️ App Security & Stealth Disguise",
            fontWeight = FontWeight.Bold,
            color = StealthPrimary,
            fontSize = 17.sp
        )

        // Game Camouflage Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎮 Fake Game Startup Screen",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Launches into a realistic arcade game menu ('Feather Fling: Sky Quest'). All options close the app, while tapping the discreet bird mascot in the corner opens the messenger.",
                            fontSize = 12.sp,
                            color = StealthOnSurfaceMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = gameDisguise,
                        onCheckedChange = {
                            gameDisguise = it
                            prefs.isGameMenuEnabled = it
                            Toast.makeText(context, if (it) "Game disguise enabled!" else "Game disguise disabled", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StealthPrimary
                        )
                    )
                }

                HorizontalDivider(color = StealthCardBorder)

                // Biometrics Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔐 Biometric Lock on Bird Mascot",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isBiometricsSupported) {
                                "Requires fingerprint, face, or screen lock authentication when tapping the bird logo in the game screen to unlock the real app."
                            } else {
                                "Biometric authentication is not configured on this device."
                            },
                            fontSize = 12.sp,
                            color = StealthOnSurfaceMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = biometricsRequired,
                        enabled = isBiometricsSupported,
                        onCheckedChange = {
                            biometricsRequired = it
                            prefs.isBiometricsEnabled = it
                            Toast.makeText(context, if (it) "Biometrics required on bird tap!" else "Biometrics disabled", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StealthPrimary
                        )
                    )
                }
            }
        }

        // Lock App Now Button
        Button(
            onClick = { onRelockApp() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lock App Now (Return to Game Screen)", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // In-app updater card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StealthSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StealthCardBorder)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📦 Application Version & Updates",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "Feather Fling v1.2.0 • Linguistic Carrier Stego",
                    fontSize = 12.sp,
                    color = StealthOnSurfaceMuted
                )
                OutlinedButton(
                    onClick = {
                        if (!isCheckingUpdate) {
                            isCheckingUpdate = true
                            scope.launch {
                                try {
                                    val info = updater.checkForUpdates()
                                    updateInfo = info
                                    showUpdateDialog = true
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Update check failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isCheckingUpdate = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = StealthPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isCheckingUpdate) "Checking Updates..." else "Check for Updates",
                        color = StealthPrimary
                    )
                }
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        com.stealthsms.app.InAppUpdateDialog(
            appName = "Feather Fling",
            updateInfo = updateInfo!!,
            onDismiss = { showUpdateDialog = false },
            onInstallUpdate = {
                showUpdateDialog = false
                updater.downloadAndInstallApk(updateInfo!!)
            }
        )
    }
}
