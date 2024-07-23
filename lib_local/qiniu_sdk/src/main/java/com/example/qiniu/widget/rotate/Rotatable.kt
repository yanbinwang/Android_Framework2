package com.example.qiniu.widget.rotate

interface Rotatable {
    // Set parameter 'animation' to true to have animation when rotation.
    fun setOrientation(orientation: Int, animation: Boolean)
}