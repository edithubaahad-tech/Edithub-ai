package com.example.data.models

enum class MediaType {
    VIDEO,
    IMAGE
}

enum class AspectRatio(val label: String, val ratio: Float, val widthRatio: Int, val heightRatio: Int) {
    PORTRAIT_9_16("9:16 (Shorts/Reels)", 9f / 16f, 9, 16),
    LANDSCAPE_16_9("16:9 (YouTube)", 16f / 9f, 16, 9),
    SQUARE_1_1("1:1 (Instagram)", 1f, 1, 1),
    PORTRAIT_4_5("4:5 (Feed)", 4f / 5f, 4, 5),
    CLASSIC_4_3("4:3 (Classic)", 4f / 3f, 4, 3);

    companion object {
        fun fromLabel(name: String): AspectRatio {
            return entries.find { it.name == name || it.label == name } ?: PORTRAIT_9_16
        }
    }
}

data class TimelineClip(
    val id: String,
    val mediaUri: String,
    val name: String,
    val type: MediaType = MediaType.VIDEO,
    val startMs: Long = 0L,
    val endMs: Long = 5000L,
    val originalDurationMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val rotation: Float = 0f,
    val isFlippedH: Boolean = false,
    val isFlippedV: Boolean = false,
    val cropRatio: Float = 1.0f,
    val color: Long = 0xFF3B82F6
) {
    val effectiveDurationMs: Long
        get() = ((endMs - startMs) / speed).toLong().coerceAtLeast(100L)
}

data class AudioClip(
    val id: String,
    val title: String,
    val audioUri: String,
    val startMs: Long = 0L,
    val durationMs: Long = 10000L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val fadeInMs: Long = 500L,
    val fadeOutMs: Long = 500L,
    val isVoiceover: Boolean = false
)

enum class TextAnimationType {
    NONE,
    FADE,
    POP,
    SLIDE_UP,
    SLIDE_DOWN,
    ZOOM,
    TYPEWRITER,
    BOUNCE
}

data class TextOverlay(
    val id: String,
    val text: String,
    val fontName: String = "Sans",
    val fontSizeSp: Float = 24f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x88000000,
    val outlineColor: Long = 0xFF000000,
    val hasOutline: Boolean = true,
    val hasShadow: Boolean = true,
    val opacity: Float = 1.0f,
    val animation: TextAnimationType = TextAnimationType.POP,
    val posX: Float = 0.5f, // 0.0 to 1.0 relative
    val posY: Float = 0.7f,
    val startMs: Long = 0L,
    val durationMs: Long = 3000L
)

enum class CaptionStyle {
    BOLD,
    CLEAN,
    KARAOKE,
    HIGHLIGHT,
    MINIMAL,
    SOCIAL_VIRAL
}

data class CaptionItem(
    val id: String,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val style: CaptionStyle = CaptionStyle.SOCIAL_VIRAL
)

enum class VideoFilterType(val displayName: String) {
    NONE("Original"),
    CINEMATIC("Cinematic"),
    WARM("Warm"),
    COOL("Cool"),
    VINTAGE("Vintage"),
    BLACK_WHITE("B & W"),
    HDR("HDR"),
    BRIGHT("Bright"),
    DARK("Dark"),
    MOODY("Moody"),
    VIBRANT("Vibrant"),
    NATURAL("Natural")
}

data class ColorAdjustments(
    val brightness: Float = 0f,   // -1.0 to 1.0
    val contrast: Float = 1.0f,   // 0.5 to 2.0
    val saturation: Float = 1.0f, // 0.0 to 2.0
    val temperature: Float = 0f,  // -1.0 to 1.0
    val tint: Float = 0f,         // -1.0 to 1.0
    val highlights: Float = 0f,   // -1.0 to 1.0
    val shadows: Float = 0f,      // -1.0 to 1.0
    val sharpness: Float = 0f,    // 0.0 to 1.0
    val fade: Float = 0f,         // 0.0 to 1.0
    val vignette: Float = 0f      // 0.0 to 1.0
)

enum class VideoEffectType(val displayName: String) {
    NONE("None"),
    GLITCH("Glitch"),
    SHAKE("Camera Shake"),
    FILM("Film Grain"),
    LIGHT_LEAK("Light Leak"),
    BLUR("Motion Blur"),
    GLOW("Dreamy Glow"),
    VHS("Retro VHS"),
    RGB_SPLIT("RGB Split"),
    SPEED_LINES("Speed Lines")
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    FADE("Fade"),
    DISSOLVE("Dissolve"),
    ZOOM("Zoom"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    PUSH("Push"),
    BLUR("Blur"),
    FLASH("Flash"),
    SPIN("Spin")
}

data class AIAutoEditSuggestion(
    val styleName: String,
    val pacingDescription: String,
    val recommendedCuts: List<String>,
    val musicTimingNote: String,
    val captionPlacementNote: String,
    val suggestedTransition: TransitionType,
    val suggestedFilter: VideoFilterType,
    val hookTimestampMs: Long
)

data class ScriptScene(
    val sceneNumber: Int,
    val description: String,
    val suggestedVisuals: String,
    val voiceoverText: String,
    val captionText: String,
    val durationSeconds: Int
)

data class ScriptToVideoPlan(
    val title: String,
    val targetStyle: String,
    val totalDurationSeconds: Int,
    val scenes: List<ScriptScene>
)

data class ThumbnailConcept(
    val headlineText: String,
    val subjectDescription: String,
    val backgroundStyle: String,
    val compositionTip: String,
    val suggestedPalette: List<String>,
    val previewGradientStart: Long = 0xFF8B5CF6,
    val previewGradientEnd: Long = 0xFFEC4899
)
