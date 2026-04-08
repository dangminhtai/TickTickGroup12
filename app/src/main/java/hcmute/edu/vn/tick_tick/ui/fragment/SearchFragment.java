package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.util.TaskSwipeHelper;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class SearchFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter searchAdapter;
    private EditText etSearch;
    private ImageView btnClear;
    private RecyclerView rvResults;
    private LinearLayout emptyState;
    private ChipGroup chipGroupFilter;

    private LiveData<List<Task>> currentLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        etSearch = view.findViewById(R.id.et_search);
        btnClear = view.findViewById(R.id.btn_clear_search);
        rvResults = view.findViewById(R.id.rv_search_results);
        emptyState = view.findViewById(R.id.search_empty_state);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);

        searchAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(searchAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new TaskSwipeHelper(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Task task = searchAdapter.getCurrentList().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.setCompleted(task, !task.isCompleted);
                } else if (direction == ItemTouchHelper.LEFT) {
                    viewModel.delete(task);
                }
            }
        });
        helper.attachToRecyclerView(rvResults);

        // Search box listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                btnClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                // Reset to "All" chip when text changes
                chipGroupFilter.check(R.id.chip_filter_all);
                observe(viewModel.searchTasks(query.isEmpty() ? "" : query));
            }
        });

        // Clear button
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });

        // Filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String query = etSearch.getText().toString().trim();
            if (id == R.id.chip_filter_all) {
                observe(viewModel.searchTasks(query));
            } else if (id == R.id.chip_filter_high) {
                observe(viewModel.getActiveTasksByPriority(Task.PRIORITY_HIGH));
            } else if (id == R.id.chip_filter_medium) {
                observe(viewModel.getActiveTasksByPriority(Task.PRIORITY_MEDIUM));
            } else if (id == R.id.chip_filter_low) {
                observe(viewModel.getActiveTasksByPriority(Task.PRIORITY_LOW));
            } else if (id == R.id.chip_filter_completed) {
                observe(viewModel.getTodayCompletedTasks());
            }
        });

        // Default: show all tasks
        observe(viewModel.getAllTasks());
    }

    private void observe(LiveData<List<Task>> liveData) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentLiveData = liveData;
        currentLiveData.observe(getViewLifecycleOwner(), tasks -> {
            searchAdapter.submitList(tasks);
            boolean empty = tasks == null || tasks.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvResults.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
