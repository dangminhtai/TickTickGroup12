package hcmute.edu.vn.tick_tick.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import hcmute.edu.vn.tick_tick.R;

public class ThemeUtil {

    private static final String PREF_THEME_KEY = "theme_pref";
    private static final String PREF_DARK_MODE_KEY = "dark_mode";

    public static final String THEME_INDIGO = "Indigo";
    public static final String THEME_GREEN = "Green";
    public static final String THEME_RED = "Red";
    public static final String THEME_ORANGE = "Pink";

    public static void applyTheme(Context context) {
        context.setTheme(getThemeResId(getCurrentTheme(context)));
    }

    public static void applyNightMode(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDarkTheme(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void setTheme(Context context, String theme) {
        getPrefs(context).edit().putString(PREF_THEME_KEY, theme).apply();
    }

    public static String getCurrentTheme(Context context) {
        return getPrefs(context).getString(PREF_THEME_KEY, THEME_INDIGO);
    }

    public static boolean isDarkTheme(Context context) {
        return getPrefs(context).getBoolean(PREF_DARK_MODE_KEY, false);
    }

    public static void setDarkTheme(Context context, boolean isDark) {
        getPrefs(context).edit().putBoolean(PREF_DARK_MODE_KEY, isDark).apply();
        AppCompatDelegate.setDefaultNightMode(isDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    private static int getThemeResId(String theme) {
        switch (theme) {
            case THEME_GREEN:
                return R.style.AppTheme_Green;
            case THEME_RED:
                return R.style.AppTheme_Red;
            case THEME_ORANGE:
                return R.style.AppTheme_Orange;
            default:
                return R.style.AppTheme_Indigo;
        }
    }
}
