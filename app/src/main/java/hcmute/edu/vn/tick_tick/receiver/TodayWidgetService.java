package hcmute.edu.vn.tick_tick.receiver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.data.model.Task;

public class TodayWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodayWidgetFactory(this.getApplicationContext());
    }
}

class TodayWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<Task> taskList = new ArrayList<>();
    private final AppDatabase db;
    private static final int MAX_ITEMS = 7;

    public TodayWidgetFactory(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        // Fetch upcoming deadlines (today onward) from DB synchronously
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        // Note: Using a direct list query without LiveData for widget
        taskList = db.taskDao().getUpcomingDeadlinesForWidget(start, MAX_ITEMS);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= taskList.size()) return null;

        Task task = taskList.get(position);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.today_widget_item);
        rv.setTextViewText(R.id.widget_item_title, task.title);
        // Show due time/date
        if (task.dueDate > 0) {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault());
            rv.setTextViewText(R.id.widget_item_time, df.format(new java.util.Date(task.dueDate)));
        } else {
            rv.setTextViewText(R.id.widget_item_time, "");
        }

        // Priority color
        int color;
        switch (task.priority) {
            case 3: color = Color.RED; break;
            case 2: color = Color.YELLOW; break;
            case 1: color = Color.GREEN; break;
            default: color = Color.GRAY; break;
        }
        rv.setInt(R.id.widget_item_priority, "setColorFilter", color);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
