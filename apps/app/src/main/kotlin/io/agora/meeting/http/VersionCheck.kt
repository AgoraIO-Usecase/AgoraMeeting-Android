package io.agora.meeting.http

import android.text.TextUtils
import android.util.Log
import io.agora.meeting.BuildConfig
import io.agora.meeting.http.been.AppVersionResp
import io.agora.meeting.ui.util.CryptoUtil
import io.agora.rtmtoken.RtmTokenBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class VersionCheck(private val appId: String, private val appCer: String) {
    private val systemService by lazy {
        Retrofit.Builder()
                .client(OkHttpClient.Builder()
                        .addInterceptor(object : Interceptor {
                            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                                val uid = CryptoUtil.md5("111")
                                return chain.proceed(chain.request().newBuilder()
                                        .addHeader("x-agora-token", RtmTokenBuilder().buildToken(appId, appCer, uid, RtmTokenBuilder.Role.Rtm_User, 0))
                                        .addHeader("x-agora-uid", uid)
                                        .build()
                                )
                            }
                        })
                        .addInterceptor(HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger{
                            override fun log(message: String) {
                                Log.d("VersionCheck", message)
                            }
                        }).setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build())
                .baseUrl("https://api.agora.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(SystemService::class.java)
    }

    fun check(success: (String) -> Unit, failure: (Throwable) -> Unit) {
        systemService.checkVersion(
                appId,
                2, 1, BuildConfig.VERSION_NAME
        ).enqueue(object : Callback<AppVersionResp> {
            override fun onFailure(call: Call<AppVersionResp>, t: Throwable) {
                failure.invoke(t)
            }

            override fun onResponse(call: Call<AppVersionResp>, response: Response<AppVersionResp>) {
                if (response.isSuccessful) {
                    val body = response.body()?.data ?: return
                    if (TextUtils.isEmpty(body.upgradeUrl)) {
                        failure.invoke(IllegalStateException("the version(${BuildConfig.VERSION_NAME}) is already up to date"))
                    } else {
                        success.invoke(body.upgradeUrl)
                    }
                } else {
                    failure.invoke(HttpException(response))
                }
            }
        })
    }
}