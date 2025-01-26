package com.example.futmax2.network

data class ValidateUserResponse(
    val success: Boolean,
    val exists: Boolean,
    val error: String?
)
