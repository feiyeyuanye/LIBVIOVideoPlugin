package com.jhr.libvioplugin.components

import android.util.Log
import com.jhr.libvioplugin.util.JsoupUtil
import com.jhr.libvioplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup
import java.lang.StringBuilder

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/show/4--------1---.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify ${classify}")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select(".stui-screen").select(".item").select("ul").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")

        val str = classifyAction.url?.urlDecode() ?: ""
        val charToInsert = "$page"
        var indexToInsert = str.length - 8
        // 时间选项会插入到字符串末尾
        // https://www.libvio.me/show/1--time------2---2023.html
        if (str[indexToInsert] != '-') {
            indexToInsert -= 4
        }
        var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }
        classify = url

        Log.e("TAG", "获取分类数据 $url")

        val document = JsoupUtil.getDocument(url)
        classifyList.addAll(ParseHtmlUtil.parseClassifyEm(document, url))
        return classifyList
    }
}