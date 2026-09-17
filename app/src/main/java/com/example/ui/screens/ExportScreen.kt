package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.components.borderStroke
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    editorViewModel: EditorViewModel,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by editorViewModel.project.collectAsState()
    val isExporting by editorViewModel.isExporting.collectAsState()
    val exportProgress by editorViewModel.exportProgress.collectAsState()
    val exportStage by editorViewModel.exportStage.collectAsState()
    val exportedRecord by editorViewModel.exportedRecord.collectAsState()

    val resolutions = listOf("720p (HD)", "1080p (FHD)", "4K (Ultra HD)")
    var selectedRes by remember { mutableStateOf("1080p (FHD)") }

    val fpsOptions = listOf(24, 30, 60)
    var selectedFps by remember { mutableIntStateOf(60) }

    val qualities = listOf("Medium", "High", "Maximum")
    var selectedQuality by remember { mutableStateOf("High") }

    // Dynamic Estimated File Size
    val estimatedSizeMb = remember(project.durationMs, selectedRes, selectedFps, selectedQuality) {
        val seconds = (project.durationMs / 1000f).coerceAtLeast(1f)
        val mbPerSec = when (selectedRes) {
            "4K (Ultra HD)" -> 3.5f
            "1080p (FHD)" -> 1.4f
            else -> 0.7f
        } * (selectedFps / 30f) * (if (selectedQuality == "Maximum") 1.3f else 1.0f)
        seconds * mbPerSec
    }

    Scaffold(
        containerColor = DarkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Export Video", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isExporting) {
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
            // Project Summary Box
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(project.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Format: ${project.aspectRatio.label} • Duration: ${project.durationMs / 1000f}s • ${project.clips.size} clips",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Resolution Selector
            item {
                Text("Video Resolution", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(resolutions) { res ->
                        val isSelected = res == selectedRes
                        Surface(
                            color = if (isSelected) AccentPurple else DarkSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = borderStroke(1.dp, if (isSelected) AccentPurple else DarkSurfaceBorder),
                            modifier = Modifier.clickable(enabled = !isExporting) { selectedRes = res }
                        ) {
                            Text(
                                text = res,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Frame Rate Selector
            item {
                Text("Frame Rate (FPS)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fpsOptions.forEach { fps ->
                        val isSelected = fps == selectedFps
                        Surface(
                            color = if (isSelected) AccentIndigo else DarkSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = borderStroke(1.dp, if (isSelected) AccentIndigo else DarkSurfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isExporting) { selectedFps = fps }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text("$fps FPS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    when (fps) {
                                        24 -> "Cinematic"
                                        30 -> "Standard"
                                        else -> "Smooth"
                                    },
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quality Selector
            item {
                Text("Quality Bitrate", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    qualities.forEach { q ->
                        val isSelected = q == selectedQuality
                        Surface(
                            color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else DarkSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = borderStroke(1.dp, if (isSelected) AccentCyan else DarkSurfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = !isExporting) { selectedQuality = q }
                        ) {
                            Text(
                                text = q,
                                color = if (isSelected) AccentCyan else Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Real-Time Estimated Size Card
            item {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated File Size", color = TextSecondaryDark, fontSize = 13.sp)
                        Text(
                            String.format("~%.1f MB", estimatedSizeMb),
                            color = AccentAmber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Export Action & Progress
            item {
                if (isExporting) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(exportStage, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("${(exportProgress * 100).toInt()}%", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            color = AccentPurple,
                            trackColor = DarkSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                } else if (exportedRecord != null) {
                    // Export Success View
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = borderStroke(1.dp, AccentEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald)
                                Text("Export Complete! 🎉", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Saved to device as MP4: ${exportedRecord?.resolution} at ${exportedRecord?.fps} FPS (${String.format("%.1f", exportedRecord?.fileSizeMb)} MB)",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        exportedRecord?.let { record ->
                                            try {
                                                val file = File(record.outputUri)
                                                if (file.exists()) {
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.provider",
                                                        file
                                                    )
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "video/mp4"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Share Exported Video"))
                                                } else {
                                                    Toast.makeText(context, "Exported file not found", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(context, "Cannot share video: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        exportedRecord?.let { record ->
                                            try {
                                                val file = File(record.outputUri)
                                                if (file.exists()) {
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.provider",
                                                        file
                                                    )
                                                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "video/mp4")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(viewIntent)
                                                } else {
                                                    Toast.makeText(context, "Exported file not found", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(context, "No video player found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play", color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onNavigateHome,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Back to Home", color = Color.White)
                                }
                                Button(
                                    onClick = onBack,
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            editorViewModel.startExport(selectedRes, selectedFps, selectedQuality)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Export (${selectedRes})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
