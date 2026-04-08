package hcmute.edu.vn.tick_tick.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private final List<Task> allDayTasks = new ArrayList<>();
    private final List<List<Task>> hourlyTasks = new ArrayList<>();
    private final FragmentManager fragmentManager;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private boolean isTodaySelected = false;

    public TimelineAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        for (int i = 0; i < 24; i++) {
            hourlyTasks.add(new ArrayList<>());
        }
    }

    public void setIsTodaySelected(boolean isToday) {
        this.isTodaySelected = isToday;
    }

    public void submitTasks(List<Task> tasks) {
        allDayTasks.clear();
        for (int i = 0; i < 24; i++) {
            hourlyTasks.get(i).clear();
        }

        if (tasks != null) {
            for (Task task : tasks) {
                if (task.dueDate == 0) {
                    allDayTasks.add(task);
                    continue;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(task.dueDate);
                
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                
                // Heuristic: if time is 23:59 and it's the only info, it might be an "all day" task with date only
                if (hour == 23 && minute == 59) {
                    allDayTasks.add(task);
                } else {
                    hourlyTasks.get(hour).add(task);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 25; // Header (All day) + 24 hours
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_slot, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        if (position == 0) {
            holder.bindAllDay(allDayTasks);
        } else {
            int hour = position - 1;
            holder.bind(hour, hourlyTasks.get(hour));
        }
    }

    class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour;
        LinearLayout tasksContainer;
        View nowIndicator;

        TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tv_hour);
            tasksContainer = itemView.findViewById(R.id.tasks_container);
            // We'll add nowIndicator programmatically or just reuse a view
        }

        void bindAllDay(List<Task> tasks) {
            tvHour.setText("Cả ngày");
            tvHour.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_primary));
            tasksContainer.removeAllViews();
            
            if (tasks.isEmpty()) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                for (Task task : tasks) {
                    addTaskView(task);
                }
            }
        }

        void bind(int hour, List<Task> tasks) {
            itemView.setVisibility(View.VISIBLE);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            
            tvHour.setText(String.format(Locale.getDefault(), "%02d:00", hour));
            tvHour.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_text_hint));
            tasksContainer.removeAllViews();

            // Current time indicator logic
            Calendar now = Calendar.getInstance();
            if (isTodaySelected && now.get(Calendar.HOUR_OF_DAY) == hour) {
                addNowIndicator();
                tvHour.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_overdue));
            }

            if (tasks.isEmpty()) {
                tvHour.setAlpha(0.3f);
            } else {
                tvHour.setAlpha(1.0f);
                for (Task task : tasks) {
                    addTaskView(task);
                }
            }
        }

        private void addNowIndicator() {
            View indicator = new View(itemView.getContext());
            int height = (int) (2 * itemView.getContext().getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            lp.bottomMargin = (int) (8 * itemView.getContext().getResources().getDisplayMetrics().density);
            indicator.setLayoutParams(lp);
            indicator.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.color_overdue));
            tasksContainer.addView(indicator);
        }

        private void addTaskView(Task task) {
            View taskView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_timeline_task, tasksContainer, false);
            TextView tvTitle = taskView.findViewById(R.id.tv_task_title);
            TextView tvTime = taskView.findViewById(R.id.tv_task_time);
            View priorityDot = taskView.findViewById(R.id.priority_dot);

            tvTitle.setText(task.title);
            if (task.isCompleted) {
                tvTitle.setAlpha(0.5f);
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.dueDate);
            if (task.dueDate > 0 && !(cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59)) {
                tvTime.setText(timeFormat.format(new Date(task.dueDate)));
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setVisibility(View.GONE);
            }

            int color;
            switch (task.priority) {
                case Task.PRIORITY_HIGH: color = R.color.priority_high; break;
                case Task.PRIORITY_MEDIUM: color = R.color.priority_medium; break;
                case Task.PRIORITY_LOW: color = R.color.priority_low; break;
                default: color = R.color.color_divider; break;
            }
            priorityDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), color)));

            taskView.setOnClickListener(v -> {
                AddTaskBottomSheet sheet = AddTaskBottomSheet.newInstance(task);
                sheet.show(fragmentManager, AddTaskBottomSheet.TAG);
            });

            tasksContainer.addView(taskView);
        }
    }
}
