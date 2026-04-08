package hcmute.edu.vn.tick_tick.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import hcmute.edu.vn.tick_tick.MainActivity;
import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.util.StreakManager;

public class StreakWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        StreakManager streakManager = new StreakManager(context);
        int streak = streakManager.getStreakCount();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.streak_widget);
        views.setTextViewText(R.id.tv_streak_count, String.valueOf(streak));

        // Click to open app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.tv_streak_count, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
