package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AspectRatio
import com.example.data.repository.ProjectRecord
import com.example.ui.components.borderStroke
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ProjectTemplate
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onCreateNewProject: (AspectRatio) -> Unit,
    onOpenProject: (String) -> Unit,
    onNavigateScriptToVideo: () -> Unit,
    onNavigateThumbnailGenerator: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val projects by homeViewModel.projects.collectAsState()
    val exports by homeViewModel.exports.collectAsState()
    var showAspectRatioDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectRecord?>(null) }

    Scaffold(
        containerColor = DarkCanvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Header & Branding
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HomeHeader(
                    isGeminiActive = homeViewModel.geminiService.isApiKeyConfigured(),
                    onSettingsClick = onNavigateSettings
                )
            }

            // Quick Actions Hub (Create, AI Auto Edit, AI Video Generator, AI Thumbnail)
            item {
                QuickActionsGrid(
                    onCreateClick = { showAspectRatioDialog = true },
                    onAutoEditClick = { showAspectRatioDialog = true },
                    onScriptToVideoClick = onNavigateScriptToVideo,
                    onThumbnailClick = onNavigateThumbnailGenerator
                )
            }

            // Recent Projects Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Projects",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} saved",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (projects.isEmpty()) {
                    EmptyProjectsCard(onCreateClick = { showAspectRatioDialog = true })
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        projects.forEach { project ->
                            ProjectCard(
                                project = project,
                                onOpen = { onOpenProject(project.id) },
                                onDelete = { projectToDelete = project }
                            )
                        }
                    }
                }
            }

            // Viral Video Templates Carousel
            item {
                Text(
                    text = "Video Templates",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeViewModel.templates) { tmpl ->
                        TemplateCard(
                            template = tmpl,
                            onClick = {
                                homeViewModel.createProjectFromTemplate(tmpl) { newId ->
                                    onOpenProject(newId)
                                }
                            }
                        )
                    }
                }
            }

            // My Exports Section
            if (exports.isNotEmpty()) {
                item {
                    Text(
                        text = "Exported Videos",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        exports.forEach { exp ->
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = borderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(AccentEmerald.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.MovieCreation, contentDescription = null, tint = AccentEmerald)
                                        }

                                        Column {
                                            Text(exp.projectName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text(
                                                "${exp.resolution} • ${exp.fps} FPS • ${String.format("%.1f", exp.fileSizeMb)} MB",
                                                color = TextSecondaryDark,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(onClick = { homeViewModel.deleteExport(exp.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiaryDark, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Aspect Ratio Picker Dialog for New Project
        if (showAspectRatioDialog) {
            AlertDialog(
                onDismissRequest = { showAspectRatioDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text("Select Project Format", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AspectRatio.entries.forEach { ratio ->
                            Surface(
                                color = DarkSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAspectRatioDialog = false
                                        onCreateNewProject(ratio)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ratio.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentPurple)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAspectRatioDialog = false }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                }
            )
        }

        // Delete Project Confirmation Dialog
        if (projectToDelete != null) {
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                containerColor = DarkSurface,
                title = { Text("Delete Project?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete '${projectToDelete?.name}'? This action cannot be undone.",
                        color = TextSecondaryDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            projectToDelete?.let { homeViewModel.deleteProject(it.id) }
                            projectToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isGeminiActive: Boolean,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EditHub AI",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    color = if (isGeminiActive) AccentIndigo else DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = borderStroke(1.dp, if (isGeminiActive) AccentPurple else DarkSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (isGeminiActive) "GEMINI 3.5" else "OFFLINE AI",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Create. Edit. Enhance. Publish.",
                color = TextSecondaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(42.dp)
                .background(DarkSurface, CircleShape)
                .border(1.dp, DarkSurfaceBorder, CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onCreateClick: () -> Unit,
    onAutoEditClick: () -> Unit,
    onScriptToVideoClick: () -> Unit,
    onThumbnailClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Hero Action: Create New Project
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreateClick() }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF3B82F6))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("NEW PROJECT", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Start Blank Timeline", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Multi-track video, audio, captions & transitions", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // AI Tools Triple Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // AI Auto Edit
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = borderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAutoEditClick() }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("AI Auto Edit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Auto cuts & pacing", color = TextSecondaryDark, fontSize = 11.sp)
                }
            }

            // Script to Video
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = borderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onScriptToVideoClick() }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentIndigo.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Script to Video", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Storyboard generator", color = TextSecondaryDark, fontSize = 11.sp)
                }
            }

            // AI Thumbnail
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = borderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onThumbnailClick() }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentRose.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = AccentRose, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("AI Thumbnail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("High-CTR visuals", color = TextSecondaryDark, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectRecord,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(project.lastEdited) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(project.lastEdited))
    }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = borderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Project Thumbnail Simulation
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AccentIndigo, AccentPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (project.aspectRatio == AspectRatio.LANDSCAPE_16_9) Icons.Default.Tv else Icons.Default.StayCurrentPortrait,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = project.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${project.aspectRatio.label.substringBefore(" ")} • ${project.durationMs / 1000}s • ${project.clips.size} clips",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Edited $dateStr",
                        color = TextTertiaryDark,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiaryDark, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: ProjectTemplate,
    onClick: () -> Unit
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = borderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(template.color), AccentPurple)
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = template.platform,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = template.description,
                color = TextSecondaryDark,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${template.durationSec}s",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Use Template →",
                    color = AccentPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyProjectsCard(onCreateClick: () -> Unit) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = borderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("No Projects Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                "Create your first AI video project or start with a viral template",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Project", fontWeight = FontWeight.Bold)
            }
        }
    }
}
