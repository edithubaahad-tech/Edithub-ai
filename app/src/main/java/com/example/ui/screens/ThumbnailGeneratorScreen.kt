package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiAiService
import com.example.data.models.ThumbnailConcept
import com.example.ui.components.borderStroke
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailGeneratorScreen(
    geminiService: GeminiAiService,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var videoTitle by remember { mutableStateOf("10 AI Video Editing Secrets") }
    var videoDescription by remember { mutableStateOf("How creators are saving 10 hours a week using EditHub AI tools") }
    var isGenerating by remember { mutableStateOf(false) }
    var thumbnailConcept by remember {
        mutableStateOf<ThumbnailConcept?>(
            ThumbnailConcept(
                headlineText = "10x FASTER ⚡",
                subjectDescription = "Creator with expressive surprised look, holding camera",
                backgroundStyle = "Deep dark neon cyber gradient with glowing cyan backdrop",
                compositionTip = "Subject framed on left third; headline in yellow on top-right",
                suggestedPalette = listOf("#FBBF24", "#8B5CF6", "#3B82F6", "#EC4899")
            )
        )
    }

    var customHeadline by remember { mutableStateOf("10x FASTER ⚡") }
    var isSavedNotification by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("AI Thumbnail Studio", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Visual Thumbnail Preview Canvas
            item {
                Text("Thumbnail Live Canvas (16:9)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
                ) {
                    // Gradient Background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), AccentIndigo, Color(0xFF1E1B4B))
                            )
                        )
                        // Glowing focal circles
                        drawCircle(
                            color = AccentPurple.copy(alpha = 0.35f),
                            radius = size.width * 0.45f,
                            center = Offset(size.width * 0.25f, size.height * 0.5f)
                        )
                        drawCircle(
                            color = AccentCyan.copy(alpha = 0.25f),
                            radius = size.width * 0.35f,
                            center = Offset(size.width * 0.85f, size.height * 0.3f)
                        )
                    }

                    // Simulated Subject Silhouette
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp)
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(2.dp, AccentCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                    }

                    // Headline Typography Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFFFBBF24), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = customHeadline,
                            color = Color(0xFFFDE047),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 4K / HD Badge
                    Surface(
                        color = AccentRose,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text("4K ULTRA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Headline Input & Save
            item {
                OutlinedTextField(
                    value = customHeadline,
                    onValueChange = { customHeadline = it },
                    label = { Text("Overlay Text Headline") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isSavedNotification = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Thumbnail", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                if (isSavedNotification) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Thumbnail saved to gallery successfully!", color = AccentCyan, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }

            // AI Generation Form
            item {
                Text("Generate New AI Concept", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = videoTitle,
                    onValueChange = { videoTitle = it },
                    label = { Text("Video Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = videoDescription,
                    onValueChange = { videoDescription = it },
                    label = { Text("Video Description & Hook") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            try {
                                val concept = geminiService.generateThumbnailConcept(videoTitle, videoDescription)
                                thumbnailConcept = concept
                                customHeadline = concept.headlineText
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing CTR Strategy...")
                    } else {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate High-CTR Thumbnail Strategy", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Strategic Insights Card
            thumbnailConcept?.let { concept ->
                item {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("AI Composition Strategy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("👤 Subject: ${concept.subjectDescription}", color = TextSecondaryDark, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🌄 Backdrop: ${concept.backgroundStyle}", color = TextSecondaryDark, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📐 Composition Tip: ${concept.compositionTip}", color = AccentCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
