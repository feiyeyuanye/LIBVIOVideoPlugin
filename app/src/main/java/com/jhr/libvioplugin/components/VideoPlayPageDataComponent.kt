package com.jhr.libvioplugin.components

import android.util.Log
import com.jhr.libvioplugin.components.Const.host
import com.jhr.libvioplugin.components.Const.ua
import com.jhr.libvioplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.data.VideoPlayMedia
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import kotlinx.coroutines.*

class VideoPlayPageDataComponent : IVideoPlayPageDataComponent {

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