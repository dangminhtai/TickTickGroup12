package hcmute.edu.vn.tick_tick.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class StreakManager {
    private static final String PREF_NAME = "streak_prefs";
    private static final String KEY_STREAK_COUNT = "streak_count";
    private static final String KEY_LAST_COMPLETED_DATE = "last_completed_date";

    private final SharedPreferences prefs;

    public StreakManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getStreakCount() {
        checkAndResetStreak();
        return prefs.getInt(KEY_STREAK_COUNT, 0);
    }

    public void onTaskCompleted() {
        long lastDate = prefs.getLong(KEY_LAST_COMPLETED_DATE, 0);
        long today = getStartOfDayMillis(System.currentTimeMillis());

        if (lastDate == 0) {
            // First time
            prefs.edit().putInt(KEY_STREAK_COUNT, 1)
                    .putLong(KEY_LAST_COMPLETED_DATE, today)
                    .apply();
            return;
        }

        if (lastDate == today) {
            // Already counted for today
            return;
        }

        long diff = today - lastDate;
        long oneDay = TimeUnit.DAYS.toMillis(1);

        if (diff == oneDay) {
            // Consecutive day!
            int currentStreak = prefs.getInt(KEY_STREAK_COUNT, 0);
            prefs.edit().putInt(KEY_STREAK_COUNT, currentStreak + 1)
                    .putLong(KEY_LAST_COMPLETED_DATE, today)
                    .apply();
        } else if (diff > oneDay) {
            // Streak broken
            prefs.edit().putInt(KEY_STREAK_COUNT, 1)
                    .putLong(KEY_LAST_COMPLETED_DATE, today)
                    .apply();
        }
    }

    private void checkAndResetStreak() {
        long lastDate = prefs.getLong(KEY_LAST_COMPLETED_DATE, 0);
        if (lastDate == 0) return;

        long today = getStartOfDayMillis(System.currentTimeMillis());
        long diff = today - lastDate;
        long oneDay = TimeUnit.DAYS.toMillis(1);

        if (diff > oneDay) {
            // It's been more than 1 day since last completion
            prefs.edit().putInt(KEY_STREAK_COUNT, 0).apply();
        }
    }

    private long getStartOfDayMillis(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
