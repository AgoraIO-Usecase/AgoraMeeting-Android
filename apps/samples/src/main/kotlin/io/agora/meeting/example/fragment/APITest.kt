package io.agora.meeting.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.agora.meeting.R
import io.agora.meeting.annotation.Example
import io.agora.meeting.common.BaseFragment
import io.agora.meeting.common.model.Examples
import io.agora.meeting.context.ContextPool
import io.agora.meeting.context.MessagesContext
import io.agora.meeting.context.RoomContext
import io.agora.meeting.context.UsersContext
import io.agora.meeting.context.bean.*
import io.agora.meeting.ui.MeetingUi
import io.agora.meeting.ui.util.ToastUtil

@Example(
        index = 10,
        group = Examples.GROUP_FRAGMENT,
        nameStrId = R.string.fragment_api_test,
        tipStrId = R.string.fragment_api_test_tip
)
class APITest: BaseFragment() {

    private var userTestIndex = 0
    private val userPropertiesTestList = listOf(
            mapOf(Pair("111", "222"), Pair("aaa", "bbb"), Pair("ccc", 111), Pair("666", 111.2), Pair("test", TestBean("2345"))),
            mapOf(Pair("333", "555"), Pair("ccc", "bbb")),
            mapOf(Pair("111", "333"), Pair("aaa", "ddd")),
    )

    private var roomTestIndex = 0
    private val roomPropertiesTestList = listOf(
            mapOf(Pair("111", "222"), Pair("aaa", "bbb")),
            mapOf(Pair("333", "555"), Pair("ccc", "bbb")),
            mapOf(Pair("111", "333"), Pair("aaa", "ddd")),
    )

    private var contextPool: ContextPool? = null

    private val roomEventHandler = object : RoomContext.RoomEventHandler{
        override fun onRoomJoined(state: RoomJoinState) {

        }

        override fun onRoomClosed(reason: RoomClosedReason) {

        }

        override fun onUserPermissionChanged(device: DeviceType, access: Boolean) {

        }

        override fun onFlexRoomPropertiesChanged(properties: Map<String, String>?, fromUser: UserInfo?) {
            ToastUtil.showShort("------FlexRoomProperties------\nproperties=$properties, fromUser=$fromUser")
        }
    }

    private val usersEventHandler = object : UsersContext.UsersEventHandler{
        override fun onLocalConnectStateChanged(state: ConnectState) {

        }

        override fun onUserListChanged(userInfoList: List<UserDetailInfo>) {

        }

        override fun onKickedOut() {

        }

        override fun onUserPropertiesUpdate(userId: String, full: Map<String, Any>?) {
            ToastUtil.showShort("------UserProperties------\nproperties=$full, userId=$userId")
        }
    }

    private val messageEventHandler = object : MessagesContext.MessagesEventHandler{
        override fun onChatMessagesUpdated(messages: List<ChatMessage>) {

        }

        override fun onNotifyMessagesUpdated(messages: List<NotifyMessage>) {

        }

        override fun onPrivateChatMessageReceived(content: String, fromUser: UserInfo) {
            ToastUtil.showShort("------PrivateChatMessage------\nreceived message:\ncontent=$content, fromUser=$fromUser")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_api_test, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contextPool = MeetingUi.globalVMFactory?.getContextPool() ?: return

        initEventHandler()
        initClick()
    }

    private fun initEventHandler() {
        contextPool?.usersContext?.registerEventHandler(usersEventHandler)
        contextPool?.roomContext?.registerEventHandler(roomEventHandler)
        contextPool?.messagesContext?.registerEventHandler(messageEventHandler)
    }

    private fun initClick(){
        userProps()
        roomProps()
        privateChatMsg()
    }

    private fun privateChatMsg() {
        val btnSend = requireView().findViewById<Button>(R.id.btn_send_private_msg)
        btnSend.setOnClickListener {
            val localUserInfo = contextPool?.usersContext?.getLocalUserInfo()
            val userInfoList = contextPool?.usersContext?.getUserInfoList()
            val targetUser = userInfoList?.find { it.userId != localUserInfo?.userId }
                    ?: return@setOnClickListener
            contextPool?.messagesContext?.sendPrivateMessage(targetUser.userId, "hello girls~", {}, {
                ToastUtil.showLong("sendPrivateMessage error: ${it.message}")
            })
        }
    }

    private fun roomProps() {
        val roomPropsBtn = requireView().findViewById<Button>(R.id.btn_update_room_props)
        changeRoomPropsBtnText(roomPropsBtn)
        roomPropsBtn.setOnClickListener {
            contextPool?.roomContext?.updateFlexRoomProperties(roomPropertiesTestList[roomTestIndex % roomPropertiesTestList.size])
            roomTestIndex++
            changeRoomPropsBtnText(roomPropsBtn)
        }
        requireView().findViewById<Button>(R.id.btn_get_room_props).setOnClickListener {
            contextPool?.roomContext?.getFlexRoomProperties()?.let {
                ToastUtil.showLong("------RoomProperties------\nproperties=$it")
            }
        }
    }

    private fun userProps() {
        val userPropsBtn = requireView().findViewById<Button>(R.id.btn_update_user_props)
        changeUserPropsBtnText(userPropsBtn)
        userPropsBtn.setOnClickListener {
            contextPool?.usersContext?.updateLocalUserProperties(userPropertiesTestList[userTestIndex % userPropertiesTestList.size])
            userTestIndex++
            changeUserPropsBtnText(userPropsBtn)
        }

        requireView().findViewById<Button>(R.id.btn_get_user_props).setOnClickListener {
            var toastMsg = "------UserProperties------\n"
            contextPool?.usersContext?.getUserInfoList()?.forEach {
                toastMsg += "userId = ${it.userId}\n"
                toastMsg += "properties = ${contextPool?.usersContext?.getUserProperties(it.userId)}\n"
            }
            ToastUtil.showLong(toastMsg)
        }
    }

    private fun changeUserPropsBtnText(btn: Button){
        val properties = userPropertiesTestList[userTestIndex % userPropertiesTestList.size]
        btn.setText("更新UserProps为: $properties")
    }

    private fun changeRoomPropsBtnText(btn: Button){
        val properties = roomPropertiesTestList[roomTestIndex % roomPropertiesTestList.size]
        btn.setText("更新RoomProps为: $properties")
    }

    override fun onDestroy() {
        super.onDestroy()
        contextPool?.roomContext?.unRegisterEventHandler(roomEventHandler)
        contextPool?.usersContext?.unRegisterEventHandler(usersEventHandler)
        contextPool?.messagesContext?.unRegisterEventHandler(messageEventHandler)
        contextPool = null
    }

    data class TestBean(
            val userId: String
    )
}