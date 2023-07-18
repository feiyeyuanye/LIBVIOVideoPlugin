package com.jhr.libvioplugin.util

object Text {
    private val trimRegex = Regex("\\s+")
    fun String.trimAll() = trimRegex.replace(this, "")
}