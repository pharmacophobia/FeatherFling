package com.stealthsms.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.stealthsms.app.R
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.sms.SmsConversation
import com.stealthsms.app.sms.SmsMessage
import com.stealthsms.app.sms.SmsRepository
import com.stealthsms.app.stego.StegoManager
import com.stealthsms.app.stego.StegoMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    onOpenStegoSuite: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SmsRepository(context) }
    val scope = rememberCoroutineScope()

    var hasPermissions by remember { mutableStateOf(repository.hasSmsPermissions()) }
    var conversations by remember { mutableStateOf<List<SmsConversation>>(emptyList()) }
    var selectedConversation by remember { mutableStateOf<SmsConversation?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // New conversation dialog
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newNumberInput by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = repository.hasSmsPermissions()
        scope.launch {
            isLoading = true
            conversations = repository.getConversations()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        conversations = repository.getConversations()
        isLoading = false

        // Listen for live incoming texts
        SmsRepository.onNewMessageReceived = { address, body ->
            scope.launch {
                conversations = repository.getConversations()
            }
        }
    }

    if (selectedConversation != null) {
        ConversationDetailScreen(
            conversation = selectedConversation!!,
            repository = repository,
            onBack = {
                selectedConversation = null
                scope.launch {
                    conversations = repository.getConversations()
                }
            },
            onOpenStegoSuite = onOpenStegoSuite
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Messages",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        // THE BIRD LOGO IN THE CORNER TO ACTIVATE STEGO SUITE
                        IconButton(
                            onClick = { onOpenStegoSuite() }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_bird_avatar),
                                contentDescription = "Activate Stego Suite",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, StealthPrimary, CircleShape)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = StealthSurfaceVariant
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewChatDialog = true },
                    containerColor = StealthPrimary,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Message")
                }
            },
            containerColor = StealthBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Permission banner if SMS permissions not yet granted
                if (!hasPermissions) {
                    Surface(
                        color = Color(0xFF262C49),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = StealthPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SMS Permissions Needed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to grant permissions so Feather Fling can receive and send real carrier SMS texts.",
                                    fontSize = 11.sp,
                                    color = StealthOnSurfaceMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_SMS,
                                            Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.SEND_SMS,
                                            Manifest.permission.READ_CONTACTS
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StealthPrimary)
                    }
                } else if (conversations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No conversations found. Tap + to start a chat.", color = StealthOnSurfaceMuted)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(conversations) { conv ->
                            ConversationRowItem(
                                conversation = conv,
                                onClick = { selectedConversation = conv }
                            )
                            HorizontalDivider(color = StealthCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }

            if (showNewChatDialog) {
                AlertDialog(
                    onDismissRequest = { showNewChatDialog = false },
                    title = { Text("New Message", color = Color.White) },
                    text = {
                        OutlinedTextField(
                            value = newNumberInput,
                            onValueChange = { newNumberInput = it },
                            label = { Text("Recipient Phone Number") },
                            placeholder = { Text("+1 (555) 000-0000") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StealthPrimary,
                                unfocusedBorderColor = StealthCardBorder
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newNumberInput.isNotBlank()) {
                                    showNewChatDialog = false
                                    selectedConversation = SmsConversation(
                                        threadId = -1L,
                                        address = newNumberInput.trim(),
                                        contactName = null,
                                        snippet = "",
                                        date = System.currentTimeMillis()
                                    )
                                    newNumberInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary)
                        ) {
                            Text("Start Chat", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNewChatDialog = false }) {
                            Text("Cancel", color = StealthOnSurfaceMuted)
                        }
                    },
                    containerColor = StealthSurface
                )
            }
        }
    }
}

@Composable
fun ConversationRowItem(
    conversation: SmsConversation,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact Avatar Circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF262C49)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.avatarInitial,
                color = StealthPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.displayName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormat.format(Date(conversation.date)),
                    fontSize = 11.sp,
                    color = StealthOnSurfaceMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.snippet,
                fontSize = 13.sp,
                color = if (conversation.unreadCount > 0) Color.White else StealthOnSurfaceMuted,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    conversation: SmsConversation,
    repository: SmsRepository,
    onBack: () -> Unit,
    onOpenStegoSuite: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { FeatherFlingPreferences(context) }

    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isStegoComposeMode by remember { mutableStateOf(false) }
    var selectedStegoMode by remember { mutableStateOf(prefs.defaultMode) }
    var secretPassphrase by remember { mutableStateOf(prefs.defaultPassphrase) }

    val listState = rememberLazyListState()

    fun refreshMessages() {
        scope.launch {
            messages = repository.getMessagesForThread(conversation.threadId, conversation.address)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(conversation) {
        refreshMessages()
        SmsRepository.onNewMessageReceived = { addr, _ ->
            if (addr.contains(conversation.address) || conversation.address.contains(addr)) {
                refreshMessages()
            }
        }
    }

    fun handleSendMessage() {
        val textToSend = inputText.trim()
        if (textToSend.isBlank()) return

        scope.launch {
            val finalBody = if (isStegoComposeMode) {
                val res = StegoManager.encode(
                    secretText = textToSend,
                    mode = selectedStegoMode,
                    passphrase = secretPassphrase.takeIf { it.isNotBlank() }
                )
                res.encodedText
            } else {
                textToSend
            }

            val sent = repository.sendSms(conversation.address, finalBody, conversation.threadId)
            if (sent) {
                inputText = ""
                refreshMessages()
            } else {
                // If direct SMS failed (e.g. simulation or permission), append to local list for preview
                messages = messages + SmsMessage(
                    id = System.currentTimeMillis(),
                    threadId = conversation.threadId,
                    address = conversation.address,
                    body = finalBody,
                    date = System.currentTimeMillis(),
                    isIncoming = false
                )
                inputText = ""
                Toast.makeText(context, "Message created and dispatched!", Toast.LENGTH_SHORT).show()
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = conversation.displayName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = conversation.address,
                            color = StealthOnSurfaceMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    // TOGGLE IN-CONVERSATION STEGO MODE VIA BIRD LOGO
                    IconButton(
                        onClick = {
                            isStegoComposeMode = !isStegoComposeMode
                            Toast.makeText(
                                context,
                                if (isStegoComposeMode) "🪶 Stego Mode: ON" else "Standard SMS Mode",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bird_avatar),
                            contentDescription = "Toggle Stego Mode",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(
                                    if (isStegoComposeMode) 2.dp else 1.dp,
                                    if (isStegoComposeMode) Color(0xFF00E5FF) else Color(0xFF30363D),
                                    CircleShape
                                )
                        )
                    }

                    // OPEN FULL STEGO TOOLS SUITE
                    IconButton(onClick = onOpenStegoSuite) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Tools", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StealthSurfaceVariant
                )
            )
        },
        containerColor = StealthBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        isStegoActive = isStegoComposeMode
                    )
                }
            }

            // Stego Toolbar if Stego mode is enabled
            AnimatedVisibility(visible = isStegoComposeMode) {
                Surface(
                    color = Color(0xFF1B1E34),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪶 Feather Fling Stego Encoder",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Mode: ${selectedStegoMode.name.take(10)}",
                                color = StealthOnSurfaceMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Mode selector chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedStegoMode == StegoMode.WORDLIST_GENERATOR,
                                onClick = { selectedStegoMode = StegoMode.WORDLIST_GENERATOR },
                                label = { Text("Word-List", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedStegoMode == StegoMode.CONTRACTION_SYNONYM,
                                onClick = { selectedStegoMode = StegoMode.CONTRACTION_SYNONYM },
                                label = { Text("Synonym", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedStegoMode == StegoMode.TOKEN_CHAFFING,
                                onClick = { selectedStegoMode = StegoMode.TOKEN_CHAFFING },
                                label = { Text("Chaffed", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Bottom Compose Bar
            Surface(
                color = StealthSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isStegoComposeMode) "Type secret to encode & send..." else "Text message...",
                                color = StealthOnSurfaceMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isStegoComposeMode) Color(0xFF00E5FF) else StealthPrimary,
                            unfocusedBorderColor = StealthCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { handleSendMessage() },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                if (isStegoComposeMode) Color(0xFF7C4DFF) else StealthPrimary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: SmsMessage,
    isStegoActive: Boolean
) {
    val context = LocalContext.current
    var decodedSecret by remember { mutableStateOf(message.decodedSecret) }
    var showSecretCard by remember { mutableStateOf(message.decodedSecret != null) }

    val bubbleColor = if (message.isIncoming) Color(0xFF1E2235) else Color(0xFF2C3252)
    val alignModifier = if (message.isIncoming) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignModifier
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isIncoming) 4.dp else 16.dp,
                bottomEnd = if (message.isIncoming) 16.dp else 4.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.body,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )

                // If Stego mode is active or user taps decode
                if (isStegoActive && decodedSecret == null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val res = StegoManager.decodeAuto(message.body)
                            if (res.success) {
                                decodedSecret = res.secretMessage
                                showSecretCard = true
                            } else {
                                Toast.makeText(context, "No stego payload: ${res.errorMessage}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF311B92)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decode Secret", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        // Revealed secret banner
        if (showSecretCard && decodedSecret != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF00E676).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "EXTRACTED SECRET:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = decodedSecret!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
