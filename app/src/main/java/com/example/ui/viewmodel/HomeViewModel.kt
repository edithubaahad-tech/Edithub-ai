package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.AppDatabase
import com.example.data.local.ExportEntity
import com.example.data.models.AspectRatio
import com.example.data.models.TimelineClip
import com.example.data.models.VideoFilterType
import com.example.data.repository.ProjectRecord
import com.example.data.repository.ProjectRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ProjectTemplate(
    val id: String,
    val title: String,
    val platform: String,
    val aspectRatio: AspectRatio,
    val durationSec: Int,
    val description: String,
    val color: Long,
    val filter: VideoFilterType
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao(), database.exportDao())
    val geminiService = GeminiAiService()

    val projects: StateFlow<List<ProjectRecord>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val exports: StateFlow<List<ExportEntity>> = repository.allExports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    val templates = listOf(
        ProjectTemplate(
            id = "tmpl_1",
            title = "YouTube Shorts Viral",
            platform = "Shorts",
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            durationSec = 15,
            description = "High-energy hook with bold karaoke captions & quick zoom cuts",
            color = 0xFFEF4444,
            filter = VideoFilterType.VIBRANT
        ),
        ProjectTemplate(
            id = "tmpl_2",
            title = "Instagram Reels Aesthetic",
            platform = "Reels",
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            durationSec = 20,
            description = "Smooth cinematic color grading with warm tones and soft transitions",
            color = 0xFFEC4899,
            filter = VideoFilterType.WARM
        ),
        ProjectTemplate(
            id = "tmpl_3",
            title = "TikTok Trending Challenge",
            platform = "TikTok",
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            durationSec = 12,
            description = "Fast beat drops, dynamic glitch effects, and high contrast typography",
            color = 0xFF06B6D4,
            filter = VideoFilterType.CINEMATIC
        ),
        ProjectTemplate(
            id = "tmpl_4",
            title = "Cinematic Travel Vlog",
            platform = "YouTube 16:9",
            aspectRatio = AspectRatio.LANDSCAPE_16_9,
            durationSec = 30,
            description = "Wide landscape framing, film grain overlay, and ambient lofi soundtrack",
            color = 0xFF10B981,
            filter = VideoFilterType.VINTAGE
        ),
        ProjectTemplate(
            id = "tmpl_5",
            title = "Product Launch Teaser",
            platform = "Social 1:1",
            aspectRatio = AspectRatio.SQUARE_1_1,
            durationSec = 15,
            description = "Sleek product showcase with pop title animations & punchy pacing",
            color = 0xFF8B5CF6,
            filter = VideoFilterType.HDR
        )
    )

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun deleteExport(id: String) {
        viewModelScope.launch {
            repository.deleteExport(id)
        }
    }

    fun createProjectFromTemplate(template: ProjectTemplate, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val newProject = ProjectRecord(
                id = projectId,
                name = template.title,
                aspectRatio = template.aspectRatio,
                durationMs = template.durationSec * 1000L,
                filterType = template.filter,
                clips = listOf(
                    TimelineClip(
                        id = "clip_tmpl_1",
                        mediaUri = "tmpl_scene_1",
                        name = "${template.platform} Scene 1",
                        startMs = 0L,
                        endMs = 5000L,
                        originalDurationMs = 5000L,
                        color = template.color
                    ),
                    TimelineClip(
                        id = "clip_tmpl_2",
                        mediaUri = "tmpl_scene_2",
                        name = "${template.platform} Scene 2",
                        startMs = 0L,
                        endMs = (template.durationSec * 1000L - 5000L).coerceAtLeast(4000L),
                        originalDurationMs = 8000L,
                        color = 0xFF6366F1
                    )
                )
            )
            repository.saveProject(newProject)
            onCreated(projectId)
        }
    }
}
