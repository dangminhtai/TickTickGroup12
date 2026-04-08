package hcmute.edu.vn.tick_tick;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

import hcmute.edu.vn.tick_tick.util.ReminderHelper;
import hcmute.edu.vn.tick_tick.util.ThemeUtil;

public class TickTickApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ThemeUtil.applyNightMode(this);
        DynamicColors.applyToActivitiesIfAvailable(this);
        ReminderHelper.scheduleDailyDigest(this);
    }
}
