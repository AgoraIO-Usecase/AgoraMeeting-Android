package io.agora.meeting

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.agora.meeting.ui.adapter.BindingAdapters
import io.agora.meeting.ui.util.AvatarUtil
import io.agora.meeting.ui.widget.OptionsDialogPreference

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        BindingAdapters.bindToolbarTitle(toolbar, true, getString(R.string.setting_title),
                resources.getColor(io.agora.meeting.ui.R.color.global_text_color_black),
                resources.getDimensionPixelOffset(io.agora.meeting.ui.R.dimen.global_text_size_large).toFloat())
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.container, SettingFragment())
                .commit()
    }


    class SettingFragment : PreferenceFragmentCompat(){

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setDivider(null)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.setting_preferences, rootKey)

            val avatarPf = preferenceScreen.findPreference<Preference>(getString(R.string.key_avatar))
            val namePf = preferenceScreen.findPreference<Preference>(getString(R.string.key_name))
            val notifyPf = preferenceScreen.findPreference<OptionsDialogPreference>(getString(R.string.key_notify_max_num))
            val maxHostPf = preferenceScreen.findPreference<OptionsDialogPreference>(getString(R.string.key_max_host))

            namePf?.summary = LoginActivity.sUserName
            setAvatar(avatarPf, namePf)
            notifyPf?.addOption(getString(io.agora.meeting.ui.R.string.notify_member_option_always_mute), 0)
                    ?.addOptions(10, 10, 10, getString(io.agora.meeting.ui.R.string.notify_member_option))
                    ?.addOption(getString(io.agora.meeting.ui.R.string.notify_member_option_never_mute), Int.MAX_VALUE)
                    ?.value = PreferenceManager.getDefaultSharedPreferences(context).getInt(getString(R.string.key_notify_max_num), 50)
            maxHostPf?.addOptions(1, 1, 100, "%d")
                    ?.value = PreferenceManager.getDefaultSharedPreferences(context).getInt(getString(R.string.key_max_host), 3)
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
            if(preference?.key == getString(R.string.key_about)){
                startActivity(Intent(requireContext(), AboutActivity::class.java))
                return true
            }
            return super.onPreferenceTreeClick(preference)
        }
    }
}