package hcmute.edu.vn.tick_tick.ui.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.ui.sheet.AddTaskBottomSheet;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    private final TaskViewModel viewModel;
    private final FragmentManager fragmentManager;
    private boolean completedMode = false;

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.id == newItem.id;
        }
        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.title.equals(newItem.title)
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
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View priorityStrip;
        private final ImageView ivCheckbox;
        private final TextView tvTitle;
        private final TextView tvNotes;
        private final TextView tvDueDate;
        private final View root;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            priorityStrip = itemView.findViewById(R.id.priority_strip);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvNotes = itemView.findViewById(R.id.tv_task_notes);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
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

            // Due date
            if (task.dueDate > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                String dateStr = sdf.format(new Date(task.dueDate));
                tvDueDate.setText(dateStr);
                tvDueDate.setVisibility(View.VISIBLE);
                if (task.isOverdue()) {
                    tvDueDate.setTextColor(itemView.getContext().getColor(R.color.color_overdue));
                } else {
                    tvDueDate.setTextColor(itemView.getContext().getColor(R.color.color_text_secondary));
                }
            } else {
                tvDueDate.setVisibility(View.GONE);
            }

            // Priority strip color
            int color;
            switch (task.priority) {
                case Task.PRIORITY_HIGH:
                    color = itemView.getContext().getColor(R.color.priority_high);
                    break;
                case Task.PRIORITY_MEDIUM:
                    color = itemView.getContext().getColor(R.color.priority_medium);
                    break;
                case Task.PRIORITY_LOW:
                    color = itemView.getContext().getColor(R.color.priority_low);
                    break;
                default:
                    color = itemView.getContext().getColor(R.color.priority_none);
                    break;
            }
            priorityStrip.setBackgroundColor(color);

            // Completed styling
            if (task.isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
                ivCheckbox.setImageResource(R.drawable.bg_checkbox_checked);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1f);
                ivCheckbox.setImageResource(R.drawable.bg_checkbox_unchecked);
            }

            // Checkbox click to complete
            ivCheckbox.setOnClickListener(v -> {
                Animation anim = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_out);
                anim.setDuration(150);
                root.startAnimation(anim);
                viewModel.setCompleted(task, !task.isCompleted);
            });

            // Row click to edit
            root.setOnClickListener(v -> {
                AddTaskBottomSheet sheet = AddTaskBottomSheet.newInstance(task);
                sheet.show(fragmentManager, AddTaskBottomSheet.TAG);
            });
        }
    }
}
