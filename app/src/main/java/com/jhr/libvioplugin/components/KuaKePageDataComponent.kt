package com.jhr.libvioplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import com.jhr.libvioplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.WebBrowserAction
import com.su.mediabox.pluginapi.components.ICustomPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.SimpleTextData
import com.su.mediabox.pluginapi.util.UIUtil.dp

/**
 * FileName: KuaKePageDataComponent
 * Founder: Jiang Houren
 * Create Date: 2023/7/4 10:44
 * Profile: 夸克网盘
 */
class KuaKePageDataComponent : ICustomPageDataComponent {

    var hostUrl = Const.host

    override val pageName: String
        get() = "夸克网盘"

    override fun initPage(action: CustomPageAction) {
        super.initPage(action)
        hostUrl += action.extraData
    }

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        Log.e("TAG", hostUrl)
        val data = mutableListOf<BaseData>()
        val document = JsoupUtil.getDocument(hostUrl)

        val title = document.select(".stui-player__detail").select(".title").text()
        data.add(SimpleTextData(title).apply {
            fontSize = 15F
            fontStyle = Typeface.BOLD
            fontColor = Color.BLACK
            layoutConfig = BaseData.LayoutConfig(Const.layoutSpanCount, 14.dp)
            spanSize = Const.layoutSpanCount
        })
        val info = document.select(".stui-player__detail").select(".data").text()
        data.add(SimpleTextData(info).apply {
            fontSize = 15F
            fontStyle = Typeface.BOLD
            fontColor = Color.BLACK
            spanSize = Const.layoutSpanCount
        })
        val scriptElements = document.select(".stui-player__video").select("script")
        for (script in scriptElements) {
            // 找到包含目标数据的<script>标签
            val scriptText = script.html()
            // var player_aaaa={"flag":"play","encrypt":3,"trysee":10,"points":0,"link":"\/play\/714890023-1-1.html","link_next":"","link_pre":"","url":"https:\/\/pan.quark.cn\/s\/562fc634b575","url_next":"","from":"kuake","server":"no","note":"","id":"714890023","sid":1,"nid":1}
//            Log.e("TAG","scriptText ${scriptText}")
            if (scriptText.isBlank()) break
            // 查找 "url" 字段的起始位置
            val startIndex = scriptText.indexOf("\"url\":\"") + 7
            // 查找 "url" 字段的结束位置
            val endIndex = scriptText.indexOf("\"", startIndex)
            // 提取子字符串
            val url = scriptText.substring(startIndex, endIndex)
            val cleanUrl = url.replace("\\", "") + "?entry=libvio&from=libvio"
            // https://pan.quark.cn/s/08ee6d810969?entry=libvio&from=libvio
//            Log.e("TAG","$cleanUrl")
            data.add(SimpleTextData("夸克网盘：$cleanUrl").apply {
                fontSize = 15F
                fontStyle = Typeface.BOLD
                fontColor = Color.BLUE
                spanSize = Const.layoutSpanCount
            }.apply {
                action = WebBrowserAction.obtain(cleanUrl)
            })
        }
        data.add(SimpleTextData("推荐使用“夸克浏览器”打开，转存可【倍速观看】，5T免费空间！推荐使用！").apply {
            fontSize = 15F
            fontStyle = Typeface.BOLD
            fontColor = Color.BLACK
            spanSize = Const.layoutSpanCount
        })
        return data
    }
}