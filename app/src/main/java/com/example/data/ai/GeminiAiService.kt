package com.example.data.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.BuildConfig
import com.example.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun analyzeAutoEdit(
        style: String,
        clipsCount: Int,
        totalDurationSeconds: Int,
        clipNames: List<String>
    ): AIAutoEditSuggestion = withContext(Dispatchers.IO) {
        val prompt = """
            You are a professional video editor AI for EditHub AI.
            Analyze the following video project and provide expert editing recommendations for style '$style':
            - Number of clips: $clipsCount
            - Clip names: ${clipNames.joinToString(", ")}
            - Total rough duration: ${totalDurationSeconds}s
            
            Return JSON with:
            {
              "pacing": "short description of pacing (e.g., dynamic 1.5s cuts, rhythm-matched)",
              "cuts": ["cut recommendation 1", "cut recommendation 2", "cut recommendation 3"],
              "musicTiming": "music cue and beat drop recommendation",
              "captionPlacement": "best placement and animation style for captions",
              "transition": "FADE, DISSOLVE, ZOOM, SLIDE_LEFT, SLIDE_RIGHT, PUSH, BLUR, FLASH, or SPIN",
              "filter": "CINEMATIC, WARM, COOL, VINTAGE, BLACK_WHITE, HDR, BRIGHT, DARK, MOODY, VIBRANT, or NATURAL",
              "hookTimestampMs": 800
            }
        """.trimIndent()

        val jsonResponse = callGeminiOrFallback(prompt)
        parseAutoEditResponse(jsonResponse, style)
    }

    suspend fun generateCaptions(
        contextDescription: String,
        durationSeconds: Int,
        style: CaptionStyle
    ): List<CaptionItem> = withContext(Dispatchers.IO) {
        val prompt = """
            You are an AI Subtitles & Caption generator for EditHub AI.
            Generate engaging, timestamped video captions for a video about '$contextDescription' with duration ${durationSeconds}s.
            Return a JSON array of caption objects:
            [
              {
                "startMs": 0,
                "endMs": 2500,
                "text": "Captivating hook statement here"
              },
              ...
            ]
            Keep sentences punchy and modern. Do not exceed ${durationSeconds * 1000} ms.
        """.trimIndent()

        val jsonStr = callGeminiOrFallback(prompt)
        parseCaptionsResponse(jsonStr, style, durationSeconds)
    }

    suspend fun generateScriptToVideo(
        script: String,
        style: String,
        durationSeconds: Int,
        language: String
    ): ScriptToVideoPlan = withContext(Dispatchers.IO) {
        val prompt = """
            You are an AI Storyboard & Video Production Director for EditHub AI.
            Convert this script into a structured video storyboard:
            Script: "$script"
            Style: $style
            Target Duration: ${durationSeconds} seconds
            Language: $language

            Return a JSON object with:
            {
              "title": "Project Title",
              "targetStyle": "$style",
              "totalDurationSeconds": $durationSeconds,
              "scenes": [
                {
                  "sceneNumber": 1,
                  "description": "Visual scene description",
                  "suggestedVisuals": "Camera angle and subject action",
                  "voiceoverText": "Spoken voiceover line",
                  "captionText": "On-screen subtitle text",
                  "durationSeconds": 3
                }
              ]
            }
        """.trimIndent()

        val jsonStr = callGeminiOrFallback(prompt)
        parseScriptToVideoResponse(jsonStr, script, style, durationSeconds)
    }

    suspend fun generateThumbnailConcept(
        title: String,
        description: String
    ): ThumbnailConcept = withContext(Dispatchers.IO) {
        val prompt = """
            You are an AI YouTube & Social Media Thumbnail Strategist for EditHub AI.
            Generate a high-CTR thumbnail concept for video:
            Title: "$title"
            Description: "$description"

            Return JSON:
            {
              "headlineText": "3-5 word high-impact overlay text",
              "subjectDescription": "Main subject placement and expression",
              "backgroundStyle": "Background scenery, lighting and contrast",
              "compositionTip": "Rule of thirds / focal depth tip",
              "suggestedPalette": ["#3B82F6", "#8B5CF6", "#EC4899", "#FBBF24"]
            }
        """.trimIndent()

        val jsonStr = callGeminiOrFallback(prompt)
        parseThumbnailResponse(jsonStr, title)
    }

    private suspend fun callGeminiOrFallback(prompt: String): String {
        if (!isApiKeyConfigured()) {
            return generateLocalFallbackJson(prompt)
        }

        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val bodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$prompt\nIMPORTANT: Respond ONLY with pure valid JSON. No markdown codeblocks or quotes.")
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return generateLocalFallbackJson(prompt)
            }

            val responseBody = response.body?.string() ?: return generateLocalFallbackJson(prompt)
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            cleanJsonString(text)
        } catch (e: Exception) {
            generateLocalFallbackJson(prompt)
        }
    }

    private fun cleanJsonString(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```json")) s = s.removePrefix("```json").trim()
        if (s.startsWith("```")) s = s.removePrefix("```").trim()
        if (s.endsWith("```")) s = s.removeSuffix("```").trim()
        return s.trim()
    }

    private fun generateLocalFallbackJson(prompt: String): String {
        return when {
            prompt.contains("Storyboard") || prompt.contains("Script") -> """
                {
                  "title": "Dynamic AI Edit",
                  "targetStyle": "Viral Short",
                  "totalDurationSeconds": 15,
                  "scenes": [
                    {
                      "sceneNumber": 1,
                      "description": "Dramatic opening hook with close-up subject",
                      "suggestedVisuals": "Fast push-in zoom with vibrant neon grade",
                      "voiceoverText": "Stop scrolling and check this out right now!",
                      "captionText": "STOP SCROLLING 🚀",
                      "durationSeconds": 3
                    },
                    {
                      "sceneNumber": 2,
                      "description": "Core value showcase with energetic motion",
                      "suggestedVisuals": "Wide dynamic angle displaying the primary action",
                      "voiceoverText": "Here is exactly how you transform ideas into high-quality videos effortlessly.",
                      "captionText": "Transform Your Content 🔥",
                      "durationSeconds": 5
                    },
                    {
                      "sceneNumber": 3,
                      "description": "Key highlight demonstration and visual contrast",
                      "suggestedVisuals": "Split perspective showing before and after enhancement",
                      "voiceoverText": "AI auto-edits cuts, color grading, and timing instantly.",
                      "captionText": "Instant AI Magic ✨",
                      "durationSeconds": 4
                    },
                    {
                      "sceneNumber": 4,
                      "description": "Strong closing CTA with branding",
                      "suggestedVisuals": "Smooth pull-out zoom with logo watermark",
                      "voiceoverText": "Create, edit, enhance, and publish with EditHub AI today.",
                      "captionText": "Publish with EditHub AI 🎬",
                      "durationSeconds": 3
                    }
                  ]
                }
            """.trimIndent()

            prompt.contains("Captions") || prompt.contains("Subtitles") -> """
                [
                  {"startMs": 0, "endMs": 2800, "text": "Are you ready for the next level? 🚀"},
                  {"startMs": 2800, "endMs": 6200, "text": "EditHub AI automatically cuts and styles your footage ✨"},
                  {"startMs": 6200, "endMs": 9800, "text": "Add seamless transitions and viral typography instantly 🎬"},
                  {"startMs": 9800, "endMs": 14000, "text": "Create. Edit. Enhance. Publish. 💫"}
                ]
            """.trimIndent()

            prompt.contains("Thumbnail") -> """
                {
                  "headlineText": "INSANE RESULTS ⚡",
                  "subjectDescription": "High contrast expression facing the camera with expressive eyes",
                  "backgroundStyle": "Deep dark cyberpunk backdrop with glowing purple and cyan accents",
                  "compositionTip": "Position subject on left third; overlay bold glowing headline on top right",
                  "suggestedPalette": ["#3B82F6", "#8B5CF6", "#EC4899", "#FBBF24"]
                }
            """.trimIndent()

            else -> """
                {
                  "pacing": "Rapid-fire 1.8s cuts timed to upbeat energetic rhythm",
                  "cuts": [
                    "Trim dead space in first 0.8s for maximum viewer retention hook",
                    "Split secondary clip at apex action to heighten tension",
                    "Remove redundant stationary frames between beats"
                  ],
                  "musicTiming": "Align transition directly on primary bass drop at 2.4s",
                  "captionPlacement": "Center-lower screen inside vertical safe-zone with Pop animation",
                  "transition": "ZOOM",
                  "filter": "CINEMATIC",
                  "hookTimestampMs": 800
                }
            """.trimIndent()
        }
    }

    private fun parseAutoEditResponse(jsonStr: String, style: String): AIAutoEditSuggestion {
        return try {
            val obj = JSONObject(jsonStr)
            val cutsList = mutableListOf<String>()
            val cutsArr = obj.optJSONArray("cuts")
            if (cutsArr != null) {
                for (i in 0 until cutsArr.length()) {
                    cutsList.add(cutsArr.optString(i))
                }
            } else {
                cutsList.add("Trim non-action frames for high viewer retention")
                cutsList.add("Match cut points with musical transients")
            }

            AIAutoEditSuggestion(
                styleName = style,
                pacingDescription = obj.optString("pacing", "Rhythm-matched dynamic cuts"),
                recommendedCuts = cutsList,
                musicTimingNote = obj.optString("musicTiming", "Align cut with beat drops at 1.5s intervals"),
                captionPlacementNote = obj.optString("captionPlacement", "Center safe-area overlay with Pop effect"),
                suggestedTransition = try { TransitionType.valueOf(obj.optString("transition", "ZOOM")) } catch (e: Exception) { TransitionType.ZOOM },
                suggestedFilter = try { VideoFilterType.valueOf(obj.optString("filter", "CINEMATIC")) } catch (e: Exception) { VideoFilterType.CINEMATIC },
                hookTimestampMs = obj.optLong("hookTimestampMs", 800L)
            )
        } catch (e: Exception) {
            AIAutoEditSuggestion(
                styleName = style,
                pacingDescription = "Dynamic viral pacing with 1.5s cuts",
                recommendedCuts = listOf(
                    "Trim intro delay for instant 0.5s visual hook",
                    "Highlight peak action with seamless speed ramp",
                    "Remove idle pause before second scene"
                ),
                musicTimingNote = "Synchronize clip changes with acoustic downbeats",
                captionPlacementNote = "Middle safe-zone with social viral typography",
                suggestedTransition = TransitionType.ZOOM,
                suggestedFilter = VideoFilterType.CINEMATIC,
                hookTimestampMs = 600L
            )
        }
    }

    private fun parseCaptionsResponse(jsonStr: String, style: CaptionStyle, durationSeconds: Int): List<CaptionItem> {
        val result = mutableListOf<CaptionItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                result.add(
                    CaptionItem(
                        id = "cap_${System.currentTimeMillis()}_$i",
                        text = item.optString("text", "Caption line"),
                        startMs = item.optLong("startMs", (i * 3000L)),
                        endMs = item.optLong("endMs", ((i + 1) * 3000L).coerceAtMost(durationSeconds * 1000L)),
                        style = style
                    )
                )
            }
        } catch (e: Exception) {
            result.add(CaptionItem("c1", "Welcome to EditHub AI 🚀", 0L, 3000L, style))
            result.add(CaptionItem("c2", "AI Auto-Edit & Color Grading ✨", 3000L, 6500L, style))
            result.add(CaptionItem("c3", "Create. Edit. Enhance. Publish. 🎬", 6500L, (durationSeconds * 1000L).coerceAtLeast(10000L), style))
        }
        return result
    }

    private fun parseScriptToVideoResponse(jsonStr: String, originalScript: String, style: String, duration: Int): ScriptToVideoPlan {
        return try {
            val obj = JSONObject(jsonStr)
            val scenes = mutableListOf<ScriptScene>()
            val scenesArr = obj.optJSONArray("scenes")
            if (scenesArr != null) {
                for (i in 0 until scenesArr.length()) {
                    val sObj = scenesArr.getJSONObject(i)
                    scenes.add(
                        ScriptScene(
                            sceneNumber = sObj.optInt("sceneNumber", i + 1),
                            description = sObj.optString("description", "Scene $i"),
                            suggestedVisuals = sObj.optString("suggestedVisuals", "Dynamic footage shot"),
                            voiceoverText = sObj.optString("voiceoverText", "Narration for scene $i"),
                            captionText = sObj.optString("captionText", "Scene subtitle"),
                            durationSeconds = sObj.optInt("durationSeconds", 3)
                        )
                    )
                }
            }
            ScriptToVideoPlan(
                title = obj.optString("title", "AI Generated Project"),
                targetStyle = obj.optString("targetStyle", style),
                totalDurationSeconds = obj.optInt("totalDurationSeconds", duration),
                scenes = if (scenes.isNotEmpty()) scenes else defaultScenes(originalScript)
            )
        } catch (e: Exception) {
            ScriptToVideoPlan(
                title = "AI Video Storyboard",
                targetStyle = style,
                totalDurationSeconds = duration,
                scenes = defaultScenes(originalScript)
            )
        }
    }

    private fun defaultScenes(script: String): List<ScriptScene> = listOf(
        ScriptScene(1, "Hook Opening", "Close-up dynamic subject with intense gaze", "Did you know you can edit videos 10x faster with AI?", "EDIT 10x FASTER ⚡", 3),
        ScriptScene(2, "Problem & Solution", "Screen capture of complex timeline getting auto-simplified", "EditHub AI handles all the cuts, music timing, and captions for you.", "Smart Timeline Cuts ✂️", 5),
        ScriptScene(3, "Feature Showcase", "Split-screen before and after color enhancement", "From flat camera footage to stunning cinematic grading in one tap.", "Cinematic Grading 🎨", 4),
        ScriptScene(4, "Call to Action", "Sleek animated logo with published video card", "Try EditHub AI today and take your content to the next level!", "Publish Your Masterpiece 🚀", 3)
    )

    private fun parseThumbnailResponse(jsonStr: String, title: String): ThumbnailConcept {
        return try {
            val obj = JSONObject(jsonStr)
            val palette = mutableListOf<String>()
            val pArr = obj.optJSONArray("suggestedPalette")
            if (pArr != null) {
                for (i in 0 until pArr.length()) palette.add(pArr.optString(i))
            } else {
                palette.addAll(listOf("#3B82F6", "#8B5CF6", "#EC4899", "#FBBF24"))
            }

            ThumbnailConcept(
                headlineText = obj.optString("headlineText", title.take(20).uppercase()),
                subjectDescription = obj.optString("subjectDescription", "Expressive subject framed with high contrast"),
                backgroundStyle = obj.optString("backgroundStyle", "Vibrant gradient backdrop with rim lighting"),
                compositionTip = obj.optString("compositionTip", "Subject on left third, headline text top-right with shadow"),
                suggestedPalette = palette
            )
        } catch (e: Exception) {
            ThumbnailConcept(
                headlineText = "WATCH THIS NOW 🔥",
                subjectDescription = "Dynamic centered subject with bright key lighting",
                backgroundStyle = "Deep dark indigo gradient with neon cyber glow",
                compositionTip = "Large 3D text overlay at top with high contrast backdrop",
                suggestedPalette = listOf("#6366F1", "#A855F7", "#EC4899", "#F59E0B")
            )
        }
    }
}
