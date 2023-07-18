package com.jhr.libvioplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.jhr.libvioplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        var score = -1F
        // 主演
        var protagonist = ""
        // 地区 语言
        var animeLanguage = ""
        // 更新时间
        var time = ""
        var upState = ""
        // 上映
        var show = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = document.select("a[class='pic']").select("img").attr("data-original")
        title = document.select("a[class='pic']").attr("title")
        // 更新状况
        val upStateItems = document.select(".stui-content__detail").select("p")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            when{
                t.contains("类型：") -> {
                    t.split("/").forEach{
                        if (it.contains("地区：")){
                            animeLanguage += it
                        }else if (it.contains("上映：")){
                            show = it
                        }else if (it.contains("年份：")){
                            animeLanguage += " | " + it
                        }else if (it.contains("类型：")){
                            //类型
                            it.substringAfter("类型：").split(",").forEach{
                                tags.add(TagData(it).apply {
                                    action = ClassifyAction.obtain("", "", it)
                                })
                            }
                        }
                    }
                }
                t.contains("主演：") -> protagonist = t
                t.contains("总集数：") -> upState = t
                t.contains("最后更新：") -> time = t
                t.contains("简介：") -> desc = t
                t.contains("评分：") -> {
                    score = t.substringAfter("评分：").substringBefore("分").toFloatOrNull() ?: -1F
                }
            }
        }

        // ---------------------------------- 播放列表+header
        val module = document.select(".stui-pannel__bd").select(".stui-vodlist__head")
        for (index in module) {
            when (val playName = index.select("h3").text()) {
                "猜你喜欢" -> {
                    val series = parseSeries(document)
                    if (series.isNotEmpty()) {
                        details.add(
                            SimpleTextData(playName).apply {
                                fontSize = 16F
                                fontColor = Color.WHITE
                            }
                        )
                        details.addAll(series)
                    }
                }

                else -> {
                    val playEpisode = index.select("ul")
                    val episodes = parseEpisodes(playEpisode, playName)
                    if (episodes.isEmpty())
                        continue
                    details.add(
                        SimpleTextData(playName + "(${episodes.size}集)").apply {
                            fontSize = 16F
                            fontColor = Color.WHITE
                        }
                    )
                    details.add(EpisodeListData(episodes))
                }
            }
        }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$animeLanguage").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$show").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Elements,isKuaKe:String): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select("li").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    if(isKuaKe == "夸克网盘"){
                        action = CustomPageAction.obtain(KuaKePageDataComponent::class.java)
                        action?.extraData = episodeUrl
                    }else{
                        action = PlayAction.obtain(episodeUrl)
                    }
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Element): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results = element.select("ul[class='stui-vodlist clearfix']").select("li").select(".stui-vodlist__thumb")
        for (i in results.indices) {
            val cover = results[i].attr("data-original")
            val title = results[i].attr("title")
            val url = results[i].attr("href")
            val item = MediaInfo1Data(
                title, cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}