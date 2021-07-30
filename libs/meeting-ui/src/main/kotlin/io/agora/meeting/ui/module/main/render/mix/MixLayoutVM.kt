package io.agora.meeting.ui.module.main.render.mix

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.RenderContext
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.context.bean.RenderInfoType
import io.agora.meeting.ui.base.BaseViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MixLayoutVM(
        private val renderContext: RenderContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadScheduledExecutor()
    private var workerSchedule: ScheduledFuture<*>? = null

    private var screenRenderId: Int? = null
    private var boardRenderId: Int? = null
    private var videoRenderIds = hashSetOf<Int>()
    private var userLayoutType: MixLayoutType? = null

    val layoutInfo = MutableLiveData<MixLayoutInfo>()
    val renderCount = MutableLiveData(0)
    var statsDisplayEnable = MutableLiveData<Boolean>()

    private val renderEventHandler = object : RenderContext.RenderEventHandler {
        override fun onRenderInfoAdd(renders: List<RenderInfo>) {
            workerThread.execute {
                val list = renderContext.getRenderInfoList()
                renderCount.postValue(list.size)
                updateRenderLayout(list)
            }
        }

        override fun onRenderInfoRemoved(renders: List<RenderInfo>) {
            workerThread.execute {
                val list = renderContext.getRenderInfoList()
                renderCount.postValue(list.size)
                updateRenderLayout(list)
            }
        }

        override fun onRenderInfoUpdate(renders: List<RenderInfo>) {
            workerThread.execute {
                updateRenderLayout(renderContext.getRenderInfoList())
            }
        }

    }


    init {
        renderContext.registerEventHandler(renderEventHandler)

        workerThread.execute {
            val list = renderContext.getRenderInfoList()
            renderCount.postValue(list.size)
            updateRenderLayout(list)
        }

    }

    override fun onCleared() {
        super.onCleared()
        renderContext.unRegisterEventHandler(renderEventHandler)
        workerSchedule?.cancel(true)
        workerThread.shutdownNow()
    }

    fun changeLayout(type: MixLayoutType, renderInfo: RenderInfo? = null) {
        val hasVideo = videoRenderIds.size > 0
        val hasShareScreen = screenRenderId != null
        val hasShareBoard = boardRenderId != null

        if (type == MixLayoutType.Lecturer && (!hasVideo || renderInfo?.hasVideo != true)) {
            return
        }

        if (type == MixLayoutType.Tiled && (hasShareScreen || hasShareBoard)) {
            return
        }

        if (type == MixLayoutType.Audio && hasVideo) {
            return
        }

        if (type == layoutInfo.value?.type) {
            return
        }

        userLayoutType = type
        workerSchedule?.cancel(true)
        updateRendersByType(type, renderInfo)
    }


    private fun updateRenderLayout(list: List<RenderInfo>) {
        if (list.isEmpty()) return

        screenRenderId = null
        boardRenderId = null
        videoRenderIds.clear()

        // screen share
        list.find { it.type == RenderInfoType.ScreenSharing }?.let {
            screenRenderId = it.id
        }

        // board share
        list.find { it.type == RenderInfoType.Board }?.let {
            boardRenderId = it.id
        }

        // video
        list.filter { it.hasVideo }.let {
            it.forEach {
                videoRenderIds.add(it.id)
            }
        }

        // determine target layout type
        val hasVideo = videoRenderIds.size > 0
        val hasShareScreen = screenRenderId != null
        val hasShareBoard = boardRenderId != null

        val currentLayout = layoutInfo.value?.type ?: MixLayoutType.Tiled
        var layoutType = MixLayoutType.Tiled
        if (!hasVideo) {
            layoutType = MixLayoutType.Audio
        }
        if (hasShareScreen || hasShareBoard) {
            layoutType = MixLayoutType.Lecturer
        }
        workerSchedule?.cancel(true)

        if (layoutType == MixLayoutType.Audio
                && currentLayout != MixLayoutType.Audio
                && currentLayout != MixLayoutType.Lecturer) {
            updateRendersByType(MixLayoutType.Tiled)
            updateRendersByType(MixLayoutType.Audio, delayS = 6)
        } else if (layoutType == MixLayoutType.Tiled && userLayoutType != null) {
            updateRendersByType(userLayoutType!!)
        } else {
            updateRendersByType(layoutType)
        }

    }

    private fun updateRendersByType(layout: MixLayoutType, renderInfo: RenderInfo? = null, delayS: Long = 0) {
        if (layoutInfo.value?.type != layout) {
            workerSchedule = workerThread.schedule({
                layoutInfo.postValue(MixLayoutInfo(layout, renderInfo))
            }, delayS, TimeUnit.SECONDS)
        }
    }

    data class MixLayoutInfo(
            val type: MixLayoutType,
            val renderInfo: RenderInfo? = null
    )


    enum class MixLayoutType {
        Tiled, Lecturer, Audio
    }
}