package com.yourapp.features.headshot

import android.graphics.Bitmap

/**
 * Cost of a single headshot generation for non-admin (USER) accounts.
 * Centralized here so UI, ViewModel, and analytics all reference one constant.
 */
const val HEADSHOT_CREDIT_COST = 5

/**
 * One-shot UI state for the Headshot Generator screen.
 * Rendered by HeadshotGeneratorScreen via collectAsStateWithLifecycle().
 */
sealed interface HeadshotUiState {

    /** Nothing selected yet, first-launch state. */
    data object Idle : HeadshotUiState

    /** User has picked a source photo but generation hasn't started. */
    data class ImageSelected(val inputImage: Bitmap) : HeadshotUiState

    /** Generation in flight. Keeps the input image visible so the UI doesn't flicker. */
    data class Loading(val inputImage: Bitmap) : HeadshotUiState

    /** Generation succeeded. Holds both images for the side-by-side/stacked preview. */
    data class Success(
        val inputImage: Bitmap,
        val outputImage: Bitmap
    ) : HeadshotUiState

    /** Generation failed for a reason other than credits (network, API, decode, etc). */
    data class Error(
        val inputImage: Bitmap?,
        val message: String
    ) : HeadshotUiState
}

/**
 * One-off events the ViewModel fires that the screen should react to exactly once
 * (navigation, dialogs, snackbars) rather than re-render on every recomposition.
 */
sealed interface HeadshotEvent {
    data object ShowPaywall : HeadshotEvent
    data class ShowSnackbar(val message: String) : HeadshotEvent
    data class ImageSaved(val uri: android.net.Uri) : HeadshotEvent
}

/**
 * Style options exposed to the user; each maps to a prompt fragment in the repository.
 * Kept small and closed so prompt construction stays predictable and can't be
 * arbitrarily injected by user input.
 */
enum class HeadshotStyle(val displayName: String, val promptFragment: String) {
    CORPORATE_SUIT(
        displayName = "Corporate Suit",
        promptFragment = "wearing a tailored dark business suit with a crisp collared shirt"
    ),
    BUSINESS_CASUAL(
        displayName = "Business Casual",
        promptFragment = "wearing smart business-casual attire, such as a blazer over a plain top"
    ),
    CREATIVE_PROFESSIONAL(
        displayName = "Creative Professional",
        promptFragment = "wearing modern, minimalist professional attire in neutral tones"
    );
}
