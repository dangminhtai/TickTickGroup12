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

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class CalendarFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private TextView tvNoTasks;

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

        adapter = new TaskAdapter(viewModel, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Default: today
        viewModel.setSelectedDate(System.currentTimeMillis());
        loadTasksForDate(System.currentTimeMillis());

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 12, 0, 0);
            long dateMs = cal.getTimeInMillis();
            viewModel.setSelectedDate(dateMs);
            loadTasksForDate(dateMs);
        });
    }

    private void loadTasksForDate(long dateMs) {
        viewModel.getTasksForDate(dateMs).observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
            tvNoTasks.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
