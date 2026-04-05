package com.solarsensear.data.models

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
