package io.agora.meeting.ui.module.main.render.lecturer

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.*
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.context.bean.RenderInfoType
import io.agora.meeting.context.bean.RenderMode
import io.agora.meeting.ui.base.BaseViewModel
import java.util.concurrent.Executors

class LecturerLayoutVM(
        private val renderContext: RenderContext,
        private val mediaContext: MediaContext,
        private val boardContext: BoardContext,
        private val screenContext: ScreenContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadExecutor()
    private var workerSubRenderList = listOf<RenderInfo>()

    val mainRender = MutableLiveData<RenderInfo>()
    val subRenderList = MutableLiveData<List<RenderInfo>>()
    val statsRenderInfo = MutableLiveData<RenderInfo>()

    var mainRenderId: Int = -1
        set(value) {
            field = value
            setMain(value)
        }

    var statsRenderId: Int = -1

    private val renderEventHandler = object : RenderContext.RenderEventHandler {
        override fun onRenderInfoAdd(renders: List<RenderInfo>) {
            workerThread.execute {
                addRenderList(renders)
            }
        }

        override fun onRenderInfoRemoved(renders: List<RenderInfo>) {
            workerThread.execute {
                removeRenderList(renders)
            }
        }

        override fun onRenderInfoUpdate(renders: List<RenderInfo>) {
            workerThread.execute {
                updateRenderList(renders)
            }
        }

    }

    init {
        renderContext.registerEventHandler(renderEventHandler)

        workerThread.execute {
            resetRenderList()
        }
    }

    fun getWhiteBoardView() = boardContext.getWhiteBoardView(false)

    fun setBoardWritable(writable: Boolean) {
        boardContext.setWritable(writable)
    }

    fun stopScreenShare() {
        screenContext.closeScreenSharing()
    }

    fun isSharing() = screenContext.isScreenSharing() or boardContext.isBoardSharing()

    private fun setMain(renderId: Int) {
        val mainStream = mainRender.value ?: return
        if (mainStream.id == renderId) {
            return
        }
        workerThread.execute {
            val newMainStream = workerSubRenderList.find { it.id == renderId } ?: return@execute
            val newSubStreams = workerSubRenderList.filter { it.id != renderId } ?: return@execute

            mainRender.postValue(newMainStream)
            workerSubRenderList = mutableListOf<RenderInfo>().apply {
                add(mainStream)
                addAll(newSubStreams)
            }
            subRenderList.postValue(workerSubRenderList)
        }
    }

    @WorkerThread
    private fun resetRenderList() {
        val list = renderContext.getRenderInfoList()
        if (list.isEmpty()) return

        var mainStream = list.find { it.type == RenderInfoType.ScreenSharing }
        if (mainStream == null) {
            mainStream = list.find { it.type == RenderInfoType.Board }
            if (mainStream == null) {
                mainStream = list.find { it.id == mainRenderId }
                if (mainStream == null) {
                    mainStream = list.find { it.isMe && it.hasVideo }
                    if (mainStream == null) {
                        mainStream = list.find { it.hasVideo }
                        if (mainStream == null) {
                            mainStream = list.getOrNull(0)
                        }
                    }
                }
            }
        }

        mainRender.postValue(mainStream)
        workerSubRenderList = list.filter { it.streamId != mainStream?.streamId }
        subRenderList.postValue(workerSubRenderList)
    }

    @WorkerThread
    private fun addRenderList(add: List<RenderInfo>) {
        if (add.isEmpty()) return

        var mainStream = mainRender.value
        if (mainStream == null) {
            resetRenderList()
            return
        }
        mainStream = add.find { it.type == RenderInfoType.ScreenSharing }
        if (mainStream != null) {
            resetRenderList()
            return
        }
        mainStream = add.find { it.type == RenderInfoType.Board }
        if (mainStream != null) {
            resetRenderList()
            return
        }

        val newList = mutableListOf<RenderInfo>()
        newList.addAll(workerSubRenderList)
        newList.addAll(add)
        workerSubRenderList = newList
        subRenderList.postValue(newList)
    }

    @WorkerThread
    private fun removeRenderList(remove: List<RenderInfo>) {
        if (remove.isEmpty()) return

        val mainStream = mainRender.value
        if (mainStream == null) {
            resetRenderList()
            return
        }

        val mainRemove = remove.find { it.streamId == mainStream.streamId }
        if (mainRemove != null) {
            resetRenderList()
            return
        }

        val removeIds = remove.map { it.streamId }
        workerSubRenderList = workerSubRenderList.filter {
            return@filter !removeIds.contains(it.streamId)
        }
        subRenderList.postValue(workerSubRenderList)
    }

    @WorkerThread
    private fun updateRenderList(update: List<RenderInfo>) {
        if (update.isEmpty()) return

        val mainStream = mainRender.value
        if (mainStream == null) {
            resetRenderList()
            return
        }

        val mainUpdate = update.find { it.streamId == mainStream.streamId }
        if (mainUpdate != null) {
            mainRender.postValue(mainUpdate)
        }

        val newList = mutableListOf<RenderInfo>()
        newList.addAll(workerSubRenderList)
        update.forEach { each ->
            newList.find { it.streamId == each.streamId }?.let {
                val index = newList.indexOf(it)
                newList[index] = each.copy()
            }
        }
        workerSubRenderList = newList
        subRenderList.postValue(newList)

        update.find { it.id == statsRenderId }?.let {
            this.statsRenderInfo.postValue(it)
        }
    }


    override fun onCleared() {
        super.onCleared()
        renderContext.unRegisterEventHandler(renderEventHandler)
        workerThread.shutdownNow()
    }

    fun createVideoTarget(viewGroup: Any, layoutParams: Any) = mediaContext.createVideoTarget(viewGroup, layoutParams)

    fun subscriptVideo(userId: String, streamId: String, target: VideoTarget, highStream: Boolean, renderMode: RenderMode) {
        mediaContext.subscriptVideo(userId, streamId, target,renderMode, highStream)
    }

    fun unSubscriptVideo(streamId: String){
        mediaContext.unSubscriptVideo(streamId)
    }

    fun enableStats(renderId: Int, enable: Boolean) {
        renderContext.enableRenderStats(renderId, enable)
    }

    fun disableAllStats() {
        renderContext.disableAllRenderStats()
    }

    fun isStatsEnable(renderId: Int) = renderContext.isRenderStatsEnable(renderId)

    fun subscriptAudio(streamId: String) {
        mediaContext.subscriptAudio(streamId)
    }

    fun unSubscriptAudio(streamId: String) {
        mediaContext.unSubscriptVideo(streamId)
    }
}