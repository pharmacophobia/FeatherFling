package com.stealthsms.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.stealthsms.app.R
import com.stealthsms.app.pref.FeatherFlingPreferences
import com.stealthsms.app.security.BiometricAuthHelper

@Composable
fun GameDisguiseScreen(
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: context as? Activity
    val prefs = remember { FeatherFlingPreferences(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    fun exitGame() {
        activity?.finishAffinity()
    }

    fun handleBirdTap() {
        if (prefs.isBiometricsEnabled && context is FragmentActivity) {
            BiometricAuthHelper.promptBiometric(
                activity = context,
                title = "Feather Fling Security",
                subtitle = "Scan biometric or enter PIN to launch",
                onSuccess = {
                    onUnlock()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            onUnlock()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF161B22),
                        Color(0xFF0A0C10)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // SECRET DISGUISED BIRD LOGO IN THE TOP-RIGHT CORNER
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bird_avatar),
                contentDescription = "Game Mascot",
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF30363D), CircleShape)
                    .clickable {
                        handleBirdTap()
                    }
            )
        }

        // CENTER GAME MENU
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // GAME TITLE & LOGO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF1F6FEB).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F6FEB).copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "🎮 ARCADE RETRO EDITION",
                        color = Color(0xFF58A6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "FEATHER FLING",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF0F6FC),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "SKY QUEST • ADVENTURE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            // MENU OPTIONS (ALL OPTIONS CLOSE THE APP FOR CAMOUFLAGE)
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GameMenuButton(
                    label = "▶  START GAME",
                    isPrimary = true,
                    pulse = pulseScale,
                    onClick = { exitGame() }
                )

                GameMenuButton(
                    label = "🏆  HIGH SCORES",
                    isPrimary = false,
                    onClick = { exitGame() }
                )

                GameMenuButton(
                    label = "⚙  SETTINGS",
                    isPrimary = false,
                    onClick = { exitGame() }
                )

                GameMenuButton(
                    label = "🚪  EXIT TO SYSTEM",
                    isPrimary = false,
                    onClick = { exitGame() }
                )
            }

            // FOOTER COPYRIGHT & FAKE HIGH SCORE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TOP SCORE: 94,820 PTS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF388BFD),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "© 2026 Feather Interactive Labs • v1.2",
                    fontSize = 10.sp,
                    color = Color(0xFF484F58)
                )
            }
        }
    }
}

@Composable
private fun GameMenuButton(
    label: String,
    isPrimary: Boolean,
    pulse: Float = 1.0f,
    onClick: () -> Unit
) {
    val bgModifier = if (isPrimary) {
        Modifier
            .scale(pulse)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF238636), Color(0xFF2EA043))
                ),
                shape = RoundedCornerShape(12.dp)
            )
    } else {
        Modifier
            .background(Color(0xFF21262D), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF30363D), shape = RoundedCornerShape(12.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(bgModifier)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) Color.White else Color(0xFFC9D1D9),
            letterSpacing = 1.sp
        )
    }
}
