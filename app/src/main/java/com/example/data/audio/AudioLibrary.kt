package com.example.data.audio

data class SampleAudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: String, // Music, SFX, Ambient
    val durationMs: Long,
    val bpm: Int,
    val waveformPattern: List<Float>
)

object SampleAudioLibrary {
    val tracks = listOf(
        SampleAudioTrack(
            id = "track_1",
            title = "Lo-Fi Midnight Beats",
            artist = "EditHub Studio (Royalty-Safe)",
            category = "Music",
            durationMs = 15000L,
            bpm = 85,
            waveformPattern = listOf(0.2f, 0.5f, 0.8f, 0.6f, 0.4f, 0.7f, 0.9f, 0.5f, 0.3f, 0.7f, 0.8f, 0.4f)
        ),
        SampleAudioTrack(
            id = "track_2",
            title = "Cyber Pulse Future",
            artist = "EditHub Studio (Royalty-Safe)",
            category = "Music",
            durationMs = 20000L,
            bpm = 124,
            waveformPattern = listOf(0.4f, 0.9f, 0.7f, 0.8f, 1.0f, 0.6f, 0.9f, 0.7f, 0.5f, 0.9f, 0.8f, 0.7f)
        ),
        SampleAudioTrack(
            id = "track_3",
            title = "Acoustic Sunlight",
            artist = "EditHub Studio (Royalty-Safe)",
            category = "Music",
            durationMs = 18000L,
            bpm = 100,
            waveformPattern = listOf(0.3f, 0.6f, 0.5f, 0.7f, 0.8f, 0.4f, 0.6f, 0.5f, 0.3f, 0.6f, 0.7f, 0.5f)
        ),
        SampleAudioTrack(
            id = "track_4",
            title = "Epic Cinematic Hybrid",
            artist = "EditHub Studio (Royalty-Safe)",
            category = "Music",
            durationMs = 25000L,
            bpm = 140,
            waveformPattern = listOf(0.1f, 0.2f, 0.4f, 0.7f, 0.9f, 1.0f, 0.8f, 0.6f, 0.9f, 1.0f, 0.7f, 0.3f)
        ),
        SampleAudioTrack(
            id = "sfx_1",
            title = "Fast Camera Whoosh",
            artist = "Sound FX",
            category = "SFX",
            durationMs = 600L,
            bpm = 0,
            waveformPattern = listOf(0.1f, 0.3f, 0.8f, 1.0f, 0.4f, 0.1f)
        ),
        SampleAudioTrack(
            id = "sfx_2",
            title = "Digital Glitch Pop",
            artist = "Sound FX",
            category = "SFX",
            durationMs = 450L,
            bpm = 0,
            waveformPattern = listOf(0.8f, 1.0f, 0.3f, 0.7f, 0.2f)
        ),
        SampleAudioTrack(
            id = "sfx_3",
            title = "Sub Bass Impact Drop",
            artist = "Sound FX",
            category = "SFX",
            durationMs = 1200L,
            bpm = 0,
            waveformPattern = listOf(1.0f, 0.9f, 0.8f, 0.6f, 0.4f, 0.2f, 0.1f)
        ),
        SampleAudioTrack(
            id = "sfx_4",
            title = "Notification Chime (Ding)",
            artist = "Sound FX",
            category = "SFX",
            durationMs = 800L,
            bpm = 0,
            waveformPattern = listOf(0.9f, 0.7f, 0.5f, 0.3f, 0.2f)
        )
    )
}
