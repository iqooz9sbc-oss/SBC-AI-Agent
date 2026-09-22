package com.aiassistant.app.features.headshot

import android.graphics.Bitmap
import android.net.Uri

enum class HeadshotStyle(val displayName: String, val promptModifier: String) {
    CORPORATE("Corporate", "wearing a premium fitted dark business suit, corporate studio setup"),
    EXECUTIVE("Executive", "executive leadership style, luxury indoor office background with soft bokeh"),
    MODERN_CREATIVE("Creative", "modern smart-casual blazer, neutral textured aesthetic backdrop"),
    STUDIO_MINIMAL("Minimal Studio", "clean neutral gradient background, high-end commercial photo studio lighting")
}

sealed interface HeadshotUiState {
    object Idle : HeadshotUiState
    data class ImageSelected(val inputImage: Bitmap) : HeadshotUiState
    data class Loading(val inputImage: Bitmap) : HeadshotUiState
    data class Success(val inputImage: Bitmap, val outputImage: Bitmap) : HeadshotUiState
    data class Error(val inputImage: Bitmap?, val message: String) : HeadshotUiState
}

sealed interface HeadshotEvent {
    object ShowPaywall : HeadshotEvent
    data class ShowSnackbar(val message: String) : HeadshotEvent
    data class ImageSaved(val uri: Uri) : HeadshotEvent
}

