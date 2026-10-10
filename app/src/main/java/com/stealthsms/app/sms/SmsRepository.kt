package com.stealthsms.app.sms

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.stealthsms.app.stego.StegoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    // Listener for real-time incoming SMS events
    companion object {
        var onNewMessageReceived: ((address: String, body: String) -> Unit)? = null
    }

    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val sendSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val receiveSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        return readSms && sendSms && receiveSms
    }

    suspend fun getConversations(): List<SmsConversation> = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions()) {
            return@withContext getSampleConversations()
        }

        val conversations = mutableListOf<SmsConversation>()
        val seenThreads = mutableSetOf<Long>()

        try {
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ
            )

            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val threadIdIndex = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val readIndex = it.getColumnIndex(Telephony.Sms.READ)

                while (it.moveToNext()) {
                    val threadId = it.getLong(threadIdIndex)
                    if (threadId in seenThreads) continue
                    seenThreads.add(threadId)

                    val address = it.getString(addressIndex) ?: "Unknown"
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val isRead = it.getInt(readIndex) == 1

                    val contactName = resolveContactName(address)

                    conversations.add(
                        SmsConversation(
                            threadId = threadId,
                            address = address,
                            contactName = contactName,
                            snippet = body,
                            date = date,
                            unreadCount = if (isRead) 0 else 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext getSampleConversations()
        }

        if (conversations.isEmpty()) {
            return@withContext getSampleConversations()
        }

        conversations
    }

    suspend fun getMessagesForThread(threadId: Long, fallbackAddress: String): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions() || threadId < 0) {
            return@withContext getSampleMessages(threadId, fallbackAddress)
        }

        val messages = mutableListOf<SmsMessage>()
        try {
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            )

            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                "${Telephony.Sms.THREAD_ID} = ?",
                arrayOf(threadId.toString()),
                "${Telephony.Sms.DATE} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)

                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val address = it.getString(addressIndex) ?: fallbackAddress
                    val body = it.getString(bodyIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val type = it.getInt(typeIndex)
                    val isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX

                    val testDecode = StegoManager.decodeAuto(body)

                    messages.add(
                        SmsMessage(
                            id = id,
                            threadId = threadId,
                            address = address,
                            body = body,
                            date = date,
                            isIncoming = isIncoming,
                            decodedSecret = if (testDecode.success) testDecode.secretMessage else null,
                            hasPossibleSecret = testDecode.success
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext getSampleMessages(threadId, fallbackAddress)
        }

        if (messages.isEmpty()) {
            return@withContext getSampleMessages(threadId, fallbackAddress)
        }

        messages
    }

    suspend fun sendSms(address: String, body: String, threadId: Long): Boolean = withContext(Dispatchers.IO) {
        if (!hasSmsPermissions()) {
            return@withContext false
        }

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(body)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(address, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(address, null, body, null, null)
            }

            // Persist sent SMS to ContentProvider
            try {
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, address)
                    put(Telephony.Sms.BODY, body)
                    put(Telephony.Sms.DATE, System.currentTimeMillis())
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    if (threadId > 0) {
                        put(Telephony.Sms.THREAD_ID, threadId)
                    }
                }
                context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
            } catch (e: Exception) {
                // Not default SMS app or provider insert restricted
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun resolveContactName(phoneNumber: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(0)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }

    // Realistic sample conversations for immediate testing & demo
    private fun getSampleConversations(): List<SmsConversation> {
        val now = System.currentTimeMillis()
        return listOf(
            SmsConversation(
                threadId = 1L,
                address = "+1 (555) 382-9102",
                contactName = "Sarah Connor",
                snippet = "Project dispatch log: alert balance anchor border brass bronze -- verified.",
                date = now - 1000 * 60 * 12, // 12 mins ago
                unreadCount = 1
            ),
            SmsConversation(
                threadId = 2L,
                address = "+1 (555) 749-2041",
                contactName = "Alex Mercer",
                snippet = "We are ready, so do not hesitate if you require any help.",
                date = now - 1000 * 60 * 60 * 2, // 2 hours ago
                unreadCount = 0
            ),
            SmsConversation(
                threadId = 3L,
                address = "+1 (555) 890-4139",
                contactName = "Courier Tracking",
                snippet = "Your order status has been updated. Reference number: #TRK-4645-4154-4845. Please keep this for your records.",
                date = now - 1000 * 60 * 60 * 24, // 1 day ago
                unreadCount = 0
            ),
            SmsConversation(
                threadId = 4L,
                address = "+1 (555) 923-0182",
                contactName = "David Vance",
                snippet = "Are you available for coffee tomorrow morning?",
                date = now - 1000 * 60 * 60 * 48,
                unreadCount = 0
            )
        )
    }

    private fun getSampleMessages(threadId: Long, address: String): List<SmsMessage> {
        val now = System.currentTimeMillis()
        return when (threadId) {
            1L -> listOf(
                SmsMessage(
                    id = 101L,
                    threadId = 1L,
                    address = address,
                    body = "Hey, did you review the latest courier manifest?",
                    date = now - 1000 * 60 * 30,
                    isIncoming = true
                ),
                SmsMessage(
                    id = 102L,
                    threadId = 1L,
                    address = address,
                    body = "Checking it right now, one moment.",
                    date = now - 1000 * 60 * 25,
                    isIncoming = false
                ),
                SmsMessage(
                    id = 103L,
                    threadId = 1L,
                    address = address,
                    body = "Project dispatch log: alert balance anchor border brass bronze -- verified.",
                    date = now - 1000 * 60 * 12,
                    isIncoming = true,
                    decodedSecret = "Safehouse: Vault 4",
                    hasPossibleSecret = true
                )
            )
            2L -> listOf(
                SmsMessage(
                    id = 201L,
                    threadId = 2L,
                    address = address,
                    body = "What is the update on our departure time?",
                    date = now - 1000 * 60 * 120,
                    isIncoming = false
                ),
                SmsMessage(
                    id = 202L,
                    threadId = 2L,
                    address = address,
                    body = "We are ready, so do not hesitate if you require any help.",
                    date = now - 1000 * 60 * 110,
                    isIncoming = true,
                    decodedSecret = "Go now",
                    hasPossibleSecret = true
                )
            )
            3L -> listOf(
                SmsMessage(
                    id = 301L,
                    threadId = 3L,
                    address = address,
                    body = "Your order status has been updated. Reference number: #TRK-4645-4154-4845. Please keep this for your records.",
                    date = now - 1000 * 60 * 60 * 24,
                    isIncoming = true,
                    decodedSecret = "FEATHER",
                    hasPossibleSecret = true
                )
            )
            else -> listOf(
                SmsMessage(
                    id = 401L,
                    threadId = threadId,
                    address = address,
                    body = "Are you available for coffee tomorrow morning?",
                    date = now - 1000 * 60 * 60 * 48,
                    isIncoming = true
                )
            )
        }
    }
}
