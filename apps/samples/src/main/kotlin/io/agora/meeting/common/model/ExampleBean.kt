package io.agora.meeting.common.model

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExampleBean(
        var index: Int,
        val group: String,
        val clazz: Class<*>,
        @StringRes val nameStrId: Int,
        @StringRes val tipStrId : Int
): Parcelable