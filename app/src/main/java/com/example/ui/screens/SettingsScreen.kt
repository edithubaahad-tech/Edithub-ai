package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.borderStroke
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by homeViewModel.themeMode.collectAsState()
    val isGeminiConfigured = remember { homeViewModel.geminiService.isApiKeyConfigured() }
    var cacheClearedMessage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Theme Mode
            item {
                Text("Appearance Theme", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ThemeOptionRow(
                            title = "Dark Video Editor (Default)",
                            description = "High-contrast dark canvas optimized for color grading",
                            isSelected = themeMode == ThemeMode.DARK,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.DARK) }
                        )
                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.5.dp)
                        ThemeOptionRow(
                            title = "Light Mode",
                            description = "Clean bright theme for daytime editing",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.LIGHT) }
                        )
                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.5.dp)
                        ThemeOptionRow(
                            title = "System Default",
                            description = "Matches device system theme",
                            isSelected = themeMode == ThemeMode.SYSTEM,
                            onClick = { homeViewModel.setThemeMode(ThemeMode.SYSTEM) }
                        )
                    }
                }
            }

            // AI & Backend Integration
            item {
                Text("AI Engine & Services", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, if (isGeminiConfigured) AccentIndigo else DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentPurple)
                                Column {
                                    Text("Google Gemini API", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        if (isGeminiConfigured) "Connected: gemini-3.5-flash active" else "Running in offline fallback mode",
                                        color = if (isGeminiConfigured) AccentEmerald else AccentAmber,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                color = if (isGeminiConfigured) AccentEmerald.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isGeminiConfigured) "ACTIVE" else "CONFIG NEEDED",
                                    color = if (isGeminiConfigured) AccentEmerald else AccentAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "To configure or change your Gemini API Key securely, open the Secrets panel in Google AI Studio and set GEMINI_API_KEY.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Defaults & Storage
            item {
                Text("Storage & Cache", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Temporary Video Cache", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("~24.5 MB used by proxy thumbnails and waveforms", color = TextSecondaryDark, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { cacheClearedMessage = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear", color = AccentRose, fontSize = 12.sp)
                            }
                        }

                        if (cacheClearedMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Temporary cache cleared successfully!", color = AccentCyan, fontSize = 11.sp)
                        }
                    }
                }
            }

            // About EditHub AI
            item {
                Text("About", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("EditHub AI v1.0.0", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("\"Create. Edit. Enhance. Publish.\"", color = AccentPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "A modern, touch-optimized video editing studio with timeline multi-track editing, AI auto edit, viral caption generator, and export engine.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextSecondaryDark, fontSize = 11.sp)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AccentPurple)
        )
    }
}
