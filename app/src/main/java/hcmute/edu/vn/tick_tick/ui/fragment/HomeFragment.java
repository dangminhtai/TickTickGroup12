package hcmute.edu.vn.tick_tick.ui.fragment;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;
import hcmute.edu.vn.tick_tick.util.TaskSwipeHelper;
import hcmute.edu.vn.tick_tick.util.UiPreferences;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class HomeFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter taskAdapter;
    private TaskAdapter completedAdapter;
    private TextView tvGreeting, tvDate, tvTaskCount, tvProgressPercent;
    private TextView tvFilterStatus, tvEmptyTitle, tvEmptyState;
    private LinearLayout emptyStateContainer, completedSection;
    private RecyclerView rvTasks, rvCompleted;
    private CircularProgressIndicator progressTasks;
    private MaterialCardView cardStats;
    private MaterialCardView cardNextUp;
    private TextView tvNextUpTitle, tvNextUpDue, tvNextUpEmpty;
    private View cardQuickAdd, cardGoCalendar, cardGoStats;
    private ChipGroup chipGroupFilters;
    private List<Task> latestTodayTasks = new ArrayList<>();
    private List<Task> latestUpcomingTasks = new ArrayList<>();
    private Task nextUpTask;
    
    private int totalTasks = 0;
    private int completedTasks = 0;
    private String activeFilter = FILTER_ALL;

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_PRIORITY = "PRIORITY";
    private static final String FILTER_REMINDER = "REMINDER";
    private static final String FILTER_NO_DUE = "NO_DUE";
    private static final String FILTER_OVERDUE = "OVERDUE";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Initialize views
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvDate = view.findViewById(R.id.tv_date);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        tvProgressPercent = view.findViewById(R.id.tv_progress_percent);
        tvFilterStatus = view.findViewById(R.id.tv_filter_status);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        completedSection = view.findViewById(R.id.completed_section);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        rvTasks = view.findViewById(R.id.rv_tasks);
        rvCompleted = view.findViewById(R.id.rv_completed);
        progressTasks = view.findViewById(R.id.progress_tasks);
        cardStats = view.findViewById(R.id.card_stats);
        cardQuickAdd = view.findViewById(R.id.card_quick_add);
        cardGoCalendar = view.findViewById(R.id.card_go_calendar);
        cardGoStats = view.findViewById(R.id.card_go_stats);
        cardNextUp = view.findViewById(R.id.card_next_up);
        tvNextUpTitle = view.findViewById(R.id.tv_next_up_title);
        tvNextUpDue = view.findViewById(R.id.tv_next_up_due);
        tvNextUpEmpty = view.findViewById(R.id.tv_next_up_empty);

        // Set greeting with animation
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText(R.string.greeting_morning);
        else if (hour < 17) tvGreeting.setText(R.string.greeting_afternoon);
        else tvGreeting.setText(R.string.greeting_evening);
        
        // Animate greeting
        tvGreeting.setAlpha(0f);
        tvGreeting.setTranslationY(-20f);
        tvGreeting.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();

        // Set date
        tvDate.setText(DateFormat.format("EEEE, MMMM d", Calendar.getInstance()));

        // Animate stats card
        cardStats.setAlpha(0f);
        cardStats.setScaleX(0.95f);
        cardStats.setScaleY(0.95f);
        cardStats.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(100)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();

        // Task list
        taskAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);
        rvTasks.setItemAnimator(null); // We handle animations ourselves

        // Completed list
        completedAdapter = new TaskAdapter(viewModel, getParentFragmentManager());
        completedAdapter.setCompletedMode(true);
        rvCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCompleted.setAdapter(completedAdapter);

        setupFilters();
        setupQuickActions();

        // Smart Sort Button
        View btnSmartSort = view.findViewById(R.id.btn_smart_sort);
        if (btnSmartSort != null) {
            btnSmartSort.setOnClickListener(v -> {
                taskAdapter.smartSort();
                Toast.makeText(requireContext(), getString(R.string.smart_sort_toast), Toast.LENGTH_SHORT).show();
            });
        }
        // Setup Swipe Actions for rvTasks
        ItemTouchHelper helper = new ItemTouchHelper(new TaskSwipeHelper(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Task task = taskAdapter.getCurrentList().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.setCompleted(task, true);
                } else if (direction == ItemTouchHelper.LEFT) {
                    viewModel.delete(task);
                }
            }
        });
        helper.attachToRecyclerView(rvTasks);

        // Setup Swipe Actions for rvCompleted (Swipe right to un-complete)
        ItemTouchHelper completedHelper = new ItemTouchHelper(new TaskSwipeHelper(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Task task = completedAdapter.getCurrentList().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    viewModel.setCompleted(task, false);
                } else if (direction == ItemTouchHelper.LEFT) {
                    viewModel.delete(task);
                }
            }
        });
        completedHelper.attachToRecyclerView(rvCompleted);

        // Observe today tasks
        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            latestTodayTasks = tasks != null ? tasks : new ArrayList<>();
            totalTasks = latestTodayTasks.size();
            applyFilter();
            updateStats();
            updateNextUpCard();
        });

        viewModel.getUpcomingTasks().observe(getViewLifecycleOwner(), tasks -> {
            latestUpcomingTasks = tasks != null ? tasks : new ArrayList<>();
            updateNextUpCard();
        });

        // Observe completed
        viewModel.getTodayCompletedTasks().observe(getViewLifecycleOwner(), tasks -> {
            completedAdapter.submitList(tasks);
            completedTasks = tasks != null ? tasks.size() : 0;
            
            boolean show = tasks != null && !tasks.isEmpty();
            completedSection.setVisibility(show ? View.VISIBLE : View.GONE);
            
            updateStats();
        });
        
        // View All click listener
        TextView tvViewAll = view.findViewById(R.id.tv_view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                // Navigate to All Tasks - handled by activity
                if (getActivity() != null) {
                    // You can implement navigation here
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNextUpCard();
    }
    
    private void setupFilters() {
        if (chipGroupFilters == null) return;
        chipGroupFilters.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) {
                group.check(R.id.chip_filter_all);
                return;
            }
            int id = ids.get(0);
            if (id == R.id.chip_filter_priority) activeFilter = FILTER_PRIORITY;
            else if (id == R.id.chip_filter_reminder) activeFilter = FILTER_REMINDER;
            else if (id == R.id.chip_filter_no_due) activeFilter = FILTER_NO_DUE;
            else if (id == R.id.chip_filter_overdue) activeFilter = FILTER_OVERDUE;
            else activeFilter = FILTER_ALL;
            applyFilter();
        });
    }

    private void setupQuickActions() {
        if (cardQuickAdd != null) {
            cardQuickAdd.setOnClickListener(v -> {
                AddTaskBottomSheet sheet = new AddTaskBottomSheet();
                sheet.show(getParentFragmentManager(), AddTaskBottomSheet.TAG);
            });
        }

        if (cardGoCalendar != null) {
            cardGoCalendar.setOnClickListener(v -> selectBottomNav(R.id.nav_calendar, getString(R.string.nav_calendar)));
        }

        if (cardGoStats != null) {
            cardGoStats.setOnClickListener(v -> selectBottomNav(R.id.nav_stats, getString(R.string.nav_stats)));
        }

        if (cardNextUp != null) {
            cardNextUp.setOnClickListener(v -> {
                if (nextUpTask != null) {
                    AddTaskBottomSheet sheet = AddTaskBottomSheet.newInstance(nextUpTask);
                    sheet.show(getParentFragmentManager(), AddTaskBottomSheet.TAG);
                } else {
                    AddTaskBottomSheet sheet = new AddTaskBottomSheet();
                    sheet.show(getParentFragmentManager(), AddTaskBottomSheet.TAG);
                }
            });
        }
    }

    private void selectBottomNav(int menuId, String title) {
        BottomNavigationView bottom = getActivity() != null ? getActivity().findViewById(R.id.bottom_navigation) : null;
        if (bottom != null) bottom.setSelectedItemId(menuId);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }

    private void applyFilter() {
        List<Task> filtered = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Task task : latestTodayTasks) {
            switch (activeFilter) {
                case FILTER_PRIORITY:
                    if (task.priority == Task.PRIORITY_HIGH) filtered.add(task);
                    break;
                case FILTER_REMINDER:
                    if (task.reminderTime > 0) filtered.add(task);
                    break;
                case FILTER_NO_DUE:
                    if (task.dueDate == 0) filtered.add(task);
                    break;
                case FILTER_OVERDUE:
                    if (!task.isCompleted && task.dueDate > 0 && task.dueDate < now) filtered.add(task);
                    break;
                default:
                    filtered.add(task);
                    break;
            }
        }

        taskAdapter.submitList(filtered);

        boolean isEmpty = filtered.isEmpty();
        emptyStateContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (tvFilterStatus != null) {
            String label;
            switch (activeFilter) {
                case FILTER_PRIORITY: label = getString(R.string.filter_high_priority); break;
                case FILTER_REMINDER: label = getString(R.string.filter_has_reminder); break;
                case FILTER_NO_DUE: label = getString(R.string.filter_no_due); break;
                case FILTER_OVERDUE: label = getString(R.string.filter_overdue); break;
                default: label = getString(R.string.filter_all); break;
            }
            tvFilterStatus.setText(getString(R.string.filter_status, label, filtered.size()));
        }

        if (isEmpty && tvEmptyTitle != null) {
            tvEmptyTitle.setText(R.string.all_caught_up);
            tvEmptyState.setText(R.string.tap_to_add_task);
        }
    }
    
    private void updateStats() {
        int total = totalTasks + completedTasks;
        tvTaskCount.setText(String.valueOf(totalTasks));
        
        if (total > 0) {
            int percent = (int) ((completedTasks * 100.0f) / total);
            tvProgressPercent.setText(percent + "%");
            
            // Animate progress
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressTasks, "progress", progressTasks.getProgress(), percent);
            progressAnimator.setDuration(500);
            progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            progressAnimator.start();
        } else {
            tvProgressPercent.setText("0%");
            progressTasks.setProgress(0);
        }
    }

    private void updateNextUpCard() {
        if (cardNextUp == null) return;

        boolean enabled = UiPreferences.isNextUpCardEnabled(requireContext());
        cardNextUp.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (!enabled) return;

        nextUpTask = pickNextTask();
        if (nextUpTask == null) {
            tvNextUpTitle.setText(R.string.next_up_title);
            tvNextUpDue.setVisibility(View.GONE);
            tvNextUpEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvNextUpTitle.setText(nextUpTask.title != null && !nextUpTask.title.isEmpty()
                ? nextUpTask.title
                : getString(R.string.task_title_hint));
        tvNextUpEmpty.setVisibility(View.GONE);

        if (nextUpTask.dueDate > 0) {
            Calendar due = Calendar.getInstance();
            due.setTimeInMillis(nextUpTask.dueDate);
            Calendar today = Calendar.getInstance();

            String timePart = DateFormat.format("HH:mm", due).toString();
            String dueLabel;
            if (isSameDay(due, today)) {
                dueLabel = getString(R.string.next_up_due_today, timePart);
            } else {
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                if (isSameDay(due, tomorrow)) {
                    dueLabel = getString(R.string.next_up_due_tomorrow, timePart);
                } else {
                    String datePart = DateFormat.format("MMM d", due).toString();
                    dueLabel = getString(R.string.next_up_due_future, datePart, timePart);
                }
            }
            tvNextUpDue.setText(dueLabel);
            tvNextUpDue.setVisibility(View.VISIBLE);
        } else {
            tvNextUpDue.setVisibility(View.GONE);
        }
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private Task pickNextTask() {
        List<Task> pool = new ArrayList<>();
        for (Task t : latestTodayTasks) {
            if (!t.isCompleted) pool.add(t);
        }
        for (Task t : latestUpcomingTasks) {
            if (!t.isCompleted) pool.add(t);
        }
        if (pool.isEmpty()) return null;

        long now = System.currentTimeMillis();
        Task best = null;
        for (Task task : pool) {
            if (task.dueDate == 0) continue; // skip undated first pass
            if (best == null || task.dueDate < best.dueDate ||
                    (task.dueDate == best.dueDate && task.priority > best.priority)) {
                if (task.dueDate >= now || best == null) {
                    best = task;
                }
            }
        }

        if (best != null) return best;

        // fallback to undated tasks, pick newest
        for (Task task : pool) {
            if (task.dueDate == 0) {
                if (best == null || task.createdAt < best.createdAt) {
                    best = task;
                }
            }
        }
        return best;
    }
}
