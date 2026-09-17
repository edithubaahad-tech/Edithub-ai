package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.*
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun SafePlayerPreview(
    aspectRatio: AspectRatio,
    currentMediaClip: TimelineClip?,
    currentPlayheadMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    activeCaptions: List<CaptionItem>,
    activeTextOverlays: List<TextOverlay>,
    filterType: VideoFilterType,
    filterIntensity: Float,
    colorAdjustments: ColorAdjustments,
    effectType: VideoEffectType,
    effectIntensity: Float,
    showSafeAreas: Boolean,
    onToggleSafeAreas: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onAddMediaClick: () -> Unit,
    exoPlayer: ExoPlayer? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "player_effects")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitch"
    )

    // Build color matrix based on filter & adjustments
    val colorMatrix = remember(filterType, filterIntensity, colorAdjustments) {
        buildColorMatrix(filterType, filterIntensity, colorAdjustments)
    }

    Box(
        modifier = modifier
            .background(DarkCanvas)
            .padding(if (isFullscreen) 0.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Player Container constrained to aspect ratio
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(aspectRatio.ratio)
                .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp))
                .background(Color.Black)
                .border(
                    width = 1.dp,
                    color = if (showSafeAreas) AccentIndigo.copy(alpha = 0.8f) else DarkSurfaceBorder,
                    shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp)
                )
                .clickable { onTogglePlay() },
            contentAlignment = Alignment.Center
        ) {
            // Media Render Layer
            if (currentMediaClip != null) {
                val context = LocalContext.current
                var graphicsModifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = currentMediaClip.rotation
                        scaleX = if (currentMediaClip.isFlippedH) -1f else 1f
                        scaleY = if (currentMediaClip.isFlippedV) -1f else 1f
                        if (effectType == VideoEffectType.SHAKE && isPlaying) {
                            translationX = sin(currentPlayheadMs / 50.0).toFloat() * (effectIntensity / 10f)
                            translationY = sin(currentPlayheadMs / 40.0).toFloat() * (effectIntensity / 10f)
                        }
                    }

                // If media is a valid URI, load it; else render animated video canvas simulation
                if (currentMediaClip.mediaUri.startsWith("content://") || currentMediaClip.mediaUri.startsWith("file://") || currentMediaClip.mediaUri.startsWith("http")) {
                    if (currentMediaClip.type == MediaType.VIDEO && exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    this.player = exoPlayer
                                    this.useController = false
                                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                            },
                            update = { view ->
                                if (view.player != exoPlayer) {
                                    view.player = exoPlayer
                                }
                            },
                            modifier = graphicsModifier
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentMediaClip.mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Video frame",
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.colorMatrix(colorMatrix),
                            modifier = graphicsModifier
                        )
                    }
                } else {
                    // Demo/generated scene render
                    GeneratedFrameCanvas(
                        clip = currentMediaClip,
                        isPlaying = isPlaying,
                        playheadMs = currentPlayheadMs,
                        colorFilterMatrix = colorMatrix,
                        modifier = graphicsModifier
                    )
                }

                // Visual Effects Layer
                if (effectType != VideoEffectType.NONE) {
                    RenderEffectOverlay(effectType, effectIntensity, glitchOffset, pulseAnim)
                }

                // Safe-Area Guidelines (for vertical formats: TikTok, Reels, Shorts)
                if (showSafeAreas && (aspectRatio == AspectRatio.PORTRAIT_9_16 || aspectRatio == AspectRatio.PORTRAIT_4_5)) {
                    SafeAreaOverlay()
                }

                // Active Text Overlays Layer
                activeTextOverlays.forEach { textOverlay ->
                    if (currentPlayheadMs >= textOverlay.startMs && currentPlayheadMs <= (textOverlay.startMs + textOverlay.durationMs)) {
                        RenderTextOverlayItem(textOverlay, pulseAnim)
                    }
                }

                // Active Subtitle / AI Captions Layer
                activeCaptions.firstOrNull { currentPlayheadMs in it.startMs..it.endMs }?.let { caption ->
                    RenderCaptionItem(caption, pulseAnim)
                }

            } else {
                // Empty Project State
                EmptyProjectPlaceholder(onAddMediaClick = onAddMediaClick)
            }

            // Quick Play/Pause Center Indicator
            if (!isPlaying && currentMediaClip != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.5.dp, AccentPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Top Badges (Aspect Ratio & Safe Area toggle)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    border = borderStroke(0.5.dp, DarkSurfaceBorder)
                ) {
                    Text(
                        text = aspectRatio.label.substringBefore(" "),
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleSafeAreas,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (showSafeAreas) AccentIndigo.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Safe areas",
                            tint = if (showSafeAreas) Color.White else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Player Badges (Time & Filter Info)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${formatTime(currentPlayheadMs)} / ${formatTime(totalDurationMs)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (filterType != VideoFilterType.NONE) {
                    Surface(
                        color = AccentPurple.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = filterType.displayName,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderEffectOverlay(
    effectType: VideoEffectType,
    effectIntensity: Float,
    glitchOffset: Float,
    pulseAnim: Float
) {
    when (effectType) {
        VideoEffectType.GLITCH -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val alpha = (effectIntensity / 100f) * 0.3f
                drawRect(
                    color = Color.Cyan.copy(alpha = alpha),
                    topLeft = Offset(glitchOffset * 2f, 0f),
                    size = size
                )
                drawRect(
                    color = Color.Magenta.copy(alpha = alpha),
                    topLeft = Offset(-glitchOffset * 2f, 0f),
                    size = size
                )
                for (y in 0 until size.height.toInt() step 12) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
        VideoEffectType.FILM -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = (effectIntensity / 100f) * 0.6f))
                    )
                )
            }
        }
        VideoEffectType.LIGHT_LEAK -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF9E00).copy(alpha = (effectIntensity / 100f) * 0.45f), Color.Transparent)
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.8f, size.height * 0.2f)
                )
            }
        }
        VideoEffectType.GLOW -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = AccentPurple.copy(alpha = (effectIntensity / 100f) * 0.18f * pulseAnim)
                )
            }
        }
        VideoEffectType.VHS -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (y in 0 until size.height.toInt() step 8) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun RenderTextOverlayItem(overlay: TextOverlay, pulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val animModifier = when (overlay.animation) {
            TextAnimationType.POP -> Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }
            TextAnimationType.BOUNCE -> Modifier.graphicsLayer { translationY = sin(pulse * 6.28f) * 6f }
            else -> Modifier
        }

        Box(
            modifier = Modifier
                .align(
                    when {
                        overlay.posY < 0.33f -> Alignment.TopCenter
                        overlay.posY > 0.66f -> Alignment.BottomCenter
                        else -> Alignment.Center
                    }
                )
                .then(animModifier)
                .background(
                    color = Color(overlay.backgroundColor).copy(alpha = overlay.opacity),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = overlay.text,
                color = Color(overlay.textColor),
                fontSize = overlay.fontSizeSp.sp,
                fontWeight = if (overlay.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (overlay.isItalic) FontStyle.Italic else FontStyle.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun RenderCaptionItem(caption: CaptionItem, pulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        when (caption.style) {
            CaptionStyle.SOCIAL_VIRAL -> {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(2.dp, AccentAmber)
                ) {
                    Text(
                        text = caption.text.uppercase(),
                        color = Color(0xFFFDE047),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            CaptionStyle.KARAOKE -> {
                Surface(
                    color = AccentPurple.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = caption.text,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            CaptionStyle.BOLD -> {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = caption.text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            CaptionStyle.HIGHLIGHT -> {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = caption.text,
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            CaptionStyle.MINIMAL -> {
                Text(
                    text = caption.text,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
            CaptionStyle.CLEAN -> {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp),
                    border = borderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = caption.text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SafeAreaOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))

        // TikTok / Reels Top UI Safe zone (safe from search/top tabs)
        val topMargin = size.height * 0.12f
        // Bottom UI Safe zone (safe from captions/audio bar)
        val bottomMargin = size.height * 0.20f
        // Right UI Safe zone (safe from like/comment/share icons)
        val rightMargin = size.width * 0.18f

        drawRect(
            color = Color(0xFF38BDF8).copy(alpha = 0.7f),
            topLeft = Offset(size.width * 0.05f, topMargin),
            size = Size(size.width * 0.90f - rightMargin, size.height - topMargin - bottomMargin),
            style = stroke
        )
    }
}

@Composable
private fun GeneratedFrameCanvas(
    clip: TimelineClip,
    isPlaying: Boolean,
    playheadMs: Long,
    colorFilterMatrix: ColorMatrix,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Render rich gradient backdrop matching clip theme
        val baseColor = Color(clip.color)
        val phase = if (isPlaying) (playheadMs % 4000L) / 4000f else 0.5f

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.9f),
                    AccentIndigo,
                    AccentPurple.copy(alpha = 0.8f)
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width * (0.5f + phase * 0.5f), size.height)
            )
        )

        // Animated video waveforms and geometric dynamic patterns
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = size.width * 0.35f * (0.9f + sin(phase * 6.28f) * 0.1f),
            center = Offset(centerX, centerY)
        )

        // Simulated cinematic grid lines
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, size.height * 0.33f),
            end = Offset(size.width, size.height * 0.33f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, size.height * 0.66f),
            end = Offset(size.width, size.height * 0.66f),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun EmptyProjectPlaceholder(onAddMediaClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(DarkSurfaceElevated, CircleShape)
                .border(1.dp, DarkSurfaceBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = "No media",
                tint = AccentPurple,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Media In Timeline",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Upload MP4, MOV, WebM or photos to begin",
            color = TextSecondaryDark,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onAddMediaClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Media", fontWeight = FontWeight.SemiBold)
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (ms % 1000) / 100
    return String.format("%02d:%02d.%d", minutes, seconds, tenths)
}

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

fun buildColorMatrix(
    filter: VideoFilterType,
    intensity: Float,
    adj: ColorAdjustments
): ColorMatrix {
    val matrix = when (filter) {
        VideoFilterType.BLACK_WHITE -> ColorMatrix().apply { setToSaturation(0f) }
        VideoFilterType.VIBRANT -> ColorMatrix().apply { setToSaturation(1.6f) }
        VideoFilterType.COOL -> ColorMatrix(
            floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 0.9f, 0f, 0f, 0f,
                0f, 0f, 1.3f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.WARM -> ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.1f, 0f, 0f, 5f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.CINEMATIC -> ColorMatrix(
            floatArrayOf(
                1.1f, 0f, 0f, 0f, -5f,
                0f, 1.05f, 0f, 0f, -5f,
                0f, 0f, 1.2f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.VINTAGE -> ColorMatrix(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, 20f,
                0f, 0.8f, 0f, 0f, 15f,
                0f, 0f, 0.6f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.HDR -> ColorMatrix().apply { setToSaturation(1.4f) }
        VideoFilterType.BRIGHT -> ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 25f,
                0f, 1.2f, 0f, 0f, 25f,
                0f, 0f, 1.2f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.DARK -> ColorMatrix(
            floatArrayOf(
                0.7f, 0f, 0f, 0f, -20f,
                0f, 0.7f, 0f, 0f, -20f,
                0f, 0f, 0.7f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        VideoFilterType.MOODY -> ColorMatrix(
            floatArrayOf(
                1.0f, 0f, 0f, 0f, 5f,
                0f, 0.9f, 0f, 0f, -10f,
                0f, 0f, 1.1f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        else -> ColorMatrix()
    }

    // Apply color adjustments
    if (adj.saturation != 1.0f) {
        val satMatrix = ColorMatrix()
        satMatrix.setToSaturation(adj.saturation)
        matrix.timesAssign(satMatrix)
    }

    return matrix
}
