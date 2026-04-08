package hcmute.edu.vn.tick_tick.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.tick_tick.MainActivity;
import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.data.model.Task;

/**
 * Home-screen widget that surfaces the nearest upcoming task deadline.
 */
public class NextUpWidget extends AppWidgetProvider {

    private static final String ACTION_REFRESH = "hcmute.edu.vn.tick_tick.ACTION_NEXT_UP_REFRESH";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(context, NextUpWidget.class));
            onUpdate(context, mgr, ids);
        }
    }

    private void updateWidget(Context context, AppWidgetManager mgr, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_next_up);

        // Click whole card opens app
        PendingIntent openApp = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_next_up_root, openApp);

        // Refresh tap
        Intent refreshIntent = new Intent(context, NextUpWidget.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_next_up_refresh, refreshPendingIntent);

        // Load nearest task in background
        EXECUTOR.execute(() -> {
            List<Task> next = fetchNextTasks(context, 3);
            bindTasks(context, views, next);
            mgr.updateAppWidget(widgetId, views);
        });
    }

    private List<Task> fetchNextTasks(Context context, int limit) {
        try {
            long now = System.currentTimeMillis();
            return AppDatabase.getInstance(context.getApplicationContext())
                    .taskDao()
                    .getUpcomingDeadlinesForWidget(now, limit);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void bindTasks(Context context, RemoteViews views, List<Task> tasks) {
        // Clear all slots first
        int[] titleIds = {R.id.widget_next_up_title_1, R.id.widget_next_up_title_2, R.id.widget_next_up_title_3};
        int[] dueIds = {R.id.widget_next_up_due_1, R.id.widget_next_up_due_2, R.id.widget_next_up_due_3};
        for (int i = 0; i < titleIds.length; i++) {
            views.setTextViewText(titleIds[i], "");
            views.setTextViewText(dueIds[i], "");
        }

        if (tasks == null || tasks.isEmpty()) {
            views.setTextViewText(R.id.widget_next_up_title_1, context.getString(R.string.next_up_empty));
            return;
        }

        int count = Math.min(tasks.size(), 3);
        for (int i = 0; i < count; i++) {
            Task task = tasks.get(i);
            String title = task.title != null && !task.title.isEmpty()
                    ? task.title
                    : context.getString(R.string.task_title_hint);
            views.setTextViewText(titleIds[i], title);

            String dueText = formatDue(context, task);
            views.setTextViewText(dueIds[i], dueText);
        }
    }

    private String formatDue(Context context, Task task) {
        if (task.dueDate <= 0) return "";

        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(task.dueDate);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        CharSequence timePart = DateFormat.format("HH:mm", due);
        if (isSameDay(due, today)) {
            return context.getString(R.string.next_up_due_today, timePart);
        } else if (isSameDay(due, tomorrow)) {
            return context.getString(R.string.next_up_due_tomorrow, timePart);
        } else {
            CharSequence datePart = DateFormat.format("MMM d", due);
            return context.getString(R.string.next_up_due_future, datePart, timePart);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
