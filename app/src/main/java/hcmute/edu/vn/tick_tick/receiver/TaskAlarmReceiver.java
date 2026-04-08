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

import hcmute.edu.vn.tick_tick.MainActivity;
import hcmute.edu.vn.tick_tick.R;

public class TaskAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    private static final String LEGACY_CHANNEL_ID = "tick_tick_reminders";
    public static final String CHANNEL_ID = "tick_tick_reminders_v2";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        if (taskId == -1 || taskTitle == null) return;

        createNotificationChannel(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, taskId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent completeAction = PendingIntent.getBroadcast(
                context,
                taskId * 10 + 1,
                new Intent(context, TaskActionReceiver.class)
                        .setAction(TaskActionReceiver.ACTION_COMPLETE)
                        .putExtra(TaskActionReceiver.EXTRA_TASK_ID, taskId)
                        .putExtra(TaskActionReceiver.EXTRA_TASK_TITLE, taskTitle),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent snoozeAction = PendingIntent.getBroadcast(
                context,
                taskId * 10 + 2,
                new Intent(context, TaskActionReceiver.class)
                        .setAction(TaskActionReceiver.ACTION_SNOOZE)
                        .putExtra(TaskActionReceiver.EXTRA_TASK_ID, taskId)
                        .putExtra(TaskActionReceiver.EXTRA_TASK_TITLE, taskTitle),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Lấy URI âm thanh mặc định của hệ thống
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notes)
                .setContentTitle("🔔 Nhắc nhở: " + taskTitle)
                .setContentText("Đã đến lúc thực hiện công việc này!")
                .addAction(R.drawable.ic_check, "Hoàn thành", completeAction)
                .addAction(R.drawable.ic_skip_next, "Hoãn 15 phút", snoozeAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .setSound(defaultSoundUri) // Pre-Oreo sound
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Ensure sound/vibrate on pre-O
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(taskId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // Always recreate channel to avoid inheriting a silent config
                if (manager.getNotificationChannel(CHANNEL_ID) != null) {
                    manager.deleteNotificationChannel(CHANNEL_ID);
                }
                if (manager.getNotificationChannel(LEGACY_CHANNEL_ID) != null) {
                    manager.deleteNotificationChannel(LEGACY_CHANNEL_ID);
                }

                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
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
                channel.setSound(defaultSoundUri, audioAttributes);

                manager.createNotificationChannel(channel);
            }
        }
    }
}
