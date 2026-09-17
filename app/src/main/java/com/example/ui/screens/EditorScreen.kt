package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    editorViewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by editorViewModel.project.collectAsState()
    val isPlaying by editorViewModel.isPlaying.collectAsState()
    val currentPlayheadMs by editorViewModel.currentPlayheadMs.collectAsState()
    val selectedClipId by editorViewModel.selectedClipId.collectAsState()
    val zoomFactor by editorViewModel.zoomFactor.collectAsState()
    val showSafeAreas by editorViewModel.showSafeAreas.collectAsState()
    val isFullscreen by editorViewModel.isFullscreen.collectAsState()
    val canUndo by editorViewModel.canUndo.collectAsState()
    val canRedo by editorViewModel.canRedo.collectAsState()

    // AI States
    val isGeneratingCaptions by editorViewModel.isGeneratingCaptions.collectAsState()
    val isAnalyzingAutoEdit by editorViewModel.isAnalyzingAutoEdit.collectAsState()
    val autoEditSuggestion by editorViewModel.autoEditSuggestion.collectAsState()
    val isSpeakingVoiceover by editorViewModel.voiceoverEngine.isSpeaking.collectAsState()

    val context = LocalContext.current

    // Active Bottom Sheet
    var activeSheet by remember { mutableStateOf<EditorSheetType?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(project.name) }

    // Media Picker Launcher (Zero-permission Android Photo Picker for multiple videos & images)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            editorViewModel.importMediaUris(uris, context)
        }
    }

    // Audio file picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            editorViewModel.importAudioUris(uris, context)
        }
    }

    // Selected clip lookup
    val selectedClip = remember(project.clips, selectedClipId) {
        project.clips.find { it.id == selectedClipId } ?: project.clips.firstOrNull()
    }

    // Active clip for current playhead
    val activePlayingClip = remember(project.clips, currentPlayheadMs, selectedClip) {
        editorViewModel.getClipAtTime(currentPlayheadMs)?.first ?: selectedClip
    }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            if (!isFullscreen) {
                EditorTopBar(
                    projectName = project.name,
                    aspectRatio = project.aspectRatio,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onBack = onNavigateBack,
                    onRename = {
                        editedName = project.name
                        showRenameDialog = true
                    },
                    onUndo = { editorViewModel.undo() },
                    onRedo = { editorViewModel.redo() },
                    onSave = { editorViewModel.saveProjectToDatabase() },
                    onExport = onNavigateExport
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Center Player / Preview Canvas
            SafePlayerPreview(
                aspectRatio = project.aspectRatio,
                currentMediaClip = activePlayingClip,
                currentPlayheadMs = currentPlayheadMs,
                totalDurationMs = project.durationMs,
                isPlaying = isPlaying,
                onTogglePlay = { editorViewModel.togglePlay() },
                activeCaptions = project.captions,
                activeTextOverlays = project.textOverlays,
                filterType = project.filterType,
                filterIntensity = project.filterIntensity,
                colorAdjustments = project.colorAdjustments,
                effectType = project.effectType,
                effectIntensity = project.effectIntensity,
                showSafeAreas = showSafeAreas,
                onToggleSafeAreas = { editorViewModel.toggleSafeAreas() },
                isFullscreen = isFullscreen,
                onToggleFullscreen = { editorViewModel.toggleFullscreen() },
                onAddMediaClick = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                exoPlayer = editorViewModel.player,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isFullscreen) 1f else 0.52f)
            )

            if (!isFullscreen) {
                // Secondary Tools Ribbon (Horizontal Scroll of Tool Chips)
                ToolsRibbonBar(
                    onSelectTool = { activeSheet = it }
                )

                // Multi-Track Timeline Scrubber
                TimelineView(
                    clips = project.clips,
                    audioClips = project.audioClips,
                    textOverlays = project.textOverlays,
                    captions = project.captions,
                    selectedClipId = selectedClipId,
                    onSelectClip = { editorViewModel.selectClip(it) },
                    currentPlayheadMs = currentPlayheadMs,
                    onSeekPlayhead = { editorViewModel.seekTo(it) },
                    totalDurationMs = project.durationMs,
                    zoomFactor = zoomFactor,
                    onZoomChange = { editorViewModel.setZoomFactor(it) },
                    onSplitClipAtPlayhead = { editorViewModel.splitClipAtPlayhead() },
                    onTrimSelectedClipStart = { editorViewModel.trimSelectedClipStart(it) },
                    onTrimSelectedClipEnd = { editorViewModel.trimSelectedClipEnd(it) },
                    onDuplicateSelectedClip = { editorViewModel.duplicateSelectedClip() },
                    onDeleteSelectedClip = { editorViewModel.deleteSelectedClip() },
                    onAddMediaClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Sheet Manager
        if (activeSheet != null) {
            EditorBottomSheetContainer(
                sheetType = activeSheet!!,
                selectedClip = selectedClip,
                onDismiss = { activeSheet = null },
                aspectRatio = project.aspectRatio,
                onSelectAspectRatio = { editorViewModel.setAspectRatio(it) },
                onRotateClip = { editorViewModel.rotateSelectedClip() },
                onFlipHorizontal = { editorViewModel.flipSelectedClipH() },
                onFlipVertical = { editorViewModel.flipSelectedClipV() },
                onSpeedChange = { editorViewModel.updateSelectedClipSpeed(it) },
                onVolumeChange = { editorViewModel.updateSelectedClipVolume(it) },
                onToggleMute = { editorViewModel.toggleSelectedClipMute() },
                onFreezeFrame = { },
                onDuplicateClip = { editorViewModel.duplicateSelectedClip() },
                onDeleteClip = { editorViewModel.deleteSelectedClip() },
                filterType = project.filterType,
                filterIntensity = project.filterIntensity,
                onSelectFilter = { editorViewModel.setFilter(it) },
                onFilterIntensityChange = { editorViewModel.setFilterIntensity(it) },
                colorAdjustments = project.colorAdjustments,
                onColorAdjustmentsChange = { editorViewModel.setColorAdjustments(it) },
                onResetColors = { editorViewModel.resetColorAdjustments() },
                onAddTextOverlay = { editorViewModel.addTextOverlay(it) },
                captions = project.captions,
                onGenerateCaptions = { editorViewModel.generateCaptions(it) },
                isGeneratingCaptions = isGeneratingCaptions,
                onDeleteCaption = { editorViewModel.deleteCaption(it) },
                onTriggerAutoEdit = { editorViewModel.analyzeAutoEdit(it) },
                autoEditSuggestion = autoEditSuggestion,
                isAnalyzingAutoEdit = isAnalyzingAutoEdit,
                onApplyAutoEdit = { editorViewModel.applyAutoEditSuggestion() },
                onApplyEnhance = { },
                onApplyBgRemoval = { },
                onAddAudioTrack = { editorViewModel.addAudioTrack(it) },
                onUploadAudioClick = { audioPickerLauncher.launch("audio/*") },
                audioClips = project.audioClips,
                onUpdateAudioClipVolume = { id, vol -> editorViewModel.updateAudioClipVolume(id, vol) },
                onUpdateAudioClipFades = { id, fi, fo -> editorViewModel.updateAudioClipFades(id, fi, fo) },
                onToggleAudioClipMute = { editorViewModel.toggleAudioClipMute(it) },
                onDeleteAudioClip = { editorViewModel.deleteAudioClip(it) },
                onToggleMuteAllClips = { editorViewModel.toggleMuteAllVideoClips() },
                onSpeakVoiceover = { text, lang, spd, ptch ->
                    editorViewModel.voiceoverEngine.speak(text, lang, spd, ptch)
                },
                onAddVoiceoverClip = { text, lang, dur ->
                    editorViewModel.addVoiceoverClip(text, lang, dur)
                },
                isSpeakingVoiceover = isSpeakingVoiceover,
                audioRecordingHelper = editorViewModel.audioRecordingHelper,
                onAddRecordedVoiceover = { editorViewModel.addRecordedVoiceover(it) },
                transitionType = project.transitionType,
                transitionDurationMs = project.transitionDurationMs,
                onSelectTransition = { t, d -> editorViewModel.setTransition(t, d) },
                effectType = project.effectType,
                effectIntensity = project.effectIntensity,
                onSelectEffect = { e, i -> editorViewModel.setEffect(e, i) }
            )
        }

        // Project Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                containerColor = DarkSurface,
                title = { Text("Rename Project", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editedName.isNotBlank()) {
                                editorViewModel.updateProjectName(editedName)
                            }
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                }
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    projectName: String,
    aspectRatio: AspectRatio,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onRename: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(modifier = Modifier.clickable { onRename() }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = projectName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = TextTertiaryDark, modifier = Modifier.size(12.dp))
                }
                Text(
                    text = aspectRatio.label.substringBefore(" "),
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color.White else TextTertiaryDark
                )
            }

            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color.White else TextTertiaryDark
                )
            }

            IconButton(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
            }

            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ToolsRibbonBar(
    onSelectTool: (EditorSheetType) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolRibbonButton(Icons.Default.Transform, "Edit/Ratio") { onSelectTool(EditorSheetType.BASIC_TOOLS) }
        ToolRibbonButton(Icons.Default.ColorLens, "Filters") { onSelectTool(EditorSheetType.FILTERS) }
        ToolRibbonButton(Icons.Default.Tune, "Color") { onSelectTool(EditorSheetType.COLOR_ADJUST) }
        ToolRibbonButton(Icons.Default.TextFields, "Text") { onSelectTool(EditorSheetType.TEXT_TOOL) }
        ToolRibbonButton(Icons.Default.Subtitles, "AI Captions", isAi = true) { onSelectTool(EditorSheetType.CAPTIONS) }
        ToolRibbonButton(Icons.Default.AutoFixHigh, "AI Auto Edit", isAi = true) { onSelectTool(EditorSheetType.AI_AUTO_EDIT) }
        ToolRibbonButton(Icons.Default.Hd, "AI Enhance", isAi = true) { onSelectTool(EditorSheetType.AI_ENHANCE) }
        ToolRibbonButton(Icons.Default.Layers, "BG Removal", isAi = true) { onSelectTool(EditorSheetType.BG_REMOVAL) }
        ToolRibbonButton(Icons.Default.MusicNote, "Audio") { onSelectTool(EditorSheetType.AUDIO) }
        ToolRibbonButton(Icons.Default.RecordVoiceOver, "AI Voiceover", isAi = true) { onSelectTool(EditorSheetType.VOICEOVER) }
        ToolRibbonButton(Icons.Default.Movie, "Transitions") { onSelectTool(EditorSheetType.TRANSITIONS) }
        ToolRibbonButton(Icons.Default.AutoFixNormal, "Effects") { onSelectTool(EditorSheetType.EFFECTS) }
    }
}

@Composable
private fun ToolRibbonButton(
    icon: ImageVector,
    label: String,
    isAi: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (isAi) AccentPurple.copy(alpha = 0.2f) else DarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = borderStroke(1.dp, if (isAi) AccentPurple else DarkSurfaceBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isAi) AccentCyan else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = if (isAi) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
