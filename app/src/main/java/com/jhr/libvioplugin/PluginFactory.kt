package com.jhr.libvioplugin

import com.jhr.libvioplugin.components.Const
import com.jhr.libvioplugin.components.HomePageDataComponent
import com.jhr.libvioplugin.components.KuaKePageDataComponent
import com.jhr.libvioplugin.components.MediaClassifyPageDataComponent
import com.jhr.libvioplugin.components.MediaDetailPageDataComponent
import com.jhr.libvioplugin.components.MediaSearchPageDataComponent
import com.jhr.libvioplugin.components.MediaUpdateDataComponent
import com.jhr.libvioplugin.components.VideoPlayPageDataComponent
import com.su.mediabox.pluginapi.IPluginFactory
import com.su.mediabox.pluginapi.components.IBasePageDataComponent
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaUpdateDataComponent
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent

/**
 * 每个插件必须实现本类
 *
 * 注意包和类名都要相同，且必须提供公开的无参数构造方法
 */
class PluginFactory : IPluginFactory() {

    override val host: String = Const.host

    override fun <T : IBasePageDataComponent> createComponent(clazz: Class<T>) = when (clazz) {
        IHomePageDataComponent::class.java -> HomePageDataComponent()  // 主页
        IMediaSearchPageDataComponent::class.java -> MediaSearchPageDataComponent()  // 搜索
        IMediaDetailPageDataComponent::class.java -> MediaDetailPageDataComponent()  // 详情
        IMediaClassifyPageDataComponent::class.java -> MediaClassifyPageDataComponent()  // 媒体分类
        IMediaUpdateDataComponent::class.java -> MediaUpdateDataComponent
        IVideoPlayPageDataComponent::class.java -> VideoPlayPageDataComponent() // 视频播放
        //自定义页面，需要使用具体类而不是它的基类（接口）
        KuaKePageDataComponent::class.java -> KuaKePageDataComponent()  // 夸克网盘
        else -> null
    } as? T

}