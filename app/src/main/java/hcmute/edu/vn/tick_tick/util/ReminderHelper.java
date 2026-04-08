package hcmute.edu.vn.tick_tick.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import hcmute.edu.vn.tick_tick.receiver.TaskAlarmReceiver;

public class ReminderHelper {

    private static final String TAG = "ReminderHelper";
    private static final int DAILY_DIGEST_CODE = 910001;

    /**
     * Schedule an exact alarm at reminderTimeMillis.
     * On Android 12+ this requires SCHEDULE_EXACT_ALARM or USE_EXACT_ALARM permission.
     */
    public static void scheduleReminder(Context context, int taskId, String taskTitle, long reminderTimeMillis) {
        if (reminderTimeMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Reminder time is in the past, skipping.");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    Log.d(TAG, "Exact alarm scheduled for taskId=" + taskId);
                } else {
                    // Fallback to inexact alarm (within ~1 min)
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                    Log.d(TAG, "Inexact alarm scheduled for taskId=" + taskId);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                Log.d(TAG, "Exact alarm scheduled for taskId=" + taskId);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException scheduling alarm: " + e.getMessage());
        }
    }

    /**
     * Schedule a daily digest notification at 8:00 AM local time.
     */
    public static void scheduleDailyDigest(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, hcmute.edu.vn.tick_tick.receiver.DailyReminderReceiver.class)
                .setAction(hcmute.edu.vn.tick_tick.receiver.DailyReminderReceiver.ACTION_DAILY_DIGEST);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_DIGEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 8);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pendingIntent);
            }
            Log.d(TAG, "Daily digest scheduled at " + next.getTime());
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException scheduling daily digest: " + e.getMessage());
        }
    }

    public static void cancelDailyDigest(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, hcmute.edu.vn.tick_tick.receiver.DailyReminderReceiver.class)
                .setAction(hcmute.edu.vn.tick_tick.receiver.DailyReminderReceiver.ACTION_DAILY_DIGEST);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_DIGEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Daily digest cancelled");
    }

    /**
     * Cancel any previously scheduled alarm for this task.
     */
    public static void cancelReminder(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Alarm cancelled for taskId=" + taskId);
    }
}
