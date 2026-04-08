package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.DateStripAdapter;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.ui.adapter.TimelineAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class CalendarFragment extends Fragment {

    private TaskViewModel viewModel;
    private TimelineAdapter timelineAdapter;
    private TaskAdapter listAdapter;
    private DateStripAdapter dateStripAdapter;
    private TextView tvMonthYear;
    private View emptyTimeline;
    private RecyclerView rvTimeline, rvListView, rvDateStrip;
    private MaterialButton btnToggleView;
    
    private boolean isTimelineView = true;
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM, yyyy", new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        tvMonthYear = view.findViewById(R.id.tv_month_year);
        emptyTimeline = view.findViewById(R.id.empty_timeline);
        rvTimeline = view.findViewById(R.id.rv_timeline);
        rvListView = view.findViewById(R.id.rv_list_view);
        rvDateStrip = view.findViewById(R.id.rv_date_strip);
        View btnToday = view.findViewById(R.id.btn_today);
        btnToggleView = view.findViewById(R.id.btn_toggle_view);

        // Setup Date Strip
        dateStripAdapter = new DateStripAdapter(date -> {
            updateMonthYear(date);
            viewModel.setSelectedDate(date.getTime());
            loadTasksForDate(date.getTime());
        });
        rvDateStrip.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDateStrip.setAdapter(dateStripAdapter);

        // Setup Timeline View
        timelineAdapter = new TimelineAdapter(getParentFragmentManager());
        rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTimeline.setAdapter(timelineAdapter);

        // Setup List View
        listAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        rvListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvListView.setAdapter(listAdapter);

        // View Toggling Logic
        btnToggleView.setOnClickListener(v -> {
            isTimelineView = !isTimelineView;
            updateViewMode();
        });

        View btnCalendar = view.findViewById(R.id.btn_calendar_picker);

        // Date Picker Dialog
        btnCalendar.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            Long current = viewModel.getSelectedDate().getValue();
            if (current != null) cal.setTimeInMillis(current);
            
            new android.app.DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth, 0, 0, 0);
                selected.set(Calendar.MILLISECOND, 0);
                long dateMs = selected.getTimeInMillis();
                
                scrollToDate(dateMs, true);
                viewModel.setSelectedDate(dateMs);
                loadTasksForDate(dateMs);
                updateMonthYear(new Date(dateMs));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Initial state - use start of today
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long today = todayCal.getTimeInMillis();
        scrollToDate(today, false);
        viewModel.setSelectedDate(today);
        loadTasksForDate(today);
        updateMonthYear(new Date(today));
        updateViewMode();

        btnToday.setOnClickListener(v -> {
            Calendar nowCal = Calendar.getInstance();
            nowCal.set(Calendar.HOUR_OF_DAY, 0);
            nowCal.set(Calendar.MINUTE, 0);
            nowCal.set(Calendar.SECOND, 0);
            nowCal.set(Calendar.MILLISECOND, 0);
            long now = nowCal.getTimeInMillis();
            scrollToDate(now, true);
            viewModel.setSelectedDate(now);
            loadTasksForDate(now);
            updateMonthYear(new Date(now));
        });
        
        // Ensure scroll to current hour in today's timeline
        rvTimeline.post(() -> {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (currentHour > 1) {
                rvTimeline.scrollToPosition(currentHour - 1);
            }
        });
    }

    private void updateViewMode() {
        if (isTimelineView) {
            rvTimeline.setVisibility(View.VISIBLE);
            rvTimeline.setAlpha(0f);
            rvTimeline.animate().alpha(1f).setDuration(300).start();
            
            rvListView.setVisibility(View.GONE);
            btnToggleView.setIconResource(R.drawable.ic_list);
        } else {
            rvTimeline.setVisibility(View.GONE);
            
            rvListView.setVisibility(View.VISIBLE);
            rvListView.setAlpha(0f);
            rvListView.animate().alpha(1f).setDuration(300).start();
            
            btnToggleView.setIconResource(R.drawable.ic_today);
        }
        
        // Reload current data for the current mode
        if (viewModel.getSelectedDate().getValue() != null) {
            loadTasksForDate(viewModel.getSelectedDate().getValue());
        }
    }

    private void scrollToDate(long dateMs, boolean smooth) {
        dateStripAdapter.setSelectedDate(dateMs);
        int pos = dateStripAdapter.getPositionForDate(dateMs);
        if (pos != -1) {
            if (smooth) rvDateStrip.smoothScrollToPosition(pos);
            else rvDateStrip.scrollToPosition(pos);
        }
    }

    private void updateMonthYear(Date date) {
        String formatted = monthYearFormat.format(date);
        if (formatted.length() > 0) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        tvMonthYear.setText(formatted);
    }

    private void loadTasksForDate(long dateMs) {
        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(dateMs);
        Calendar today = Calendar.getInstance();
        boolean isToday = selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         selected.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        
        timelineAdapter.setIsTodaySelected(isToday);
        
        viewModel.getTasksForDate(dateMs).observe(getViewLifecycleOwner(), tasks -> {
            if (isTimelineView) {
                timelineAdapter.submitTasks(tasks);
            } else {
                listAdapter.submitList(tasks);
            }
            
            boolean isEmpty = tasks == null || tasks.isEmpty();
            emptyTimeline.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }
}
