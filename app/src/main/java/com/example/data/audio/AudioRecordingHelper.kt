package com.example.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class RecordedAudioResult(
    val fileUri: String,
    val durationMs: Long,
    val filePath: String
)

class AudioRecordingHelper {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime = 0L
    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    fun startRecording(context: Context): Boolean {
        if (_isRecording.value) return false

        try {
            val audioDir = File(context.cacheDir, "voiceovers").apply { mkdirs() }
            val outputFile = File(audioDir, "vo_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
            _recordingDurationMs.value = 0L

            amplitudeJob?.cancel()
            amplitudeJob = scope.launch {
                while (_isRecording.value) {
                    delay(50L)
                    _recordingDurationMs.value = System.currentTimeMillis() - recordingStartTime
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        val normAmp = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _currentAmplitude.value = normAmp
                    } catch (_: Exception) {
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e("AudioRecordingHelper", "Error starting media recorder", e)
            cancelRecording()
            return false
        }
    }

    fun stopRecording(): RecordedAudioResult? {
        if (!_isRecording.value) return null

        amplitudeJob?.cancel()
        _isRecording.value = false

        val duration = (System.currentTimeMillis() - recordingStartTime).coerceAtLeast(300L)
        val file = currentOutputFile

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {
                }
                release()
            }
            mediaRecorder = null

            if (file != null && file.exists() && file.length() > 0) {
                return RecordedAudioResult(
                    fileUri = Uri.fromFile(file).toString(),
                    durationMs = duration,
                    filePath = file.absolutePath
                )
            }
        } catch (e: Exception) {
            Log.e("AudioRecordingHelper", "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
            _currentAmplitude.value = 0f
            _recordingDurationMs.value = 0L
        }
        return null
    }

    fun cancelRecording() {
        amplitudeJob?.cancel()
        _isRecording.value = false
        _currentAmplitude.value = 0f
        _recordingDurationMs.value = 0L

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {
                }
                release()
            }
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
}
