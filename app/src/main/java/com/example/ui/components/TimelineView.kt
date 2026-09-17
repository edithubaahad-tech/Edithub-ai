package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*

@Composable
fun TimelineView(
    clips: List<TimelineClip>,
    audioClips: List<AudioClip>,
    textOverlays: List<TextOverlay>,
    captions: List<CaptionItem>,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    currentPlayheadMs: Long,
    onSeekPlayhead: (Long) -> Unit,
    totalDurationMs: Long,
    zoomFactor: Float, // 1.0f base, 0.5f to 4.0f
    onZoomChange: (Float) -> Unit,
    onSplitClipAtPlayhead: () -> Unit,
    onTrimSelectedClipStart: (Long) -> Unit,
    onTrimSelectedClipEnd: (Long) -> Unit,
    onDuplicateSelectedClip: () -> Unit,
    onDeleteSelectedClip: () -> Unit,
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val msPerDp = (100f / zoomFactor).coerceAtLeast(10f) // millisecond per dp
    val totalWidthDp = ((totalDurationMs.coerceAtLeast(10000L)) / msPerDp).dp + 240.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder)
    ) {
        // Timeline Action Bar (Split, Duplicate, Delete, Zoom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Quick Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = onSplitClipAtPlayhead,
                    enabled = selectedClipId != null,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurface,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Split", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Split", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onDuplicateSelectedClip,
                    enabled = selectedClipId != null,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurface,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onDeleteSelectedClip,
                    enabled = selectedClipId != null,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurface,
                        contentColor = AccentRose
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Zoom Controls & Current Time Readout
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { onZoomChange((zoomFactor - 0.25f).coerceAtLeast(0.5f)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                }

                Text(
                    text = "${(zoomFactor * 100).toInt()}%",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = { onZoomChange((zoomFactor + 0.25f).coerceAtMost(3.0f)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Multi-Track Canvas & Scrubber
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .horizontalScroll(scrollState)
        ) {
            // Tracks Column
            Column(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 80.dp, top = 6.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Time Ruler (Top Seconds Indicator)
                TimeRuler(
                    totalDurationMs = totalDurationMs.coerceAtLeast(10000L),
                    msPerDp = msPerDp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )

                // Track 1: Text Overlays & Captions Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Render Captions
                    captions.forEach { cap ->
                        val startDp = (cap.startMs / msPerDp).dp
                        val widthDp = (((cap.endMs - cap.startMs) / msPerDp).coerceAtLeast(20f)).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startDp)
                                .width(widthDp)
                                .fillMaxHeight()
                                .background(AccentAmber.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "💬 ${cap.text}",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Render Text Overlays
                    textOverlays.forEach { txt ->
                        val startDp = (txt.startMs / msPerDp).dp
                        val widthDp = ((txt.durationMs / msPerDp).coerceAtLeast(20f)).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startDp)
                                .width(widthDp)
                                .fillMaxHeight()
                                .background(AccentPurple.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "T: ${txt.text}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Track 2: Primary Video/Media Track (Large Thumbnails)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    clips.forEachIndexed { index, clip ->
                        val isSelected = clip.id == selectedClipId
                        val clipWidthDp = (clip.effectiveDurationMs / msPerDp).dp.coerceAtLeast(50.dp)

                        Box(
                            modifier = Modifier
                                .width(clipWidthDp)
                                .fillMaxHeight()
                                .padding(horizontal = 1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(clip.color).copy(alpha = 0.85f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else DarkSurfaceBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectClip(clip.id) }
                        ) {
                            // Clip Info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = clip.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (clip.speed != 1.0f) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${clip.speed}x",
                                            color = AccentCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            // Duration badge at bottom
                            Text(
                                text = "${clip.effectiveDurationMs / 1000f}s",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                            )

                            // Trim Handles if selected
                            if (isSelected) {
                                // Left handle (Trim Start)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .width(18.dp)
                                        .fillMaxHeight()
                                        .background(AccentCyan.copy(alpha = 0.95f))
                                        .pointerInput(clip.id) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val deltaMs = (dragAmount.x * msPerDp).toLong()
                                                onTrimSelectedClipStart(deltaMs)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Trim start", tint = Color.Black, modifier = Modifier.size(14.dp))
                                }

                                // Right handle (Trim End)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(18.dp)
                                        .fillMaxHeight()
                                        .background(AccentCyan.copy(alpha = 0.95f))
                                        .pointerInput(clip.id) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val deltaMs = (dragAmount.x * msPerDp).toLong()
                                                onTrimSelectedClipEnd(deltaMs)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Trim end", tint = Color.Black, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Add Clip Button inline in timeline
                    IconButton(
                        onClick = onAddMediaClick,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, AccentPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add clip", tint = AccentPurple)
                    }
                }

                // Track 3: Audio / Music Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    audioClips.forEach { audio ->
                        val startDp = (audio.startMs / msPerDp).dp
                        val widthDp = ((audio.durationMs / msPerDp).coerceAtLeast(30f)).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startDp)
                                .width(widthDp)
                                .fillMaxHeight()
                                .background(
                                    if (audio.isVoiceover) AccentEmerald.copy(alpha = 0.85f) else AccentIndigo.copy(alpha = 0.85f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (audio.isVoiceover) Icons.Default.Mic else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = audio.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Playhead Scrubber (Red Vertical Line + Head Marker)
            val playheadOffsetDp = (currentPlayheadMs / msPerDp).dp + 16.dp

            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp - 14.dp)
                    .width(28.dp)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = (dragAmount.x * msPerDp).toLong()
                            val newMs = (currentPlayheadMs + deltaMs).coerceIn(0L, totalDurationMs.coerceAtLeast(1000L))
                            onSeekPlayhead(newMs)
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // Playhead Pin Head
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(AccentRose, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )

                // Playhead Vertical Line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(AccentRose)
                )
            }
        }
    }
}

@Composable
private fun TimeRuler(
    totalDurationMs: Long,
    msPerDp: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalSecs = (totalDurationMs / 1000).toInt()
        for (sec in 0..totalSecs) {
            val xDp = (sec * 1000f) / msPerDp
            val x = xDp.dp.toPx()

            // Major tick each second
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )

            // Half-second minor tick
            val halfX = ((sec * 1000f + 500f) / msPerDp).dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(halfX, size.height * 0.5f),
                end = Offset(halfX, size.height),
                strokeWidth = 0.8f
            )
        }
    }
}
