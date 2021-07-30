package io.agora.meeting.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import io.agora.meeting.context.ContextPool
import io.agora.meeting.context.Releasable
import io.agora.meeting.ui.framework.RootActivity
import io.agora.meeting.ui.module.VMFactory
import io.agora.meeting.ui.util.Utils

class MeetingUi(private val context: Context) : Releasable{

    companion object{
        var globalVMFactory: VMFactory? = null
            private set

        var utilsInitialized = false
            private set
    }

    private var targetAty: Class<*>? = null

    private val atyLifecycleCallback = object : Application.ActivityLifecycleCallbacks{
        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityDestroyed(activity: Activity) {
            if(targetAty?.isInstance(activity) == true){
                reset()
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityResumed(activity: Activity) {}
    }

    init {
        if (!utilsInitialized) {
            utilsInitialized = true
            Utils.init(context.applicationContext)
        }
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(atyLifecycleCallback)
    }

    fun launch(context: Context, contextPool: ContextPool, targetAty: Class<*>? = null, extras: Bundle? = null, success: ()-> Unit, failure: (Throwable)->Unit) {
        if (globalVMFactory != null) {
            failure.invoke(IllegalStateException("MeetingUi has launched"))
            return
        }
        this.targetAty = targetAty ?: RootActivity::class.java
        globalVMFactory = VMFactory(contextPool)
        Intent(context, this.targetAty).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            extras?.let {
                putExtras(it)
            }
            try {
                context.startActivity(this)
                success.invoke()
            } catch (e: Exception) {
                failure.invoke(e)
                reset()
            }
        }
    }

    private fun reset() {
        globalVMFactory?.getContextPool()?.roomContext?.leaveRoom()
        globalVMFactory?.release()
        globalVMFactory = null
        targetAty = null
    }

    override fun release() {
        reset()
        (context.applicationContext as? Application)?.unregisterActivityLifecycleCallbacks(atyLifecycleCallback)
    }


}