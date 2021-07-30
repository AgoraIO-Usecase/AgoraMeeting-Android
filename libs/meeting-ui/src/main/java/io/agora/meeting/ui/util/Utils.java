package io.agora.meeting.ui.util;

import android.content.Context;

/**
 * Description:
 *
 * @author xcz
 * @since 3/4/21
 */
public class Utils {

    public static void init(Context context){
        PreferenceUtil.init(context);
        ToastUtil.init(context);
    }

}
