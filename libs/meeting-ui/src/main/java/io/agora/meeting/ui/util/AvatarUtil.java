package io.agora.meeting.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


/**
 * Description:
 *
 * @author xcz
 * @since 3/4/21
 */
public class AvatarUtil {

    public static void loadCircleAvatar(Context context, String userName, @Nullable DrawableTarget target) {
        int index = getAvatarIndex(CryptoUtil.md5(userName));
        Glide.with(context)
                .load(getAvatarUrl(index))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(new CustomTarget<Drawable>() {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (target != null) {
                            target.onResourceReady(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (target != null) {
                            target.onLoadCleared(placeholder);
                        }
                    }
                });
    }

    public static void loadCircleAvatar(View attachParent, ImageView imageView, String userName) {
        int index = getAvatarIndex(CryptoUtil.md5(userName));
        Glide.with(attachParent)
                .load(getAvatarUrl(index))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }

    public static void loadAvatar(View attachParent, ImageView imageView, String userName) {
        int index = getAvatarIndex(CryptoUtil.md5(userName));
        Glide.with(attachParent)
                .load(getAvatarUrl(index))
                .centerCrop()
                .into(imageView);
    }


    private static String getAvatarUrl(int index) {
        return "file:///android_asset/avatar/avatar_" + index + ".png";
    }

    private static int getAvatarIndex(String userId) {
        if (TextUtils.isEmpty(userId) || userId.length() < 2) {
            return 0;
        }
        String indexStr = userId.substring(userId.length() - 2).toUpperCase();
        return Integer.parseInt(indexStr, 16) % 36;
    }

    public interface DrawableTarget {
        void onResourceReady(@NonNull Drawable resource);

        void onLoadCleared(@Nullable Drawable placeholder);
    }
}
