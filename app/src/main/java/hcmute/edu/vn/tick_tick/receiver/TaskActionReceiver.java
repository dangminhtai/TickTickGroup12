package hcmute.edu.vn.tick_tick.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hcmute.edu.vn.tick_tick.data.dao.TaskDao;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.util.ReminderHelper;

public class TaskActionReceiver extends BroadcastReceiver {

    public static final String ACTION_COMPLETE = "hcmute.edu.vn.tick_tick.ACTION_COMPLETE";
    public static final String ACTION_SNOOZE = "hcmute.edu.vn.tick_tick.ACTION_SNOOZE";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    private static final long SNOOZE_INTERVAL_MILLIS = 15 * 60 * 1000L; // 15 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        if (taskId == -1) return;

        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        String action = intent.getAction();

        TaskDao dao = AppDatabase.getInstance(context).taskDao();

        if (ACTION_COMPLETE.equals(action)) {
            AppDatabase.executor().execute(() -> {
                dao.setTaskCompleted(taskId, true, System.currentTimeMillis());
                dao.updateReminderTime(taskId, 0);
                ReminderHelper.cancelReminder(context, taskId);
                refreshTodayWidget(context);
            });
        } else if (ACTION_SNOOZE.equals(action)) {
            long snoozeAt = System.currentTimeMillis() + SNOOZE_INTERVAL_MILLIS;
            AppDatabase.executor().execute(() -> {
                dao.updateReminderTime(taskId, snoozeAt);
                ReminderHelper.cancelReminder(context, taskId);
                ReminderHelper.scheduleReminder(context, taskId, taskTitle != null ? taskTitle : "", snoozeAt);
                refreshTodayWidget(context);
            });
        }
    }

    private void refreshTodayWidget(Context context) {
        Intent refreshIntent = new Intent(context, TodayWidget.class);
        refreshIntent.setAction(TodayWidget.ACTION_REFRESH);
        context.sendBroadcast(refreshIntent);
    }
}
