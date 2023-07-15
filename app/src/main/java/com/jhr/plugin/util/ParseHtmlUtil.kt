package com.jhr.plugin.util

import android.util.Log
import com.jhr.plugin.components.Const.host
import com.jhr.plugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    fun parseSearchEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val lpic = element.select("ul[class='stui-vodlist clearfix']")
        val results: Elements = lpic.select("li")
        for (i in results.indices) {
            val a = results[i].select(".stui-vodlist__thumb")
            var cover = a.attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = a.attr("title")
            val url = a.attr("href")
            val episode = results[i].select(".text-right").text()
            val describe = results[i].select(".pic-tag-top").text()
            val item = MediaInfo1Data(
                title, cover, host + url, episode
            ).apply {
                spanSize = (layoutSpanCount / 3)
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        videoInfoItemDataList[0].layoutConfig = BaseData.LayoutConfig(spanCount = layoutSpanCount)
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     * @param element ul的父元素
     */
    fun parseClassifyEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select("ul[class='stui-vodlist clearfix']").select("li")
        for (i in results.indices) {
            val title = results[i].select("a").attr("title")
            var cover = results[i].select("a").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val url = results[i].select("a").attr("href")
            val episode = results[i].select(".text-right").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        videoInfoItemDataList[0].layoutConfig = BaseData.LayoutConfig(spanCount = layoutSpanCount)
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        val li = element.select("li")
        var classifyCategory = ""
        for ((index,em) in li.withIndex()){
            if (index == 0) {
                classifyCategory = em.select("span").text()
                continue
            }
            val a = em.select("a")
            classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        a.attr("href").apply {
//                            Log.e("TAG", "分类链接 $this")
                        },
                        classifyCategory,
                        a.text()
                    )
                })
            }
        return classifyItemDataList
    }
}