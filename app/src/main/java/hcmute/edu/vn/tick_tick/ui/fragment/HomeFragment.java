package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.text.format.DateFormat;
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

import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class HomeFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter taskAdapter;
    private TaskAdapter completedAdapter;
    private TextView tvGreeting, tvDate, tvEmptyState, tvCompletedHeader;
    private RecyclerView rvTasks, rvCompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvDate = view.findViewById(R.id.tv_date);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvCompletedHeader = view.findViewById(R.id.tv_completed_header);
        rvTasks = view.findViewById(R.id.rv_tasks);
        rvCompleted = view.findViewById(R.id.rv_completed);

        // Set greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText(R.string.greeting_morning);
        else if (hour < 17) tvGreeting.setText(R.string.greeting_afternoon);
        else tvGreeting.setText(R.string.greeting_evening);

        // Set date
        tvDate.setText(DateFormat.format("EEEE, MMMM d", Calendar.getInstance()));

        // Task list
        taskAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        // Completed list
        completedAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        completedAdapter.setCompletedMode(true);
        rvCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCompleted.setAdapter(completedAdapter);

        // Observe today tasks
        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.submitList(tasks);
            tvEmptyState.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
            rvTasks.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });

        // Observe completed
        viewModel.getTodayCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            completedAdapter.submitList(tasks);
            boolean show = tasks != null && !tasks.isEmpty();
            tvCompletedHeader.setVisibility(show ? View.VISIBLE : View.GONE);
            rvCompleted.setVisibility(show ? View.VISIBLE : View.GONE);
        });
    }
}
