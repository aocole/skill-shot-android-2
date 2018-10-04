package com.skillshot.android.api.model

data class Machine(
    val id: Int,
    val title: Title
)

data class Title(
    val id: Int,
    val name: String
)