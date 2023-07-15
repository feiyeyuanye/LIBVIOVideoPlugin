package com.jhr.plugin.danmaku

import com.jhr.plugin.danmaku.OyydsDanmaku
import retrofit2.http.*

interface OyydsDanmakuApis {

    @Headers("user-agent: MediaBox/SakuraAnime3Plugin")
    @GET("https://api.danmu.oyyds.top/api/message/getSomeV3?type=1&platforms=base,bilibili,dandan")
    suspend fun getDanmakuData(
        @Query("keyword") keyword: String,
        @Query("number") number: String
    ): OyydsDanmaku

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("https://api.danmu.oyyds.top/api/message/addOne")
    suspend fun addDanmaku(
        @Field("content") content: String,
        @Field("time") time: String,
        @Field("episodeId") episodeId: String,
        @Field("type") type: String,
        @Field("color") color: String
    )

}