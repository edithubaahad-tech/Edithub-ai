package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.audio.AudioRecordingHelper
import com.example.data.audio.RecordedAudioResult
import com.example.data.audio.SampleAudioLibrary
import com.example.data.audio.SampleAudioTrack
import com.example.data.models.*
import com.example.ui.theme.*

// Active Bottom Sheet Enum
enum class EditorSheetType {
    BASIC_TOOLS,
    FILTERS,
    COLOR_ADJUST,
    TEXT_TOOL,
    CAPTIONS,
    AI_AUTO_EDIT,
    AI_ENHANCE,
    BG_REMOVAL,
    AUDIO,
    VOICEOVER,
    TRANSITIONS,
    EFFECTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorBottomSheetContainer(
    sheetType: EditorSheetType,
    selectedClip: TimelineClip?,
    onDismiss: () -> Unit,
    // Basic tools callbacks
    aspectRatio: AspectRatio,
    onSelectAspectRatio: (AspectRatio) -> Unit,
    onRotateClip: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onFreezeFrame: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    // Filter callbacks
    filterType: VideoFilterType,
    filterIntensity: Float,
    onSelectFilter: (VideoFilterType) -> Unit,
    onFilterIntensityChange: (Float) -> Unit,
    // Color callbacks
    colorAdjustments: ColorAdjustments,
    onColorAdjustmentsChange: (ColorAdjustments) -> Unit,
    onResetColors: () -> Unit,
    // Text tool callbacks
    onAddTextOverlay: (TextOverlay) -> Unit,
    // Captions callbacks
    captions: List<CaptionItem>,
    onGenerateCaptions: (CaptionStyle) -> Unit,
    isGeneratingCaptions: Boolean,
    onDeleteCaption: (String) -> Unit,
    // AI Auto edit callbacks
    onTriggerAutoEdit: (String) -> Unit,
    autoEditSuggestion: AIAutoEditSuggestion?,
    isAnalyzingAutoEdit: Boolean,
    onApplyAutoEdit: () -> Unit,
    // AI Enhance callbacks
    onApplyEnhance: (String) -> Unit,
    // BG Removal callbacks
    onApplyBgRemoval: (String) -> Unit,
    // Audio callbacks
    onAddAudioTrack: (SampleAudioTrack) -> Unit,
    onUploadAudioClick: () -> Unit,
    audioClips: List<AudioClip> = emptyList(),
    onUpdateAudioClipVolume: (String, Float) -> Unit = { _, _ -> },
    onUpdateAudioClipFades: (String, Long, Long) -> Unit = { _, _, _ -> },
    onToggleAudioClipMute: (String) -> Unit = {},
    onDeleteAudioClip: (String) -> Unit = {},
    onToggleMuteAllClips: () -> Unit = {},
    // Voiceover callbacks
    onSpeakVoiceover: (String, String, Float, Float) -> Unit,
    onAddVoiceoverClip: (String, String, Long) -> Unit,
    isSpeakingVoiceover: Boolean,
    audioRecordingHelper: AudioRecordingHelper? = null,
    onAddRecordedVoiceover: (RecordedAudioResult) -> Unit = {},
    // Transition & Effect callbacks
    transitionType: TransitionType,
    transitionDurationMs: Long,
    onSelectTransition: (TransitionType, Long) -> Unit,
    effectType: VideoEffectType,
    effectIntensity: Float,
    onSelectEffect: (VideoEffectType, Float) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DarkSurfaceBorder, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            when (sheetType) {
                EditorSheetType.BASIC_TOOLS -> BasicToolsSheetContent(
                    selectedClip = selectedClip,
                    aspectRatio = aspectRatio,
                    onSelectAspectRatio = onSelectAspectRatio,
                    onRotateClip = onRotateClip,
                    onFlipHorizontal = onFlipHorizontal,
                    onFlipVertical = onFlipVertical,
                    onSpeedChange = onSpeedChange,
                    onVolumeChange = onVolumeChange,
                    onToggleMute = onToggleMute,
                    onFreezeFrame = onFreezeFrame,
                    onDuplicateClip = onDuplicateClip,
                    onDeleteClip = onDeleteClip,
                    onToggleMuteAllClips = onToggleMuteAllClips
                )
                EditorSheetType.FILTERS -> FiltersSheetContent(
                    selectedFilter = filterType,
                    intensity = filterIntensity,
                    onSelectFilter = onSelectFilter,
                    onIntensityChange = onFilterIntensityChange
                )
                EditorSheetType.COLOR_ADJUST -> ColorAdjustmentSheetContent(
                    adj = colorAdjustments,
                    onChange = onColorAdjustmentsChange,
                    onReset = onResetColors
                )
                EditorSheetType.TEXT_TOOL -> TextToolSheetContent(
                    onAddText = { overlay ->
                        onAddTextOverlay(overlay)
                        onDismiss()
                    }
                )
                EditorSheetType.CAPTIONS -> CaptionsSheetContent(
                    captions = captions,
                    onGenerate = onGenerateCaptions,
                    isGenerating = isGeneratingCaptions,
                    onDelete = onDeleteCaption
                )
                EditorSheetType.AI_AUTO_EDIT -> AIAutoEditSheetContent(
                    onAnalyze = onTriggerAutoEdit,
                    suggestion = autoEditSuggestion,
                    isAnalyzing = isAnalyzingAutoEdit,
                    onApply = {
                        onApplyAutoEdit()
                        onDismiss()
                    },
                    onCancel = onDismiss
                )
                EditorSheetType.AI_ENHANCE -> AIEnhanceSheetContent(onApplyEnhance = onApplyEnhance)
                EditorSheetType.BG_REMOVAL -> BgRemovalSheetContent(onApplyBgRemoval = onApplyBgRemoval)
                EditorSheetType.AUDIO -> AudioSheetContent(
                    audioClips = audioClips,
                    onAddSampleTrack = onAddAudioTrack,
                    onUploadAudio = onUploadAudioClick,
                    onUpdateVolume = onUpdateAudioClipVolume,
                    onUpdateFades = onUpdateAudioClipFades,
                    onToggleMuteTrack = onToggleAudioClipMute,
                    onDeleteTrack = onDeleteAudioClip,
                    onToggleMuteAllClips = onToggleMuteAllClips
                )
                EditorSheetType.VOICEOVER -> VoiceoverSheetContent(
                    onSpeak = onSpeakVoiceover,
                    onAddClip = { text, lang, dur ->
                        onAddVoiceoverClip(text, lang, dur)
                        onDismiss()
                    },
                    isSpeaking = isSpeakingVoiceover,
                    audioRecordingHelper = audioRecordingHelper,
                    onAddRecordedVoiceover = { result ->
                        onAddRecordedVoiceover(result)
                        onDismiss()
                    }
                )
                EditorSheetType.TRANSITIONS -> TransitionsSheetContent(
                    currentType = transitionType,
                    durationMs = transitionDurationMs,
                    onSelect = onSelectTransition
                )
                EditorSheetType.EFFECTS -> EffectsSheetContent(
                    currentEffect = effectType,
                    intensity = effectIntensity,
                    onSelect = onSelectEffect
                )
            }
        }
    }
}

// ---------------- BASIC TOOLS SHEET ----------------
@Composable
private fun BasicToolsSheetContent(
    selectedClip: TimelineClip?,
    aspectRatio: AspectRatio,
    onSelectAspectRatio: (AspectRatio) -> Unit,
    onRotateClip: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onFreezeFrame: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onToggleMuteAllClips: () -> Unit = {}
) {
    var speed by remember(selectedClip) { mutableStateOf(selectedClip?.speed ?: 1.0f) }
    var volume by remember(selectedClip) { mutableStateOf(selectedClip?.volume ?: 1.0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Editing Tools & Ratio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Aspect Ratio Presets", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AspectRatio.entries) { ratio ->
                val isSelected = ratio == aspectRatio
                Surface(
                    color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                    modifier = Modifier.clickable { onSelectAspectRatio(ratio) }
                ) {
                    Text(
                        text = ratio.label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transform buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ToolIconButton(icon = Icons.Default.RotateRight, label = "Rotate 90°", onClick = onRotateClip)
            ToolIconButton(icon = Icons.Default.Flip, label = "Flip H", onClick = onFlipHorizontal)
            ToolIconButton(icon = Icons.Default.SwapVert, label = "Flip V", onClick = onFlipVertical)
            ToolIconButton(icon = Icons.Default.AcUnit, label = "Freeze", onClick = onFreezeFrame)
            ToolIconButton(
                icon = if (selectedClip?.isMuted == true) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                label = if (selectedClip?.isMuted == true) "Unmute" else "Mute",
                onClick = onToggleMute
            )
            ToolIconButton(
                icon = Icons.Default.VolumeMute,
                label = "Mute All",
                onClick = onToggleMuteAllClips
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Speed", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("${String.format("%.2f", speed)}x", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = speed,
            onValueChange = {
                speed = it
                onSpeedChange(it)
            },
            valueRange = 0.25f..3.0f,
            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
        )

        // Volume Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Volume", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("${(volume * 100).toInt()}%", color = AccentPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                onVolumeChange(it)
            },
            valueRange = 0f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
        )
    }
}

// ---------------- FILTERS SHEET ----------------
@Composable
private fun FiltersSheetContent(
    selectedFilter: VideoFilterType,
    intensity: Float,
    onSelectFilter: (VideoFilterType) -> Unit,
    onIntensityChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cinematic Filters", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${intensity.toInt()}%", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = intensity,
            onValueChange = onIntensityChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(VideoFilterType.entries) { filter ->
                val isSelected = filter == selectedFilter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelectFilter(filter) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (filter) {
                                    VideoFilterType.CINEMATIC -> Color(0xFF1E3A8A)
                                    VideoFilterType.WARM -> Color(0xFFD97706)
                                    VideoFilterType.COOL -> Color(0xFF0284C7)
                                    VideoFilterType.VINTAGE -> Color(0xFF78350F)
                                    VideoFilterType.BLACK_WHITE -> Color(0xFF334155)
                                    VideoFilterType.HDR -> Color(0xFF7C3AED)
                                    VideoFilterType.MOODY -> Color(0xFF0F766E)
                                    VideoFilterType.VIBRANT -> Color(0xFFDB2777)
                                    else -> DarkSurfaceElevated
                                }
                            )
                            .border(
                                2.dp,
                                if (isSelected) AccentPurple else DarkSurfaceBorder,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = filter.displayName,
                        color = if (isSelected) Color.White else TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------- COLOR ADJUSTMENT SHEET ----------------
@Composable
private fun ColorAdjustmentSheetContent(
    adj: ColorAdjustments,
    onChange: (ColorAdjustments) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color Adjustment", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onReset) {
                Text("Reset All", color = AccentRose, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AdjustSliderItem("Brightness", adj.brightness, -0.5f, 0.5f) { onChange(adj.copy(brightness = it)) }
        AdjustSliderItem("Contrast", adj.contrast, 0.5f, 1.8f) { onChange(adj.copy(contrast = it)) }
        AdjustSliderItem("Saturation", adj.saturation, 0.0f, 2.0f) { onChange(adj.copy(saturation = it)) }
        AdjustSliderItem("Temperature", adj.temperature, -0.5f, 0.5f) { onChange(adj.copy(temperature = it)) }
        AdjustSliderItem("Tint", adj.tint, -0.5f, 0.5f) { onChange(adj.copy(tint = it)) }
        AdjustSliderItem("Vignette", adj.vignette, 0.0f, 1.0f) { onChange(adj.copy(vignette = it)) }
    }
}

@Composable
private fun AdjustSliderItem(name: String, value: Float, min: Float, max: Float, onValChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontSize = 12.sp)
        Text(String.format("%.2f", value), color = TextSecondaryDark, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
    Slider(
        value = value,
        onValueChange = onValChange,
        valueRange = min..max,
        colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
    )
}

// ---------------- TEXT TOOL SHEET ----------------
@Composable
private fun TextToolSheetContent(onAddText: (TextOverlay) -> Unit) {
    var textInput by remember { mutableStateOf("Trending Title 🔥") }
    var fontSize by remember { mutableStateOf(24f) }
    var isBold by remember { mutableStateOf(true) }
    var isItalic by remember { mutableStateOf(false) }
    var selectedAnim by remember { mutableStateOf(TextAnimationType.POP) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Add Text Overlay", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Overlay Text") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = DarkSurfaceBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Font Size: ${fontSize.toInt()}sp", color = Color.White, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isBold,
                    onClick = { isBold = !isBold },
                    label = { Text("Bold", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = isItalic,
                    onClick = { isItalic = !isItalic },
                    label = { Text("Italic") }
                )
            }
        }

        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 14f..48f,
            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Text Animation", color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TextAnimationType.entries) { anim ->
                val isSelected = anim == selectedAnim
                Surface(
                    color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { selectedAnim = anim }
                ) {
                    Text(
                        text = anim.name.replace("_", " "),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    onAddText(
                        TextOverlay(
                            id = "txt_${System.currentTimeMillis()}",
                            text = textInput,
                            fontSizeSp = fontSize,
                            isBold = isBold,
                            isItalic = isItalic,
                            animation = selectedAnim,
                            startMs = 0L,
                            durationMs = 4000L
                        )
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.TextFields, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add to Timeline", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- CAPTIONS / SUBTITLES SHEET ----------------
@Composable
private fun CaptionsSheetContent(
    captions: List<CaptionItem>,
    onGenerate: (CaptionStyle) -> Unit,
    isGenerating: Boolean,
    onDelete: (String) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(CaptionStyle.SOCIAL_VIRAL) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI Auto Captions & Subtitles", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Auto-transcribe speech and overlay animated viral subtitles",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Caption Typography Style", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CaptionStyle.entries) { style ->
                val isSelected = style == selectedStyle
                Surface(
                    color = if (isSelected) AccentAmber else DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = borderStroke(1.dp, if (isSelected) AccentAmber else DarkSurfaceBorder),
                    modifier = Modifier.clickable { selectedStyle = style }
                ) {
                    Text(
                        text = style.name.replace("_", " "),
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onGenerate(selectedStyle) },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Audio with Gemini...")
            } else {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Captions", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (captions.isNotEmpty()) {
            Text("Generated Timeline Captions (${captions.size})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(modifier = Modifier.height(140.dp)) {
                items(captions) { cap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cap.text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${cap.startMs / 1000f}s - ${cap.endMs / 1000f}s",
                                color = TextSecondaryDark,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = { onDelete(cap.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- AI AUTO EDIT SHEET ----------------
@Composable
private fun AIAutoEditSheetContent(
    onAnalyze: (String) -> Unit,
    suggestion: AIAutoEditSuggestion?,
    isAnalyzing: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    val styles = listOf("Viral Short", "Cinematic", "Vlog", "Travel", "Comedy", "Story", "Documentary", "Product")
    var selectedStyle by remember { mutableStateOf(styles.first()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI Auto Edit Director", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Analyzes project footage, recommends optimal pacing, cuts, hooks & transitions without destroying original media.",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Select Video Style Target", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(styles) { s ->
                val isSelected = s == selectedStyle
                Surface(
                    color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { selectedStyle = s }
                ) {
                    Text(
                        text = s,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { onAnalyze(selectedStyle) },
            enabled = !isAnalyzing,
            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Project Clips...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze Project With AI", fontWeight = FontWeight.Bold)
            }
        }

        if (suggestion != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proposed Edit: ${suggestion.styleName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pacing: ${suggestion.pacingDescription}", color = AccentCyan, fontSize = 12.sp)
                    Text("Music Timing: ${suggestion.musicTimingNote}", color = TextSecondaryDark, fontSize = 12.sp)
                    Text("Transition: ${suggestion.suggestedTransition.displayName} | Filter: ${suggestion.suggestedFilter.displayName}", color = TextSecondaryDark, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recommended Cuts:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    suggestion.recommendedCuts.take(3).forEach { cut ->
                        Text("• $cut", color = TextSecondaryDark, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = onApply,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply AI Edit", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- AI ENHANCE SHEET ----------------
@Composable
private fun AIEnhanceSheetContent(onApplyEnhance: (String) -> Unit) {
    val enhanceOptions = listOf(
        "Improve Quality" to Icons.Default.AutoFixHigh,
        "Sharpen" to Icons.Default.Details,
        "Reduce Noise" to Icons.Default.FilterVintage,
        "Improve Lighting" to Icons.Default.LightMode,
        "Stabilize" to Icons.Default.CameraAlt,
        "Upscale" to Icons.Default.Hd,
        "Color Enhance" to Icons.Default.Palette
    )

    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI Video Enhancement", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Client-side matrix enhancement is active. Neural super-resolution requires backend API connection.",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        enhanceOptions.forEach { (name, icon) ->
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onApplyEnhance(name)
                        statusMessage = if (name == "Upscale" || name == "Stabilize") {
                            "$name: Neural backend processing pipeline active."
                        } else {
                            "$name: Client-side enhancement applied successfully!"
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = AccentIndigo.copy(alpha = 0.2f),
                border = borderStroke(1.dp, AccentIndigo),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusMessage!!,
                    color = AccentCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// ---------------- BACKGROUND REMOVAL SHEET ----------------
@Composable
private fun BgRemovalSheetContent(onApplyBgRemoval: (String) -> Unit) {
    val bgOptions = listOf("Transparent", "Green Screen", "Blur Background", "Custom Background Image")
    var selectedOption by remember { mutableStateOf(bgOptions.first()) }
    var statusText by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("AI Background Removal", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Segment subjects and replace background. Neural segmentation models run on-device or connected processing service.",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        bgOptions.forEach { opt ->
            val isSelected = opt == selectedOption
            Surface(
                color = if (isSelected) AccentPurple.copy(alpha = 0.2f) else DarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedOption = opt }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(opt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedOption = opt },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentPurple)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onApplyBgRemoval(selectedOption)
                statusText = "Background processing pipeline initialized for '$selectedOption'."
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Layers, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Process Background Removal", fontWeight = FontWeight.Bold)
        }

        if (statusText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(statusText ?: "", color = AccentCyan, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------- AUDIO SHEET ----------------
@Composable
private fun AudioSheetContent(
    audioClips: List<AudioClip>,
    onAddSampleTrack: (SampleAudioTrack) -> Unit,
    onUploadAudio: () -> Unit,
    onUpdateVolume: (String, Float) -> Unit,
    onUpdateFades: (String, Long, Long) -> Unit,
    onToggleMuteTrack: (String) -> Unit,
    onDeleteTrack: (String) -> Unit,
    onToggleMuteAllClips: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (audioClips.isNotEmpty()) 1 else 0) }
    val tabs = listOf("Music & FX Library", "Project Tracks (${audioClips.size})")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Audio Suite", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = onToggleMuteAllClips,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceElevated, contentColor = AccentAmber),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.VolumeMute, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mute Video", fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = onUploadAudio,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceElevated, contentColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            tabs.forEachIndexed { index, tabName ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentPurple else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Tab 0: Library
            Text("Royalty-Safe EditHub Audio Library", color = TextSecondaryDark, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.height(280.dp)) {
                items(SampleAudioLibrary.tracks) { track ->
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (track.category == "SFX") AccentAmber else AccentIndigo, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (track.category == "SFX") Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(track.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${track.category} • ${track.durationMs / 1000}s" + if (track.bpm > 0) " • ${track.bpm} BPM" else "",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onAddSampleTrack(track) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Use", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Project Audio Tracks Management
            if (audioClips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MusicOff, contentDescription = null, tint = TextTertiaryDark, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Audio Tracks Added", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select a royalty-free track or import audio from storage", color = TextSecondaryDark, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(audioClips, key = { it.id }) { track ->
                        Surface(
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = borderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (track.isVoiceover) Icons.Default.Mic else Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (track.isVoiceover) AccentEmerald else AccentCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(track.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("Duration: ${track.durationMs / 1000f}s", color = TextSecondaryDark, fontSize = 11.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onToggleMuteTrack(track.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                if (track.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                contentDescription = "Mute",
                                                tint = if (track.isMuted) AccentRose else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteTrack(track.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Volume Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Volume", color = TextSecondaryDark, fontSize = 11.sp)
                                    Text("${(track.volume * 100).toInt()}%", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = track.volume,
                                    onValueChange = { onUpdateVolume(track.id, it) },
                                    valueRange = 0f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple),
                                    modifier = Modifier.height(24.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Fade In & Fade Out Sliders
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Fade In", color = TextSecondaryDark, fontSize = 10.sp)
                                            Text("${track.fadeInMs / 1000f}s", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = track.fadeInMs.toFloat(),
                                            onValueChange = { onUpdateFades(track.id, it.toLong(), track.fadeOutMs) },
                                            valueRange = 0f..2000f,
                                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Fade Out", color = TextSecondaryDark, fontSize = 10.sp)
                                            Text("${track.fadeOutMs / 1000f}s", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = track.fadeOutMs.toFloat(),
                                            onValueChange = { onUpdateFades(track.id, track.fadeInMs, it.toLong()) },
                                            valueRange = 0f..2000f,
                                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- VOICEOVER SHEET ----------------
@Composable
private fun VoiceoverSheetContent(
    onSpeak: (String, String, Float, Float) -> Unit,
    onAddClip: (String, String, Long) -> Unit,
    isSpeaking: Boolean,
    audioRecordingHelper: AudioRecordingHelper?,
    onAddRecordedVoiceover: (RecordedAudioResult) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI Speech (TTS)", "Live Mic Record")

    // TTS state
    var script by remember { mutableStateOf("Welcome to EditHub AI, the ultimate modern video editor.") }
    val languages = listOf("English", "Hindi", "Marathi", "Tamil", "Telugu", "Bengali")
    var selectedLang by remember { mutableStateOf("English") }
    var speed by remember { mutableStateOf(1.0f) }
    var pitch by remember { mutableStateOf(1.0f) }

    // Live recording state
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    val isRecording by (audioRecordingHelper?.isRecording?.collectAsState() ?: remember { mutableStateOf(false) })
    val recordingDurationMs by (audioRecordingHelper?.recordingDurationMs?.collectAsState() ?: remember { mutableStateOf(0L) })
    val currentAmplitude by (audioRecordingHelper?.currentAmplitude?.collectAsState() ?: remember { mutableStateOf(0f) })
    var lastRecordedResult by remember { mutableStateOf<RecordedAudioResult?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Voiceover Studio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Add narration with realistic neural TTS voices or record live with your device microphone.",
            color = TextSecondaryDark,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            tabs.forEachIndexed { index, tabName ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentPurple else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // AI TTS Narration
            OutlinedTextField(
                value = script,
                onValueChange = { script = it },
                label = { Text("Narration Script") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Voice Language", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    val isSelected = lang == selectedLang
                    Surface(
                        color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { selectedLang = lang }
                    ) {
                        Text(
                            text = lang,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Speed: ${String.format("%.1f", speed)}x", color = TextSecondaryDark, fontSize = 11.sp)
                    Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.5f..2.0f)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("Pitch: ${String.format("%.1f", pitch)}x", color = TextSecondaryDark, fontSize = 11.sp)
                    Slider(value = pitch, onValueChange = { pitch = it }, valueRange = 0.5f..2.0f)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onSpeak(script, selectedLang, speed, pitch) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSpeaking) "Speaking..." else "Preview Voice")
                }

                Button(
                    onClick = {
                        val durationEst = (script.split(" ").size * 400L).coerceAtLeast(3000L)
                        onAddClip(script, selectedLang, durationEst)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Timeline", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Live Microphone Recording
            if (!hasMicPermission) {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Microphone Permission Required", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Grant permission to record voice-overs directly into your project timeline.", color = TextSecondaryDark, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = borderStroke(1.dp, if (isRecording) AccentRose else DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Live Timer Display
                        val secs = (recordingDurationMs / 1000).toInt()
                        val ms = (recordingDurationMs % 1000) / 100
                        val timerText = if (isRecording) {
                            String.format("%02d:%02d.%d", secs / 60, secs % 60, ms)
                        } else if (lastRecordedResult != null) {
                            val durS = (lastRecordedResult!!.durationMs / 1000f)
                            String.format("Recorded: %.1fs", durS)
                        } else {
                            "Ready to Record"
                        }

                        Text(
                            text = timerText,
                            color = if (isRecording) AccentRose else Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Waveform / Amplitude Bars Simulation
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(40.dp)
                        ) {
                            for (i in 0..15) {
                                val barHeight = if (isRecording) {
                                    val factor = ((i % 5) + 1) * 0.2f
                                    (10.dp + (30.dp * currentAmplitude * factor)).coerceIn(6.dp, 38.dp)
                                } else {
                                    8.dp
                                }
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(barHeight)
                                        .background(
                                            if (isRecording) AccentRose else DarkSurfaceBorder,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Record Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) AccentRose else AccentPurple)
                                .clickable {
                                    if (isRecording) {
                                        val res = audioRecordingHelper?.stopRecording()
                                        lastRecordedResult = res
                                    } else {
                                        lastRecordedResult = null
                                        audioRecordingHelper?.startRecording(context)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop" else "Record",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRecording) "Tap to Stop" else "Tap Mic to Start Recording",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )

                        if (lastRecordedResult != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    onAddRecordedVoiceover(lastRecordedResult!!)
                                    lastRecordedResult = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Recording to Timeline", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TRANSITIONS SHEET ----------------
@Composable
private fun TransitionsSheetContent(
    currentType: TransitionType,
    durationMs: Long,
    onSelect: (TransitionType, Long) -> Unit
) {
    var selected by remember { mutableStateOf(currentType) }
    var dur by remember { mutableStateOf(durationMs.toFloat()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Transitions Library", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Duration", color = Color.White, fontSize = 13.sp)
            Text("${(dur / 1000f)}s", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(value = dur, onValueChange = { dur = it }, valueRange = 200f..2000f)

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(TransitionType.entries) { trans ->
                val isSelected = trans == selected
                Surface(
                    color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                    modifier = Modifier.clickable {
                        selected = trans
                        onSelect(trans, dur.toLong())
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(trans.displayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------------- EFFECTS SHEET ----------------
@Composable
private fun EffectsSheetContent(
    currentEffect: VideoEffectType,
    intensity: Float,
    onSelect: (VideoEffectType, Float) -> Unit
) {
    var selected by remember { mutableStateOf(currentEffect) }
    var intens by remember { mutableStateOf(intensity) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Visual Effects", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${intens.toInt()}%", color = AccentPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Slider(
            value = intens,
            onValueChange = {
                intens = it
                onSelect(selected, it)
            },
            valueRange = 10f..100f
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(VideoEffectType.entries) { eff ->
                val isSelected = eff == selected
                Surface(
                    color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                    modifier = Modifier.clickable {
                        selected = eff
                        onSelect(eff, intens)
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.AutoFixNormal, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(eff.displayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(DarkSurfaceElevated, CircleShape)
                .border(1.dp, DarkSurfaceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextSecondaryDark, fontSize = 10.sp)
    }
}
