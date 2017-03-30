package com.slightech.testwficonnectandsave;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jack on 16/7/14.
 */
public class UiUtil {
    /**
     * dp转px
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, double dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    /**
     * SP转PX
     * @param context
     * @param sp
     * @return
     */
    public static int sp2px(Context context, int sp) {
        return (int) (context.getResources().getDisplayMetrics().scaledDensity * sp + 0.5f);
    }
}
