package com.jhr.plugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.plugin.components.Const.host
import com.jhr.plugin.components.Const.layoutSpanCount
import com.jhr.plugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()

        //3.各类推荐
        val heads = doc.select(".stui-pannel__bd").select(".stui-vodlist__head")
        val uls = doc.select(".stui-pannel__bd").select("ul")
        for ((index,em) in uls.withIndex()){
            var typeName = ""
            var typeUrl = ""
            if (index == 0){
                typeName = "推荐"
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
            }else{
                val moduleHeading = heads[index-1].select("h3")
                typeName = moduleHeading.text()
                typeUrl = moduleHeading.select("a").attr("href")
                if (typeName == "已下架") break
                if (!typeName.isNullOrBlank()) {
                    data.add(SimpleTextData(typeName).apply {
                        fontSize = 15F
                        fontStyle = Typeface.BOLD
                        fontColor = Color.BLACK
                        spanSize = layoutSpanCount / 2
                    })
                    data.add(SimpleTextData("查看更多 >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = layoutSpanCount / 2
                    }.apply {
                        action = ClassifyAction.obtain(typeUrl, typeName)
                    })
                }
            }
            val li = em.select("li")
            for (video in li){
                video.apply {
                    val name = select("a").attr("title")
                    val videoUrl = select("a").attr("href")
                    val coverUrl = select("a").attr("data-original")
                    val episode = select(".text-right").text()

                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                         data.add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    spanSize = layoutSpanCount / 3
                                    action = DetailAction.obtain(videoUrl)
                                })
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    }
                }
            }
        }
        return data
    }
}