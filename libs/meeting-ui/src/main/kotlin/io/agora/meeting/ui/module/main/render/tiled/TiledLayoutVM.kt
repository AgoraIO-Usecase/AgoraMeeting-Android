package io.agora.meeting.ui.module.main.render.tiled

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.MediaContext
import io.agora.meeting.context.RenderContext
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import java.util.concurrent.Executors

class TiledLayoutVM(
        private val renderContext: RenderContext,
        private val mediaContext: MediaContext,
        private val usersContext: UsersContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadExecutor()

    val renderList = MutableLiveData<List<TiledRenderInfo>>()
    var statsDisplayEnable: Boolean = false
        set(value) {
            val oldField = field
            field = value
            if (!value && oldField) {
                renderContext.disableAllRenderStats()
            }
        }

    private val renderEventHandler = object : RenderContext.RenderEventHandler {
        override fun onRenderInfoAdd(renders: List<RenderInfo>) {
            workerThread.execute {
                val sort = sort(mutableListOf<TiledRenderInfo>().apply {
                    renderList.value?.let { addAll(it) }
                    addAll(renders.map { TiledRenderInfo(it, judgeIsTopWhenAdd(it), Int.MAX_VALUE) })
                })
                renderList.postValue(sort)
            }
        }

        override fun onRenderInfoRemoved(renders: List<RenderInfo>) {
            renderList.value?.let { list ->
                workerThread.execute {
                    list.filter { !renders.map { it.streamId }.contains(it.renderInfo.streamId) }.let {
                        renderList.postValue(it)
                    }
                }
            }
        }

        override fun onRenderInfoUpdate(renders: List<RenderInfo>) {
            renderList.value?.let {
                workerThread.execute {
                    val sort = sort(
                            mutableListOf<TiledRenderInfo>().apply {
                                addAll(it.map { tri ->
                                    val ri = renders.find { it.streamId == tri.renderInfo.streamId }
                                    if (ri != null) {
                                        TiledRenderInfo(ri, tri.isTop, tri.index)
                                    } else {
                                        tri
                                    }
                                })
                            }
                    )
                    renderList.postValue(sort)
                }
            }
        }

    }

    init {
        renderContext.registerEventHandler(renderEventHandler)

        workerThread.execute {
            val sort = sort(renderContext.getRenderInfoList().map {
                return@map TiledRenderInfo(it, judgeIsTopWhenAdd(it), Int.MAX_VALUE)
            })
            renderList.postValue(sort)
        }
    }

    fun setTop(index: Int, isTop: Boolean) {
        workerThread.execute {
            renderList.value?.let {
                val list = mutableListOf<TiledRenderInfo>().apply { addAll(it) }
                list.getOrNull(index)?.let {
                    list[index] = TiledRenderInfo(it.renderInfo, isTop, if (isTop) -1 else Int.MAX_VALUE)
                    val sort = sort(list)
                    renderList.postValue(sort)
                }
            }
        }
    }

    private fun judgeIsTopWhenAdd(info: RenderInfo) = info.isMe || (info.userInfo.userRole == UserRole.host && renderList.value?.isEmpty() ?: true)

    private fun sort(list: List<TiledRenderInfo>): List<TiledRenderInfo> {
        val sortedList = mutableListOf<TiledRenderInfo>().apply {
            addAll(list.filter { it.renderInfo.type == RenderInfoType.Media })
        }
        sortedList.sortWith(Comparator { e1: TiledRenderInfo, e2: TiledRenderInfo ->
            if (e1.isTop && e2.isTop) {
                return@Comparator e1.index - e2.index
            }
            if (e1.isTop) {
                return@Comparator -1
            } else if (e2.isTop) {
                return@Comparator 1
            }
            e1.index - e2.index
        })
        for (i in sortedList.indices) {
            sortedList[i] = TiledRenderInfo(sortedList[i].renderInfo, sortedList[i].isTop, i)
        }
        return sortedList
    }

    override fun onCleared() {
        super.onCleared()
        renderContext.unRegisterEventHandler(renderEventHandler)
        workerThread.shutdownNow()
    }

    fun dealUserOperation(renderInfo: RenderInfo, userOperation: UserOperation) {
        usersContext.dealUserOperation(renderInfo.userInfo.userId, userOperation, failure = { failureEvent.postValue(Event(it)) })
    }

    fun subscriptVideo(userId: String, streamId: String, view: Any, highStream: Boolean) {
        mediaContext.subscriptVideo(userId, streamId, view, RenderMode.HIDDEN, highStream)
    }

    fun unSubscriptVideo(streamId: String) {
        mediaContext.unSubscriptVideo(streamId)
    }

    fun createTextureView(context: Any): Any? {
        return mediaContext.createTextureView(context)
    }

    fun enableStats(renderId: Int, enable: Boolean) {
        renderContext.enableRenderStats(renderId, enable)
    }

    fun isStatsEnable(renderId: Int) = renderContext.isRenderStatsEnable(renderId)


    data class TiledRenderInfo(
            val renderInfo: RenderInfo,
            val isTop: Boolean,
            val index: Int,
            val isVisible: Boolean = true
    )

}