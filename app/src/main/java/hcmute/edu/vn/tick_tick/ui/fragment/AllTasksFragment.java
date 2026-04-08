package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.util.TaskSwipeHelper;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class AllTasksFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private TextView tvEmpty;
    private LinearLayout emptyContainer;
    private TextInputEditText etSearch;
    private ChipGroup chipGroup;

    private final List<Task> allTasks = new ArrayList<>();
    private String currentQuery = "";
    private String currentFilter = FILTER_ALL;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_ACTIVE = "ACTIVE";
    private static final String FILTER_COMPLETED = "COMPLETED";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_all_tasks);
        tvEmpty = view.findViewById(R.id.tv_empty_all);
        emptyContainer = view.findViewById(R.id.empty_container);
        etSearch = view.findViewById(R.id.et_search);
        chipGroup = view.findViewById(R.id.chip_group_all_filters);

        adapter = new TaskAdapter(viewModel, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new TaskSwipeHelper(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Task task = adapter.getCurrentList().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.setCompleted(task, !task.isCompleted);
                } else if (direction == ItemTouchHelper.LEFT) {
                    viewModel.delete(task);
                }
            }
        });
        helper.attachToRecyclerView(recyclerView);

        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks.clear();
            if (tasks != null) {
                allTasks.addAll(tasks);
            }
            applyFilters();
        });

        setupSearch();
        setupFilterChips();
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s != null ? s.toString().trim() : "";
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> applyFilters();
                searchHandler.postDelayed(searchRunnable, 300); // 300ms debounce
            }
        });
    }

    private void setupFilterChips() {
        if (chipGroup == null) return;
        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) {
                group.check(R.id.chip_all);
                return;
            }
            int id = ids.get(0);
            if (id == R.id.chip_active) currentFilter = FILTER_ACTIVE;
            else if (id == R.id.chip_completed) currentFilter = FILTER_COMPLETED;
            else currentFilter = FILTER_ALL;
            applyFilters();
        });
    }

    private void applyFilters() {
        List<Task> filtered = new ArrayList<>();
        String query = currentQuery.toLowerCase();

        for (Task task : allTasks) {
            if (!matchesFilter(task)) continue;
            if (!matchesQuery(task, query)) continue;
            filtered.add(task);
        }

        adapter.submitList(filtered);
        boolean isEmpty = filtered.isEmpty();
        if (emptyContainer != null) emptyContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private boolean matchesFilter(Task task) {
        switch (currentFilter) {
            case FILTER_ACTIVE:
                return !task.isCompleted;
            case FILTER_COMPLETED:
                return task.isCompleted;
            default:
                return true;
        }
    }

    private boolean matchesQuery(Task task, String queryLower) {
        if (queryLower.isEmpty()) return true;
        String title = task.title != null ? task.title.toLowerCase() : "";
        String notes = task.notes != null ? task.notes.toLowerCase() : "";
        String tags = task.tags != null ? task.tags.toLowerCase() : "";
        return title.contains(queryLower) || notes.contains(queryLower) || tags.contains(queryLower);
    }
}
