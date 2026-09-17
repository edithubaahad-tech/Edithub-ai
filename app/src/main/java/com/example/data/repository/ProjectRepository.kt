package com.example.data.repository

import com.example.data.local.ExportDao
import com.example.data.local.ExportEntity
import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val exportDao: ExportDao
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val clipsType = Types.newParameterizedType(List::class.java, TimelineClip::class.java)
    private val clipsAdapter = moshi.adapter<List<TimelineClip>>(clipsType)

    private val audioClipsType = Types.newParameterizedType(List::class.java, AudioClip::class.java)
    private val audioClipsAdapter = moshi.adapter<List<AudioClip>>(audioClipsType)

    private val textOverlaysType = Types.newParameterizedType(List::class.java, TextOverlay::class.java)
    private val textOverlaysAdapter = moshi.adapter<List<TextOverlay>>(textOverlaysType)

    private val captionsType = Types.newParameterizedType(List::class.java, CaptionItem::class.java)
    private val captionsAdapter = moshi.adapter<List<CaptionItem>>(captionsType)

    private val colorAdjustmentsAdapter = moshi.adapter(ColorAdjustments::class.java)

    val allProjects: Flow<List<ProjectRecord>> = projectDao.getAllProjects().map { list ->
        list.map { it.toRecord() }
    }

    val allExports: Flow<List<ExportEntity>> = exportDao.getAllExports()

    suspend fun getProject(id: String): ProjectRecord? {
        return projectDao.getProjectById(id)?.toRecord()
    }

    suspend fun saveProject(record: ProjectRecord) {
        val entity = ProjectEntity(
            id = record.id,
            name = record.name,
            aspectRatio = record.aspectRatio.name,
            durationMs = record.durationMs,
            lastEdited = System.currentTimeMillis(),
            thumbnailUri = record.thumbnailUri,
            clipsJson = try { clipsAdapter.toJson(record.clips) } catch (e: Exception) { "[]" },
            audioClipsJson = try { audioClipsAdapter.toJson(record.audioClips) } catch (e: Exception) { "[]" },
            textOverlaysJson = try { textOverlaysAdapter.toJson(record.textOverlays) } catch (e: Exception) { "[]" },
            captionsJson = try { captionsAdapter.toJson(record.captions) } catch (e: Exception) { "[]" },
            filterType = record.filterType.name,
            filterIntensity = record.filterIntensity,
            colorAdjustmentsJson = try { colorAdjustmentsAdapter.toJson(record.colorAdjustments) } catch (e: Exception) { "{}" },
            effectType = record.effectType.name,
            effectIntensity = record.effectIntensity,
            transitionType = record.transitionType.name,
            transitionDurationMs = record.transitionDurationMs
        )
        projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun saveExport(export: ExportEntity) {
        exportDao.insertExport(export)
    }

    suspend fun deleteExport(id: String) {
        exportDao.deleteExportById(id)
    }

    private fun ProjectEntity.toRecord(): ProjectRecord {
        val parsedClips = try { clipsAdapter.fromJson(clipsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val parsedAudio = try { audioClipsAdapter.fromJson(audioClipsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val parsedText = try { textOverlaysAdapter.fromJson(textOverlaysJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val parsedCaptions = try { captionsAdapter.fromJson(captionsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val parsedColors = try { colorAdjustmentsAdapter.fromJson(colorAdjustmentsJson) ?: ColorAdjustments() } catch (e: Exception) { ColorAdjustments() }

        return ProjectRecord(
            id = id,
            name = name,
            aspectRatio = try { AspectRatio.valueOf(aspectRatio) } catch (e: Exception) { AspectRatio.PORTRAIT_9_16 },
            durationMs = durationMs,
            lastEdited = lastEdited,
            thumbnailUri = thumbnailUri,
            clips = parsedClips,
            audioClips = parsedAudio,
            textOverlays = parsedText,
            captions = parsedCaptions,
            filterType = try { VideoFilterType.valueOf(filterType) } catch (e: Exception) { VideoFilterType.NONE },
            filterIntensity = filterIntensity,
            colorAdjustments = parsedColors,
            effectType = try { VideoEffectType.valueOf(effectType) } catch (e: Exception) { VideoEffectType.NONE },
            effectIntensity = effectIntensity,
            transitionType = try { TransitionType.valueOf(transitionType) } catch (e: Exception) { TransitionType.NONE },
            transitionDurationMs = transitionDurationMs
        )
    }
}

data class ProjectRecord(
    val id: String,
    val name: String,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val durationMs: Long = 0L,
    val lastEdited: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val clips: List<TimelineClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val captions: List<CaptionItem> = emptyList(),
    val filterType: VideoFilterType = VideoFilterType.NONE,
    val filterIntensity: Float = 100f,
    val colorAdjustments: ColorAdjustments = ColorAdjustments(),
    val effectType: VideoEffectType = VideoEffectType.NONE,
    val effectIntensity: Float = 50f,
    val transitionType: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L
)
