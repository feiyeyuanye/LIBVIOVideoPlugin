package com.jhr.plugin.components

import android.util.Log
import com.jhr.plugin.components.Const.host
import com.jhr.plugin.util.JsoupUtil
import com.jhr.plugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://libvio.me/search/%E9%BE%99----------2---.html
        val url = "${host}/search/${keyWord}----------${page}---.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}