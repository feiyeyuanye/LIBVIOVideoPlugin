package com.jhr.plugin.components

import android.util.Log
import com.jhr.plugin.components.Const.host
import com.jhr.plugin.components.Const.ua
import com.jhr.plugin.danmaku.OyydsDanmaku
import com.jhr.plugin.danmaku.OyydsDanmakuParser
import com.jhr.plugin.util.JsoupUtil
import com.jhr.plugin.util.Text.trimAll
import com.jhr.plugin.util.oyydsDanmakuApis
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.data.VideoPlayMedia
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import kotlinx.coroutines.*

class VideoPlayPageDataComponent : IVideoPlayPageDataComponent {

    private var episodeDanmakuId = ""
    override suspend fun getDanmakuData(
        videoName: String,
        episodeName: String,
        episodeUrl: String
    ): List<DanmakuItemData>? {
        try {
            val config = PluginPreferenceIns.get(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, true)
            if (!config)
                return null
            val name = videoName.trimAll()
            var episode = episodeName.trimAll()
            //剧集对集去除所有额外字符，增大弹幕适应性
            val episodeIndex = episode.indexOf("集")
            if (episodeIndex > -1 && episodeIndex != episode.length - 1) {
                episode = episode.substring(0, episodeIndex + 1)
            }
            Log.d("请求Oyyds弹幕", "媒体:$name 剧集:$episode")
            return oyydsDanmakuApis.getDanmakuData(name, episode).data.let { danmukuData ->
                val data = mutableListOf<DanmakuItemData>()
                danmukuData?.data?.forEach { dataX ->
                    OyydsDanmakuParser.convert(dataX)?.also { data.add(it) }
                }
                episodeDanmakuId = danmukuData?.episode?.id ?: ""
                data
            }
        } catch (e: Exception) {
            throw RuntimeException("弹幕加载错误：${e.message}")
        }
    }

    override suspend fun putDanmaku(
        videoName: String,
        episodeName: String,
        episodeUrl: String,
        danmaku: String,
        time: Long,
        color: Int,
        type: Int
    ): Boolean = try {
        Log.d("发送弹幕到Oyyds", "内容:$danmaku 剧集id:$episodeDanmakuId")
        oyydsDanmakuApis.addDanmaku(
            danmaku,
            //Oyyds弹幕标准时间是秒
            (time / 1000F).toString(),
            episodeDanmakuId,
            OyydsDanmakuParser.danmakuTypeMap.entries.find { it.value == type }?.key ?: "scroll",
            String.format("#%02X", color)
        )
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    /**
     * bug -> 标题会显示上一个视频的标题
     */
    override suspend fun getVideoPlayMedia(episodeUrl: String): VideoPlayMedia {
        val url = host + episodeUrl
        val document = JsoupUtil.getDocument(url)
        Log.e("TAG", url)
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        //解析链接
        val videoUrl = withContext(Dispatchers.Main) {
            val iframeUrl = withTimeoutOrNull(10 * 1000) {
                WebUtilIns.interceptResource(
                    url, ".*\\b(mp4|m3u8)\\b.*",
                    loadPolicy = object : WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                        override val headers = cookies
                        override val userAgentString = ua
                        override val isClearEnv = false
                    }
                )
            } ?: ""
            async {
                Log.e("TAG", iframeUrl)
                when {
                    iframeUrl.isBlank() -> iframeUrl
//                    iframeUrl.contains("url=") -> iframeUrl.substringAfter("url=")
//                        .substringBefore("&next=").urlDecode()
                    else -> { iframeUrl }
                }
            }
        }
        //剧集名
        val name = withContext(Dispatchers.Default) {
            async {
                document.select("title").text()
            }
        }
        Log.e("TAG", "解析后name："+name.await())
        Log.e("TAG", "解析后url："+videoUrl.await())
        return VideoPlayMedia(name.await(), videoUrl.await() as String)
    }
}