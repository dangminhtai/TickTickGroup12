package hcmute.edu.vn.tick_tick.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import hcmute.edu.vn.tick_tick.data.dao.TaskDao;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.util.ReminderHelper;

public class ReminderBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        TaskDao dao = AppDatabase.getInstance(context).taskDao();
        AppDatabase.executor().execute(() -> {
            List<Task> pending = dao.getPendingReminderTasks(System.currentTimeMillis());
            if (pending == null || pending.isEmpty()) return;
            for (Task task : pending) {
                ReminderHelper.scheduleReminder(context, task.id, task.title, task.reminderTime);
            }

            // Also reschedule the daily digest summary
            ReminderHelper.scheduleDailyDigest(context);
        });
    }
}
