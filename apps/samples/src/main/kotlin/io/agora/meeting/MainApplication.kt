package io.agora.meeting

import android.app.Application
import android.content.Context
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.model.ExampleBean
import io.agora.meeting.common.model.Examples
import io.agora.meeting.context.bean.MeetingConfig
import io.agora.meeting.sdk.MeetingSDK
import io.agora.meeting.utils.ClassUtils

class MainApplication : Application() {

    private val meetingSDK by lazy {
        MeetingSDK(this,
                MeetingConfig(getString(R.string.agora_app_id), logAll = true),
                mapOf())
    }

    override fun onCreate() {
        super.onCreate()
        initExamples()
    }

    private fun initExamples() {
        try {
            val packageName = ClassUtils.getFileNameByPackageName(this, "io.agora.meeting.example")
                    ?: return
            for (name in packageName) {
                val aClass = Class.forName(name)
                val declaredAnnotations = aClass.annotations
                for (annotation in declaredAnnotations) {
                    if (annotation is Example) {
                        Examples.addItem(ExampleBean(annotation.index, annotation.group, aClass, annotation.nameStrId, annotation.tipStrId))
                    }
                }
            }
            Examples.sortItem()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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