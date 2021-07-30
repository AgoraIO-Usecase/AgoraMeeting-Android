package io.agora.meeting.ui.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.agora.meeting.context.ContextPool
import io.agora.meeting.ui.module.board.BoardVM
import io.agora.meeting.ui.module.chat.ChatVM
import io.agora.meeting.ui.module.main.bottom.MainBottomVM
import io.agora.meeting.ui.module.main.notify.MainNotifyVM
import io.agora.meeting.ui.module.main.render.audio.AudioLayoutVM
import io.agora.meeting.ui.module.main.render.lecturer.LecturerLayoutVM
import io.agora.meeting.ui.module.main.render.mix.MixLayoutVM
import io.agora.meeting.ui.module.main.render.tiled.TiledLayoutVM
import io.agora.meeting.ui.module.main.top.MainTopVM
import io.agora.meeting.ui.module.notify.NotifyVM
import io.agora.meeting.ui.module.root.RootVM
import io.agora.meeting.ui.module.setting.SettingVM
import io.agora.meeting.ui.module.users.UsersVM

class VMFactory(private val contextPool: ContextPool) : ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            RootVM::class.java -> RootVM(contextPool.roomContext, contextPool.usersContext)
            MainTopVM::class.java -> MainTopVM(contextPool.roomContext, contextPool.usersContext, contextPool.mediaContext, contextPool.screenContext, contextPool.boardContext)
            MainBottomVM::class.java -> MainBottomVM(contextPool.roomContext, contextPool.usersContext, contextPool.mediaContext, contextPool.messagesContext, contextPool.screenContext, contextPool.boardContext)
            UsersVM::class.java -> UsersVM(contextPool.usersContext)
            BoardVM::class.java -> BoardVM(contextPool.boardContext)
            ChatVM::class.java -> ChatVM(contextPool.messagesContext)
            NotifyVM::class.java -> NotifyVM(contextPool.mediaContext, contextPool.messagesContext)
            MainNotifyVM::class.java -> MainNotifyVM(contextPool.mediaContext, contextPool.messagesContext)
            SettingVM::class.java -> SettingVM(contextPool.roomContext, contextPool.platformContext, contextPool.usersContext, contextPool.messagesContext)
            TiledLayoutVM::class.java -> TiledLayoutVM(contextPool.renderContext, contextPool.mediaContext, contextPool.usersContext)
            LecturerLayoutVM::class.java -> LecturerLayoutVM(contextPool.renderContext, contextPool.mediaContext, contextPool.boardContext, contextPool.screenContext)
            AudioLayoutVM::class.java -> AudioLayoutVM(contextPool.renderContext)
            MixLayoutVM::class.java -> MixLayoutVM(contextPool.renderContext)
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        } as T
    }

    fun getContextPool() = contextPool

    fun release() {
        contextPool.release()
    }


}