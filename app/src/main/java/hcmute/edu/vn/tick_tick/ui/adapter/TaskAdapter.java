package hcmute.edu.vn.tick_tick.ui.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;
import hcmute.edu.vn.tick_tick.util.StreakManager;
import hcmute.edu.vn.tick_tick.util.XPManager;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private final TaskViewModel viewModel;
    private final FragmentManager fragmentManager;
    private boolean completedMode = false;
    private int lastAnimatedPosition = -1;

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.id == newItem.id;
        }
        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return Objects.equals(oldItem.title, newItem.title)
                && oldItem.isCompleted == newItem.isCompleted
                && oldItem.priority == newItem.priority
                && oldItem.dueDate == newItem.dueDate;
        }
    };

    public TaskAdapter(TaskViewModel viewModel, FragmentManager fragmentManager) {
        super(DIFF_CALLBACK);
        this.viewModel = viewModel;
        this.fragmentManager = fragmentManager;
    }

    public void setCompletedMode(boolean completedMode) {
        this.completedMode = completedMode;
    }

    public void smartSort() {
        List<Task> currentList = new ArrayList<>(getCurrentList());
        Collections.sort(currentList, (t1, t2) -> {
            // Calculate score for t1
            long score1 = calculateSmartScore(t1);
            // Calculate score for t2
            long score2 = calculateSmartScore(t2);
            
            // Higher score first
            return Long.compare(score2, score1);
        });
        submitList(currentList);
    }

    private long calculateSmartScore(Task task) {
        long score = 0;
        
        // Priority weight: High(3000), Medium(2000), Low(1000), None(0)
        score += task.priority * 1000L;
        
        // Deadline weight: The closer the deadline, the higher the score
        if (task.dueDate > 0) {
            long now = System.currentTimeMillis();
            long timeLeft = task.dueDate - now;
            
            if (timeLeft < 0) {
                // Overdue tasks are very important (+5000)
                score += 5000;
            } else {
                // Closer to now (e.g., within 24h) gets more boost
                long oneDay = 24 * 60 * 60 * 1000;
                if (timeLeft < oneDay) {
                    score += (oneDay - timeLeft) / (60 * 60 * 1000); // 1 point per hour closer
                }
            }
        }
        
        return score;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
        
        // Animate item entrance
        if (position > lastAnimatedPosition) {
            animateItemEntrance(holder.itemView, position);
            lastAnimatedPosition = position;
        }
    }
    
    private void animateItemEntrance(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        // Cap the start delay to prevent late items from taking too long to appear
        long delay = Math.min(position * 50L, 500L);
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay(delay)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View priorityStrip;
        private final FrameLayout checkboxContainer;
        private final ImageView ivCheckbox;
        private final TextView tvTitle;
        private final TextView tvNotes;
        private final TextView tvDueDate;
        private final TextView tvPriorityBadge;
        private final TextView tvTags;
        private final LinearLayout infoRow;
        private final MaterialCardView cardTask;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.card_task);
            priorityStrip = itemView.findViewById(R.id.priority_strip);
            checkboxContainer = itemView.findViewById(R.id.checkbox_container);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvNotes = itemView.findViewById(R.id.tv_task_notes);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvPriorityBadge = itemView.findViewById(R.id.tv_priority_badge);
            tvTags = itemView.findViewById(R.id.tv_tags);
            infoRow = itemView.findViewById(R.id.info_row);
        }

        void bind(Task task) {
            tvTitle.setText(task.title);

            // Notes
            if (task.notes != null && !task.notes.isEmpty()) {
                tvNotes.setText(task.notes);
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            // Due date & Priority badge
            boolean showInfoRow = false;
            
            if (task.dueDate > 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(task.dueDate);
                String pattern = "MMM d";
                if (cal.get(java.util.Calendar.HOUR_OF_DAY) != 23 || cal.get(java.util.Calendar.MINUTE) != 59) {
                    pattern = "MMM d, HH:mm";
                }
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                String dateStr = sdf.format(new Date(task.dueDate));
                tvDueDate.setText(dateStr);
                tvDueDate.setVisibility(View.VISIBLE);
                showInfoRow = true;
                
                if (task.isOverdue()) {
                    tvDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_overdue));
                    GradientDrawable bgDrawable = (GradientDrawable) ContextCompat.getDrawable(
                            itemView.getContext(), R.drawable.bg_date_chip);
                    if (bgDrawable != null) {
                        bgDrawable.setColor(ContextCompat.getColor(itemView.getContext(), R.color.color_overdue_bg));
                        tvDueDate.setBackground(bgDrawable);
                    }
                } else {
                    tvDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_text_secondary));
                    GradientDrawable bgDrawable = (GradientDrawable) ContextCompat.getDrawable(
                            itemView.getContext(), R.drawable.bg_date_chip);
                    if (bgDrawable != null) {
                        bgDrawable.setColor(ContextCompat.getColor(itemView.getContext(), R.color.color_surface_variant));
                        tvDueDate.setBackground(bgDrawable);
                    }
                }
            } else {
                tvDueDate.setVisibility(View.GONE);
            }

            // Priority badge
            if (task.priority != Task.PRIORITY_NONE) {
                tvPriorityBadge.setVisibility(View.VISIBLE);
                showInfoRow = true;
                
                int textColor, bgColor;
                String priorityText;
                
                switch (task.priority) {
                    case Task.PRIORITY_HIGH:
                        textColor = R.color.priority_high;
                        bgColor = R.color.priority_high_bg;
                        priorityText = "HIGH";
                        break;
                    case Task.PRIORITY_MEDIUM:
                        textColor = R.color.priority_medium;
                        bgColor = R.color.priority_medium_bg;
                        priorityText = "MED";
                        break;
                    case Task.PRIORITY_LOW:
                        textColor = R.color.priority_low;
                        bgColor = R.color.priority_low_bg;
                        priorityText = "LOW";
                        break;
                    default:
                        textColor = R.color.color_text_hint;
                        bgColor = R.color.priority_none_bg;
                        priorityText = "";
                        break;
                }
                
                tvPriorityBadge.setText(priorityText);
                tvPriorityBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
                GradientDrawable badgeBg = (GradientDrawable) ContextCompat.getDrawable(
                        itemView.getContext(), R.drawable.bg_priority_badge);
                if (badgeBg != null) {
                    badgeBg.setColor(ContextCompat.getColor(itemView.getContext(), bgColor));
                    tvPriorityBadge.setBackground(badgeBg);
                }
            } else {
                tvPriorityBadge.setVisibility(View.GONE);
            }

            // Tags
            if (task.tags != null && !task.tags.trim().isEmpty()) {
                tvTags.setText(task.tags.trim());
                tvTags.setVisibility(View.VISIBLE);
                showInfoRow = true;
            } else {
                tvTags.setVisibility(View.GONE);
            }
            
            infoRow.setVisibility(showInfoRow ? View.VISIBLE : View.GONE);

            // Priority strip color
            int color;
            switch (task.priority) {
                case Task.PRIORITY_HIGH:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                    break;
                case Task.PRIORITY_MEDIUM:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
                    break;
                case Task.PRIORITY_LOW:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_low);
                    break;
                default:
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_none);
                    break;
            }
            
            GradientDrawable stripDrawable = (GradientDrawable) ContextCompat.getDrawable(
                    itemView.getContext(), R.drawable.bg_priority_strip);
            if (stripDrawable != null) {
                stripDrawable.setColor(color);
                priorityStrip.setBackground(stripDrawable);
            }

            // Completed styling with animation
            if (task.isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
                tvNotes.setAlpha(0.4f);
                ivCheckbox.setImageResource(R.drawable.bg_checkbox_checked);
                cardTask.setAlpha(0.7f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1f);
                tvNotes.setAlpha(1f);
                ivCheckbox.setImageResource(R.drawable.bg_checkbox_unchecked);
                cardTask.setAlpha(1f);
            }

            // Checkbox click with bounce animation
            checkboxContainer.setOnClickListener(v -> {
                animateCheckbox(ivCheckbox, !task.isCompleted);
                // Award XP when completing (not when un-completing)
                if (!task.isCompleted) {
                    XPManager xpManager = new XPManager(v.getContext());
                    int earned = xpManager.awardXP(task.priority);
                    
                    StreakManager streakManager = new StreakManager(v.getContext());
                    streakManager.onTaskCompleted();
                    
                    android.widget.Toast.makeText(v.getContext(),
                            "+" + earned + " XP 🎉",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                viewModel.setCompleted(task, !task.isCompleted);
            });

            // Row click to edit with ripple
            cardTask.setOnClickListener(v -> {
                AddTaskBottomSheet sheet = AddTaskBottomSheet.newInstance(task);
                sheet.show(fragmentManager, AddTaskBottomSheet.TAG);
            });
        }
        
        private void animateCheckbox(ImageView checkbox, boolean isCompleting) {
            // Scale down then up with bounce
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(checkbox, "scaleX", 1f, 0.7f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(checkbox, "scaleY", 1f, 0.7f);
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(checkbox, "scaleX", 0.7f, 1.1f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(checkbox, "scaleY", 0.7f, 1.1f, 1f);
            
            scaleDownX.setDuration(100);
            scaleDownY.setDuration(100);
            scaleUpX.setDuration(200);
            scaleUpY.setDuration(200);
            scaleUpX.setInterpolator(new OvershootInterpolator());
            scaleUpY.setInterpolator(new OvershootInterpolator());
            
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(scaleDownX, scaleDownY);
            
            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(scaleUpX, scaleUpY);
            
            AnimatorSet fullAnimation = new AnimatorSet();
            fullAnimation.playSequentially(scaleDown, scaleUp);
            fullAnimation.start();
        }
    }
}
