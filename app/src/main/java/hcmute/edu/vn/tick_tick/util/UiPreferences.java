package hcmute.edu.vn.tick_tick.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class UiPreferences {

    public static final String KEY_SHOW_NEXT_UP_CARD = "pref_show_next_up_card";

    private UiPreferences() {}

    public static boolean isNextUpCardEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SHOW_NEXT_UP_CARD, true);
    }

    public static void setNextUpCardEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_NEXT_UP_CARD, enabled).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
}

