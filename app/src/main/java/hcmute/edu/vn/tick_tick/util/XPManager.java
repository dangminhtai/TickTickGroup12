package hcmute.edu.vn.tick_tick.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages XP (Experience Points) and Level for gamification.
 * XP is earned when tasks are completed:
 *   - PRIORITY_NONE: +10 XP
 *   - PRIORITY_LOW:  +15 XP
 *   - PRIORITY_MEDIUM: +25 XP
 *   - PRIORITY_HIGH: +40 XP
 */
public class XPManager {

    private static final String PREFS_NAME = "xp_prefs";
    private static final String KEY_XP = "total_xp";
    private static final String KEY_STREAK = "streak_days";
    private static final String KEY_LAST_ACTIVE = "last_active_day";

    // XP per priority
    public static final int XP_NONE = 10;
    public static final int XP_LOW = 15;
    public static final int XP_MEDIUM = 25;
    public static final int XP_HIGH = 40;


    private final SharedPreferences prefs;

    public XPManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Award XP for completing a task based on its priority */
    public int awardXP(int priority) {
        int earned;
        switch (priority) {
            case 3: earned = XP_HIGH; break;
            case 2: earned = XP_MEDIUM; break;
            case 1: earned = XP_LOW; break;
            default: earned = XP_NONE; break;
        }
        int newTotal = getTotalXP() + earned;
        prefs.edit().putInt(KEY_XP, newTotal).apply();
        updateStreak();
        return earned;
    }

    public int getTotalXP() {
        return prefs.getInt(KEY_XP, 0);
    }

    /** Level = 1 base, +1 per 100 XP, capped at level display */
    public int getLevel() {
        return 1 + (getTotalXP() / 100);
    }

    /** XP needed for next level */
    public int getXPForNextLevel() {
        return 100 - (getTotalXP() % 100);
    }

    /** XP progress within current level (0–100) */
    public int getLevelProgress() {
        return getTotalXP() % 100;
    }

    public String getLevelTitle() {
        int level = getLevel();
        if (level < 3)  return "🌱 Người mới bắt đầu";
        if (level < 6)  return "⚡ Học việc";
        if (level < 10) return "🔥 Chiến binh";
        if (level < 15) return "💎 Chuyên gia";
        if (level < 20) return "🚀 Bậc thầy";
        return "👑 Huyền thoại";
    }

    // ---- Streak ----

    public int getStreakDays() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    private void updateStreak() {
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0);
        long todayMidnight = getTodayMidnight();
        long yesterdayMidnight = todayMidnight - 86400000L;

        if (lastActive >= todayMidnight) {
            // Already active today, no change
        } else if (lastActive >= yesterdayMidnight) {
            // Was active yesterday — extend streak
            int streak = prefs.getInt(KEY_STREAK, 0);
            prefs.edit()
                    .putInt(KEY_STREAK, streak + 1)
                    .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
                    .apply();
        } else {
            // Missed days — reset streak
            prefs.edit()
                    .putInt(KEY_STREAK, 1)
                    .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
                    .apply();
        }
    }

    private long getTodayMidnight() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
