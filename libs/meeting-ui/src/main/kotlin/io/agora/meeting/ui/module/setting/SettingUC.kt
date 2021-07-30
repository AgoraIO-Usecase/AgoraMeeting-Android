package io.agora.meeting.ui.module.setting

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.observe
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import io.agora.meeting.context.bean.DeviceType
import io.agora.meeting.ui.R
import io.agora.meeting.ui.base.BaseUiController
import io.agora.meeting.ui.databinding.SettingLayoutBinding
import io.agora.meeting.ui.util.AvatarUtil
import io.agora.meeting.ui.util.ClipboardUtil
import io.agora.meeting.ui.util.ToastUtil
import io.agora.meeting.ui.widget.OptionsDialogPreference

class SettingUC : BaseUiController<SettingLayoutBinding, SettingVM>(
        SettingLayoutBinding::class.java,
        SettingVM::class.java
) {

    private var preferenceFragment: PreferenceFragment? = null

    override fun onViewModelBind() {
        super.onViewModelBind()
        preferenceFragment = PreferenceFragment(requireViewModel())
        requireFragmentManager().beginTransaction()
                .add(R.id.preference_container, preferenceFragment!!)
                .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceFragment = null
    }


    class PreferenceFragment(private val viewModel: SettingVM) : PreferenceFragmentCompat() {

        private var cameraApproveSwitch: SwitchPreferenceCompat? = null
        private var micApproveSwitch: SwitchPreferenceCompat? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.meeting_setting_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setDivider(null)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            initPreferences()
        }

        private fun initPreferences() {
            val preferenceScreen = preferenceScreen
            cameraApproveSwitch = preferenceScreen.findPreference(getString(R.string.key_camera_approve))
            micApproveSwitch = preferenceScreen.findPreference(getString(R.string.key_mic_approve))
            val roomNamePf = preferenceScreen.findPreference<Preference>(getString(R.string.key_room_name))
            val roomPwdPf = preferenceScreen.findPreference<Preference>(getString(R.string.key_room_pwd))
            val userNamePf = preferenceScreen.findPreference<Preference>(getString(R.string.key_name))
            val userAvatarPf = preferenceScreen.findPreference<Preference>(getString(R.string.key_avatar))
            val userRolePf = preferenceScreen.findPreference<Preference>(getString(R.string.key_role))

            viewModel.cameraAccess.observe(viewLifecycleOwner) {
                cameraApproveSwitch!!.isChecked = !it
            }
            cameraApproveSwitch?.setOnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                if (newValue is Boolean) {
                    viewModel.changeUserPermission(DeviceType.Camera, !newValue)
                }
                true
            }
            viewModel.micAccess.observe(viewLifecycleOwner) {
                micApproveSwitch?.isChecked = !it
            }
            micApproveSwitch?.setOnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                if (newValue is Boolean) {
                    viewModel.changeUserPermission(DeviceType.Mic, !newValue)
                }
                true
            }


            roomNamePf?.summary = viewModel.getRoomInfo().roomName
            roomPwdPf?.summary = viewModel.getRoomInfo().roomPwd

            viewModel.getLocalUserInfo().let {
                cameraApproveSwitch?.isEnabled = it.userRole == io.agora.meeting.context.bean.UserRole.host
                micApproveSwitch?.isEnabled = it.userRole == io.agora.meeting.context.bean.UserRole.host

                userNamePf?.summary = it.userName
                userRolePf?.summary = when (it.userRole) {
                    io.agora.meeting.context.bean.UserRole.host -> getString(R.string.cmm_admin)
                    else -> getString(R.string.cmm_member)
                }
                setAvatar(userAvatarPf, userNamePf)
            }

            val optionPreference = (preferenceScreen.findPreference(getString(R.string.key_notify_max_num)) as OptionsDialogPreference?)
                    ?.addOption(getString(R.string.notify_member_option_always_mute), 0)
                    ?.addOptions(10, 10, 10, getString(R.string.notify_member_option))
                    ?.addOption(getString(R.string.notify_member_option_never_mute), Int.MAX_VALUE)
            optionPreference?.value = viewModel.getUserInOutNotifyLimit()
            optionPreference?.setOnItemSelectedListener { _, value ->
                viewModel.setUserInOutNotifyLimit(value)
            }
        }

        private fun setAvatar(avatarPreference: Preference?, namePreference: Preference?) {
            if (TextUtils.isEmpty(namePreference!!.summary)) {
                return
            }
            AvatarUtil.loadCircleAvatar(requireContext(), namePreference.summary.toString(), object : AvatarUtil.DrawableTarget{
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(resource: Drawable) {
                    avatarPreference!!.icon = resource
                }

            })
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            val key = preference!!.key
            if (TextUtils.equals(key, getString(R.string.key_upload))) {
                viewModel.uploadLog(
                        {
                            ClipboardUtil.copy2Clipboard(requireContext(), it)
                            ToastUtil.showShort(R.string.net_upload_success)
                        },
                        {
                            ToastUtil.showShort(R.string.net_upload_failed)
                        })
            }
            return true
        }

    }
}