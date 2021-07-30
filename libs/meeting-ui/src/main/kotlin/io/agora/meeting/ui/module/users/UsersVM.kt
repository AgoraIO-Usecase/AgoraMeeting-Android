package io.agora.meeting.ui.module.users

import android.text.TextUtils
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.UserDetailInfo
import io.agora.meeting.context.bean.UserOperation
import io.agora.meeting.context.bean.UserRole
import io.agora.meeting.ui.base.BaseViewModel
import io.agora.meeting.ui.base.Event
import java.util.concurrent.Executors

class UsersVM(
        private val usersContext: UsersContext
) : BaseViewModel() {
    private val workerExecutor = Executors.newSingleThreadExecutor()

    val memberNum = MutableLiveData<Int>(usersContext.getUserInfoList().size)
    val userInfoList = MutableLiveData<List<UserDetailInfo>>()

    private var userNameFilter = ""

    private val usersEventHandler = object : UsersContext.UsersEventHandler {

        override fun onUserListChanged(userInfoList: List<UserDetailInfo>) {
            memberNum.postValue(userInfoList.size)
            workerExecutor.submit {
                sortAndFilter()
            }
        }

        override fun onKickedOut() {

        }
    }

    init {
        usersContext.registerEventHandler(usersEventHandler)

        workerExecutor.submit {
            sortAndFilter()
        }
    }

    override fun onCleared() {
        super.onCleared()
        workerExecutor.shutdownNow()
        usersContext.unRegisterEventHandler(usersEventHandler)
    }

    fun handleUserOperation(userInfo: UserDetailInfo, operation: UserOperation) {
        usersContext.dealUserOperation(userInfo.userId, operation, failure = { failureEvent.postValue(Event(it)) })
    }

    fun setFilterName(filter: String) {
        if (userNameFilter == filter) {
            return
        }
        userNameFilter = filter
        workerExecutor.submit {
            sortAndFilter()
        }
    }

    @WorkerThread
    private fun sortAndFilter() {
        val list = mutableListOf<UserDetailInfo>()

        var me: UserDetailInfo? = null
        val hosts = mutableListOf<UserDetailInfo>()
        val others = mutableListOf<UserDetailInfo>()
        val _userNameFilter: String = userNameFilter

        val copy = ArrayList(usersContext.getUserInfoList())
        for (m in copy) {
            if (TextUtils.isEmpty(_userNameFilter) || m.userName.contains(_userNameFilter)) {
                if (m.isMe) {
                    me = m
                } else if (m.userRole == UserRole.host) {
                    hosts.add(m)
                } else {
                    others.add(m)
                }
            }
        }
        if (me != null) list.add(me)
        list.addAll(hosts)
        list.addAll(others)
        userInfoList.postValue(list)
    }

}