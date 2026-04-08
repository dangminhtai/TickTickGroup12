package hcmute.edu.vn.tick_tick.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.MainActivity;
import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.dao.TaskDao;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.data.model.Task;

/**
 * Morning digest notification summarizing today's tasks.
 */
public class DailyReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_DIGEST = "hcmute.edu.vn.tick_tick.ACTION_DAILY_DIGEST";
    public static final int REQUEST_CODE = 910001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_DAILY_DIGEST.equals(intent.getAction())) return;

        TaskDao dao = AppDatabase.getInstance(context).taskDao();

        AppDatabase.executor().execute(() -> {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 1);

            List<Task> todayTasks = dao.getTasksBetweenForWidget(start.getTimeInMillis(), end.getTimeInMillis());
            if (todayTasks == null) {
                todayTasks = Collections.emptyList();
            }

            createNotificationChannel(context);

            Intent openIntent = new Intent(context, MainActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent openPending = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            String title = context.getString(R.string.app_name) + " • Tóm tắt hôm nay";
            String fallback = "Không có việc nào trong hôm nay";

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            int maxLines = 5;
            int added = 0;
            for (Task task : todayTasks) {
                if (added >= maxLines) break;
                String due = formatDue(task);
                inboxStyle.addLine("• " + task.title + due);
                added++;
            }
            if (todayTasks.isEmpty()) {
                inboxStyle.addLine(fallback);
            }

            String contentText = todayTasks.isEmpty()
                    ? fallback
                    : "Bạn có " + todayTasks.size() + " việc hôm nay";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TaskAlarmReceiver.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notes)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setStyle(inboxStyle)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setColor(ContextCompat.getColor(context, R.color.color_primary))
                    .setContentIntent(openPending)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(REQUEST_CODE, builder.build());
            }
        });
    }

    private String formatDue(Task task) {
        if (task.dueDate == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(" • HH:mm", Locale.getDefault());
        return sdf.format(task.dueDate);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        if (manager.getNotificationChannel(TaskAlarmReceiver.CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(
                TaskAlarmReceiver.CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Thông báo nhắc nhở công việc");
        channel.enableLights(true);
        channel.enableVibration(true);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        channel.setSound(defaultSoundUri, audioAttributes);

        manager.createNotificationChannel(channel);
    }
}

