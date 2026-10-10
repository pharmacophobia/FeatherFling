package com.stealthsms.app.sms

data class SmsConversation(
    val threadId: Long,
    val address: String,
    val contactName: String?,
    val snippet: String,
    val date: Long,
    val unreadCount: Int = 0
) {
    val displayName: String
        get() = contactName?.takeIf { it.isNotBlank() } ?: address.takeIf { it.isNotBlank() } ?: "Unknown"

    val avatarInitial: String
        get() = displayName.firstOrNull()?.uppercase() ?: "?"
}

data class SmsMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isIncoming: Boolean,
    val decodedSecret: String? = null,
    val hasPossibleSecret: Boolean = false
)
