package io.agora.meeting.sdk

import android.content.Context
import android.os.Bundle
import io.agora.meeting.context.ContextPool
import io.agora.meeting.context.OnExitListener
import io.agora.meeting.context.Releasable
import io.agora.meeting.context.bean.DeviceNetQuality
import io.agora.meeting.context.bean.LaunchConfig
import io.agora.meeting.context.bean.MeetingConfig
import io.agora.meeting.core.MeetingCore
import io.agora.meeting.core.annotaion.Keep
import io.agora.meeting.ui.MeetingUi

class MeetingSDK(
        private val context: Context,
        private val config: MeetingConfig,
        private val params: Map<String, String>? = null
){

    companion object {
        const val CORE_VERSION_CODE = MeetingCore.VERSION_CODE
        const val CORE_VERSION_NAME = MeetingCore.VERSION_NAME
        const val CORE_RTC_VERSION = MeetingCore.RTC_VERSION
        const val CORE_RTM_VERSION = MeetingCore.RTM_VERSION
        const val CORE_WHITEBOARD_VERSION = MeetingCore.WHITEBOARD_VERSION
    }

    private val meetingCore = MeetingCore(context.applicationContext, config, params)
    private val meetingUi = MeetingUi(context.applicationContext)

    fun launch(launchConfig: LaunchConfig,
               success: ((ContextPool) -> Unit)? = null,
               failure: ((Throwable) -> Unit)? = null,
               targetAty: Class<*>? = null,
               extras: Bundle? = null
    ) {

        meetingCore.launch(launchConfig, { cp ->
            // launch meeting ui root activity
            meetingUi.launch(context, cp, targetAty,
                    extras = extras,
                    success = {
                        success?.invoke(cp)
                    },
                    failure = {
                        failure?.invoke(it)
                    })
        }, {
            // error
            failure?.invoke(it)
        })
    }


    fun enableNetQualityCheck(listener: (DeviceNetQuality) -> Unit, failure: (Throwable) -> Unit) {
        meetingCore.enableNetQualityCheck(listener, failure)
    }

    /**
     * 关闭网络探测
     */
    fun disableNetQualityCheck() {
        meetingCore.disableNetQualityCheck()
    }

    fun setOnExitListener(listener: OnExitListener) {
        meetingCore.setOnExitListener(listener)
    }

    fun release() {
        Releasable.release(meetingUi)
        Releasable.release(meetingCore)
    }

}