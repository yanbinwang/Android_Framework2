package com.example.debugging.bean

data class ExtraInput(
    val describe: String? = null,
    val onInput: ((String) -> Unit)? = null,
    val nowValue: (() -> String)? = null,
    val defaultValue: (() -> String)? = null,
    val type: Int? = null
)