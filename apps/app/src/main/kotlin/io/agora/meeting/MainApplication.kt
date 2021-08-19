package io.agora.meeting

import android.app.Application
import android.content.Context
import io.agora.meeting.context.bean.MeetingConfig
import io.agora.meeting.sdk.MeetingSDK


class MainApplication : Application() {

    private lateinit var meetingSDK: MeetingSDK

    override fun onCreate() {
        super.onCreate()
        meetingSDK = MeetingSDK(
                this,
                MeetingConfig(getString(R.string.agora_app_id), logAll = true))
    }


    companion object {

        @JvmStatic
        fun getMeetingSDK(context: Context): MeetingSDK {
            val applicationContext = context.applicationContext
            if (applicationContext is MainApplication) {
                return applicationContext.meetingSDK
            }
            throw IllegalArgumentException("the application of context is not MainApplication")
        }
    }

}