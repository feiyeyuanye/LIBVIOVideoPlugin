package com.jhr.libvioplugin.util

import com.jhr.libvioplugin.danmaku.OyydsDanmakuApis
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

val retrofit = Retrofit.Builder()
    .client(OkHttpClient())
    .baseUrl("https://api.github.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val oyydsDanmakuApis by lazy(LazyThreadSafetyMode.NONE) { retrofit.create<OyydsDanmakuApis>() }