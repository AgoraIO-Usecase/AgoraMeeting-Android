package io.agora.meeting.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    val failureEvent = MutableLiveData<Event<Throwable>>()
    val loadingEvent = MutableLiveData<Event<Boolean>>()

}