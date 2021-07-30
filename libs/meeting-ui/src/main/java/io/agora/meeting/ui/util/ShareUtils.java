package io.agora.meeting.ui.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import io.agora.meeting.context.bean.RoomInfo;
import io.agora.meeting.context.bean.UserDetailInfo;
import io.agora.meeting.ui.R;

/**
 * Description:
 *
 * @author xcz
 * @since 1/27/21
 */
public class ShareUtils {


    public static String getMeetingShareInfo(Context context, RoomInfo room, UserDetailInfo me){
        Resources resources = context.getResources();
        String shareInfo = resources.getString(R.string.invite_meeting_name, room.getRoomName()) +
                "\n" + resources.getString(R.string.invite_meeting_pwd, room.getRoomPwd()) +
                "\n" + resources.getString(R.string.invite_invited_by, me.getUserName()) +
                //"\n" + resources.getString(R.string.invite_web_link, resources.getString(R.string.web_url)) +
                "\n" + resources.getString(R.string.invite_android_link, resources.getString(R.string.android_url)) +
                "\n" + resources.getString(R.string.invite_ios_link, resources.getString(R.string.ios_url));
        return shareInfo;
    }

    private static void shareTextBySystem(Context context, String content){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, ""));
    }
}
