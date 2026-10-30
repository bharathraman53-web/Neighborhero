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


