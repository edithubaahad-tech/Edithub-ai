package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.ai.GeminiAiService
import com.example.data.models.ScriptScene
import com.example.data.models.ScriptToVideoPlan
import com.example.ui.components.borderStroke
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptToVideoScreen(
    geminiService: GeminiAiService,
    onBack: () -> Unit,
    onOpenGeneratedProject: (String, List<ScriptScene>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var scriptInput by remember {
        mutableStateOf("Create an engaging 15-second viral video revealing 3 incredible AI productivity shortcuts that save 10 hours every week.")
    }
    val styles = listOf("Viral Short", "Cinematic", "Tech Explainer", "Vlog", "Product Promo", "Documentary")
    var selectedStyle by remember { mutableStateOf(styles.first()) }
    var durationSeconds by remember { mutableIntStateOf(15) }
    val languages = listOf("English", "Hindi", "Spanish", "French", "German")
    var selectedLanguage by remember { mutableStateOf("English") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedPlan by remember { mutableStateOf<ScriptToVideoPlan?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("AI Script-To-Video", color = Color.White, fontWeight = FontWeight.Bold) },
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
            item {
                Text(
                    text = "Input Video Script or Topic Prompt",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = scriptInput,
                    onValueChange = { scriptInput = it },
                    minLines = 4,
                    maxLines = 8,
                    placeholder = { Text("Enter your idea, script, or blog summary...", color = TextTertiaryDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Target Style", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(styles) { style ->
                        val isSelected = style == selectedStyle
                        Surface(
                            color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                            modifier = Modifier.clickable { selectedStyle = style }
                        ) {
                            Text(
                                text = style,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration: ${durationSeconds}s", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Language: $selectedLanguage", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Slider(
                    value = durationSeconds.toFloat(),
                    onValueChange = { durationSeconds = it.toInt() },
                    valueRange = 10f..60f,
                    steps = 9,
                    colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(languages) { lang ->
                        val isSelected = lang == selectedLanguage
                        Surface(
                            color = if (isSelected) AccentIndigo else DarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedLanguage = lang }
                        ) {
                            Text(
                                text = lang,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (scriptInput.isNotBlank()) {
                            isGenerating = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    val plan = geminiService.generateScriptToVideo(
                                        script = scriptInput,
                                        style = selectedStyle,
                                        durationSeconds = durationSeconds,
                                        language = selectedLanguage
                                    )
                                    generatedPlan = plan
                                } catch (e: Exception) {
                                    errorMessage = "Generation error: ${e.message}"
                                } finally {
                                    isGenerating = false
                                }
                            }
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Drafting Storyboard with Gemini...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Storyboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(errorMessage!!, color = AccentRose, fontSize = 12.sp)
                }
            }

            // Generated Storyboard Result
            generatedPlan?.let { plan ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("${plan.scenes.size} Scenes • ${plan.totalDurationSeconds}s total", color = AccentCyan, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onOpenGeneratedProject(plan.title, plan.scenes, plan.targetStyle)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open in Editor", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(plan.scenes) { scene ->
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Scene ${scene.sceneNumber}: ${scene.description}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(4.dp)) {
                                    Text("${scene.durationSeconds}s", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🎥 Visuals: ${scene.suggestedVisuals}", color = TextSecondaryDark, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🎙️ Narration: \"${scene.voiceoverText}\"", color = Color(0xFF93C5FD), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("💬 Caption: \"${scene.captionText}\"", color = Color(0xFFFDE047), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
