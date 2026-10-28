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

