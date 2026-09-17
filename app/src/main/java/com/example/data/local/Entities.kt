package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val aspectRatio: String,
    val durationMs: Long,
    val lastEdited: Long,
    val thumbnailUri: String?,
    val clipsJson: String,
    val audioClipsJson: String,
    val textOverlaysJson: String,
    val captionsJson: String,
    val filterType: String,
    val filterIntensity: Float,
    val colorAdjustmentsJson: String,
    val effectType: String,
    val effectIntensity: Float,
    val transitionType: String,
    val transitionDurationMs: Long
)

@Entity(tableName = "exports")
data class ExportEntity(
    @PrimaryKey val id: String,
    val projectName: String,
    val resolution: String,
    val fps: Int,
    val quality: String,
    val fileSizeMb: Double,
    val createdAt: Long,
    val outputUri: String
)
