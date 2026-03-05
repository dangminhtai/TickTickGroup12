package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class CalendarFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private TextView tvNoTasks;
    private TextView tvSelectedDateLabel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d 'thg' M", new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        RecyclerView recyclerView = view.findViewById(R.id.rv_calendar_tasks);
        tvNoTasks = view.findViewById(R.id.tv_no_tasks_date);
        View layoutNoTasks = view.findViewById(R.id.layout_no_tasks);
        tvSelectedDateLabel = view.findViewById(R.id.tv_selected_date_label);
        View btnGoToday = view.findViewById(R.id.btn_go_today);

        adapter = new TaskAdapter(viewModel, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Khởi tạo ngày hiện tại
        long today = System.currentTimeMillis();
        updateDateLabel(today);
        viewModel.setSelectedDate(today);
        loadTasksForDate(today, layoutNoTasks);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 12, 0, 0);
            long dateMs = cal.getTimeInMillis();
            
            updateDateLabel(dateMs);
            viewModel.setSelectedDate(dateMs);
            loadTasksForDate(dateMs, layoutNoTasks);
        });

        btnGoToday.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            calendarView.setDate(now, true, true);
            updateDateLabel(now);
            viewModel.setSelectedDate(now);
            loadTasksForDate(now, layoutNoTasks);
        });
    }

    private void updateDateLabel(long dateMs) {
        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(dateMs);
        
        Calendar today = Calendar.getInstance();
        
        if (selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selected.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvSelectedDateLabel.setText("Hôm nay");
        } else {
            tvSelectedDateLabel.setText(dateFormat.format(dateMs));
        }
    }

    private void loadTasksForDate(long dateMs, View layoutNoTasks) {
        viewModel.getTasksForDate(dateMs).observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
            layoutNoTasks.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
