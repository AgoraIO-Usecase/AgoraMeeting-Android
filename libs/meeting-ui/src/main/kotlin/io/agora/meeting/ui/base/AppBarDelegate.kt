package io.agora.meeting.ui.base

import androidx.appcompat.widget.Toolbar

interface AppBarDelegate {
    fun setupAppBar(toolbar: Toolbar?, isLight: Boolean, title: String? = null)
}