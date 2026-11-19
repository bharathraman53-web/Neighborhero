package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class AssignHelperDto(
    @SerializedName("helper_id") val helperId: String? = null
)

data class SubmitCompletionDto(
    @SerializedName("notes") val notes: String,
    @SerializedName("proof_photo_url") val proofPhotoUrl: String? = null
)

data class VerifyCompletionDto(
    @SerializedName("rating") val rating: Int = 5,
    @SerializedName("feedback") val feedback: String = "",
    @SerializedName("tags") val tags: String = ""
)

data class UploadEvidencePhotoDto(
    @SerializedName("photo_url") val photoUrl: String
)

data class TaskEvidenceDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("request_id") val requestId: String? = null,
    @SerializedName("helper_id") val helperId: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("proof_photo_url") val proofPhotoUrl: String? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null,
    @SerializedName("helper_name") val helperName: String? = null,
    @SerializedName("helper_avatar") val helperAvatar: String? = null
)

data class UpdateLiveLocationDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("heading") val heading: Double? = null,
    @SerializedName("accuracy") val accuracy: Double? = null,
    @SerializedName("speed") val speed: Double? = null
)

data class LiveTrackingDto(
    @SerializedName("request_id") val requestId: String,
    @SerializedName("status") val status: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("helper_id") val helperId: String? = null,
    @SerializedName("helper_name") val helperName: String? = null,
    @SerializedName("helper_avatar") val helperAvatar: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("heading") val heading: Double? = null,
    @SerializedName("accuracy") val accuracy: Double? = null,
    @SerializedName("speed") val speed: Double? = null,
    @SerializedName("requester_latitude") val requesterLatitude: Double? = null,
    @SerializedName("requester_longitude") val requesterLongitude: Double? = null,
    @SerializedName("requester_location") val requesterLocation: String? = null,
    @SerializedName("distance_km") val distanceKm: Double? = null,
    @SerializedName("distance_miles") val distanceMiles: Double? = null,
    @SerializedName("eta_minutes") val etaMinutes: Int? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("message") val message: String? = null
)


