package hcmute.edu.vn.tick_tick.ui.sheet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AddTaskBottomSheet";
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_NOTES = "task_notes";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_DUE = "task_due";

    private TaskViewModel viewModel;
    private EditText etTitle, etNotes;
    private TextView tvDueDateLabel;
    private Button btnSave;
    private ImageButton btnDatePicker;
    private ChipGroup priorityChipGroup;

    private Calendar selectedDate = null;
    private int selectedPriority = Task.PRIORITY_NONE;
    private Task editingTask = null;

    public static AddTaskBottomSheet newInstance(Task task) {
        AddTaskBottomSheet sheet = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, task.id);
        args.putString(ARG_TASK_TITLE, task.title);
        args.putString(ARG_TASK_NOTES, task.notes != null ? task.notes : "");
        args.putInt(ARG_TASK_PRIORITY, task.priority);
        args.putLong(ARG_TASK_DUE, task.dueDate);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        etTitle = view.findViewById(R.id.et_task_title);
        etNotes = view.findViewById(R.id.et_task_notes);
        tvDueDateLabel = view.findViewById(R.id.tv_due_date_label);
        btnSave = view.findViewById(R.id.btn_save_task);
        btnDatePicker = view.findViewById(R.id.btn_date_picker);
        priorityChipGroup = view.findViewById(R.id.chip_group_priority);

        // Check if editing existing task
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_TASK_ID)) {
            editingTask = new Task();
            editingTask.id = args.getInt(ARG_TASK_ID);
            editingTask.title = args.getString(ARG_TASK_TITLE, "");
            editingTask.notes = args.getString(ARG_TASK_NOTES, "");
            editingTask.priority = args.getInt(ARG_TASK_PRIORITY, Task.PRIORITY_NONE);
            editingTask.dueDate = args.getLong(ARG_TASK_DUE, 0);

            etTitle.setText(editingTask.title);
            etNotes.setText(editingTask.notes);
            selectedPriority = editingTask.priority;

            if (editingTask.dueDate > 0) {
                selectedDate = Calendar.getInstance();
                selectedDate.setTimeInMillis(editingTask.dueDate);
                updateDateLabel();
            }

            updatePriorityChips();
            btnSave.setText(getString(R.string.save));
            TextView tvTitle = view.findViewById(R.id.tv_sheet_title);
            if (tvTitle != null) tvTitle.setText(getString(R.string.edit_task));
        }

        // Date picker
        btnDatePicker.setOnClickListener(v -> {
            Calendar cal = selectedDate != null ? selectedDate : Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (dp, year, month, day) -> {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, day, 23, 59, 0);
                        updateDateLabel();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Priority chips
        priorityChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedPriority = Task.PRIORITY_NONE;
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_priority_high) selectedPriority = Task.PRIORITY_HIGH;
            else if (checkedId == R.id.chip_priority_medium) selectedPriority = Task.PRIORITY_MEDIUM;
            else if (checkedId == R.id.chip_priority_low) selectedPriority = Task.PRIORITY_LOW;
            else selectedPriority = Task.PRIORITY_NONE;
        });

        // Save
        btnSave.setOnClickListener(v -> saveTask());

        etTitle.requestFocus();
    }

    private void updateDateLabel() {
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            tvDueDateLabel.setText(sdf.format(selectedDate.getTime()));
            tvDueDateLabel.setTextColor(requireContext().getColor(R.color.color_accent));
        }
    }

    private void updatePriorityChips() {
        int chipId = R.id.chip_priority_none;
        if (selectedPriority == Task.PRIORITY_HIGH) chipId = R.id.chip_priority_high;
        else if (selectedPriority == Task.PRIORITY_MEDIUM) chipId = R.id.chip_priority_medium;
        else if (selectedPriority == Task.PRIORITY_LOW) chipId = R.id.chip_priority_low;
        priorityChipGroup.check(chipId);
    }

    private void saveTask() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            etTitle.setError(getString(R.string.task_title_hint));
            return;
        }

        if (editingTask != null) {
            // Update
            editingTask.title = title;
            editingTask.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            editingTask.priority = selectedPriority;
            editingTask.dueDate = selectedDate != null ? selectedDate.getTimeInMillis() : 0;
            viewModel.update(editingTask);
        } else {
            // Insert
            Task task = new Task(title);
            task.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            task.priority = selectedPriority;
            task.dueDate = selectedDate != null ? selectedDate.getTimeInMillis() : 0;
            task.listId = 1; // Default: Inbox
            viewModel.insert(task);
        }
        dismiss();
    }
}
