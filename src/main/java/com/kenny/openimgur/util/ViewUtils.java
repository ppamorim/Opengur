package com.kenny.openimgur.util;

import android.content.Context;
import android.os.Build;

/**
 * Created by kcampagna on 7/27/14.
 */
public class ViewUtils {

    /**
     * Returns the height of the status bar
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int height = 0;

        // On 4.4 + devices, we need to account for the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                height = context.getResources().getDimensionPixelSize(resourceId);
            }
        }

        return height;
    }
}
