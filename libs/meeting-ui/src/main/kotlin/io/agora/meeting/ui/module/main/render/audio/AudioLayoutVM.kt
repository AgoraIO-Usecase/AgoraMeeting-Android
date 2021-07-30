package io.agora.meeting.ui.module.main.render.audio

import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.RenderContext
import io.agora.meeting.context.bean.RenderInfo
import io.agora.meeting.context.bean.RenderInfoType
import io.agora.meeting.ui.base.BaseViewModel
import java.util.concurrent.Executors
import kotlin.math.ceil
import kotlin.math.min

class AudioLayoutVM(
        private val renderContext: RenderContext
) : BaseViewModel() {

    private val workerThread = Executors.newSingleThreadExecutor()

    val renderInfoList = MutableLiveData<List<RenderInfo>>()

    private val renderEventHandler = object : RenderContext.RenderEventHandler {
        override fun onRenderInfoAdd(renders: List<RenderInfo>) {
            workerThread.execute {
                renderInfoList.postValue(transform(renderContext.getRenderInfoList()))
            }
        }

        override fun onRenderInfoRemoved(renders: List<RenderInfo>) {
            workerThread.execute {
                renderInfoList.postValue(transform(renderContext.getRenderInfoList()))
            }
        }

        override fun onRenderInfoUpdate(renders: List<RenderInfo>) {
            workerThread.execute {
                renderInfoList.postValue(transform(renderContext.getRenderInfoList()))
            }
        }

    }

    init {
        renderContext.registerEventHandler(renderEventHandler)

        val _renderInfoList = renderContext.getRenderInfoList()
        if (_renderInfoList.isNotEmpty()) {
            workerThread.execute {
                renderInfoList.postValue(transform(_renderInfoList))
            }
        }
    }


    private fun transform(data: List<RenderInfo>): List<RenderInfo> {
        return data.filter { it.type == RenderInfoType.Media }
    }


    override fun onCleared() {
        super.onCleared()
        renderContext.unRegisterEventHandler(renderEventHandler)
        workerThread.shutdownNow()
    }

}