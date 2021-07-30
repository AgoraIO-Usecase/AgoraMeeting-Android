package io.agora.meeting

import android.app.Application
import android.content.Context
import android.os.Process
import android.text.TextUtils
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import io.agora.meeting.context.bean.MeetingConfig
import io.agora.meeting.sdk.MeetingSDK
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class MainApplication : Application() {

    private lateinit var meetingSDK: MeetingSDK

    override fun onCreate() {
        super.onCreate()
        meetingSDK = MeetingSDK(
                this,
                MeetingConfig(getString(R.string.agora_app_id), logAll = true),
                mapOf(Pair("host", getString(R.string.agora_app_host))))
        initBugly()
    }

    private fun initBugly() {
        if (TextUtils.isEmpty(getBuglyAppId())) {
            return
        }
        val context = applicationContext
        // 获取当前包名
        val packageName = context.packageName
        // 获取当前进程名
        val processName = getProcessName(Process.myPid())
        // 设置是否为上报进程
        val strategy = UserStrategy(context)
        strategy.isUploadProcess = processName == null || processName == packageName
        // 初始化Bugly
        CrashReport.initCrashReport(context, getBuglyAppId(), BuildConfig.DEBUG, strategy)
        // 设置标签
        CrashReport.setUserSceneTag(context, if (BuildConfig.DEBUG) 999 else 0)
    }

    private fun getBuglyAppId() = getString(R.string.bugly_app_id)

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName: String = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                if (reader != null) {
                    reader.close()
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
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