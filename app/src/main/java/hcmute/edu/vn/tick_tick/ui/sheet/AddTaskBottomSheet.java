package hcmute.edu.vn.tick_tick.ui.sheet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.TextWatcher;
import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.util.ReminderHelper;
import hcmute.edu.vn.tick_tick.util.NLPParser;
import hcmute.edu.vn.tick_tick.util.AINoteHelper;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AddTaskBottomSheet";
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_NOTES = "task_notes";
    private static final String ARG_TASK_PRIORITY = "task_priority";
    private static final String ARG_TASK_DUE = "task_due";
    private static final String ARG_TASK_TAGS = "task_tags";
    private static final String ARG_TASK_REMINDER = "task_reminder";
    private static final String ARG_TASK_RECURRING = "task_recurring";

    private TaskViewModel viewModel;
    private EditText etTitle, etNotes, etTags;
    private TextView tvDueDateLabel, tvReminderLabel, tvRepeatLabel, tvAiSummary, tvAiTags;
    private Button btnSave;
    private ImageView btnDatePicker;
    private ChipGroup priorityChipGroup;

    private Calendar selectedDate = null;
    private Calendar selectedReminderTime = null;
    private String selectedRecurring = Task.RECURRING_NONE;
    private int selectedPriority = Task.PRIORITY_NONE;
    private Task editingTask = null;
    private final Handler nlpHandler = new Handler(Looper.getMainLooper());
    private Runnable nlpRunnable;
    private final Handler aiHandler = new Handler(Looper.getMainLooper());
    private Runnable aiRunnable;

    public static AddTaskBottomSheet newInstance(Task task) {
        AddTaskBottomSheet sheet = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, task.id);
        args.putString(ARG_TASK_TITLE, task.title);
        args.putString(ARG_TASK_NOTES, task.notes != null ? task.notes : "");
        args.putInt(ARG_TASK_PRIORITY, task.priority);
        args.putLong(ARG_TASK_DUE, task.dueDate);
        args.putString(ARG_TASK_TAGS, task.tags != null ? task.tags : "");
        args.putLong(ARG_TASK_REMINDER, task.reminderTime);
        args.putString(ARG_TASK_RECURRING, task.recurringType != null ? task.recurringType : "");
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
        etTags = view.findViewById(R.id.et_task_tags);
        tvDueDateLabel = view.findViewById(R.id.tv_due_date_label);
        tvReminderLabel = view.findViewById(R.id.tv_reminder_label);
        tvRepeatLabel = view.findViewById(R.id.tv_repeat_label);
        tvAiSummary = view.findViewById(R.id.tv_ai_summary);
        tvAiTags = view.findViewById(R.id.tv_ai_tags);
        btnSave = view.findViewById(R.id.btn_save_task);
        btnDatePicker = view.findViewById(R.id.btn_date_picker);
        priorityChipGroup = view.findViewById(R.id.chip_group_priority);

        // Reminder card click – show reminder time picker
        View cardReminder = view.findViewById(R.id.card_reminder);
        if (cardReminder != null) {
            cardReminder.setOnClickListener(v -> showReminderTimePicker());
        }

        // Repeat card click – show repeat dialog
        View cardRepeat = view.findViewById(R.id.card_repeat);
        if (cardRepeat != null) {
            cardRepeat.setOnClickListener(v -> showRepeatDialog());
        }

        // Check if editing existing task
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_TASK_ID)) {
            editingTask = new Task();
            editingTask.id = args.getInt(ARG_TASK_ID);
            editingTask.title = args.getString(ARG_TASK_TITLE, "");
            editingTask.notes = args.getString(ARG_TASK_NOTES, "");
            editingTask.priority = args.getInt(ARG_TASK_PRIORITY, Task.PRIORITY_NONE);
            editingTask.dueDate = args.getLong(ARG_TASK_DUE, 0);
            editingTask.tags = args.getString(ARG_TASK_TAGS, "");
            editingTask.reminderTime = args.getLong(ARG_TASK_REMINDER, 0);
            editingTask.recurringType = args.getString(ARG_TASK_RECURRING, "");

            etTitle.setText(editingTask.title);
            etNotes.setText(editingTask.notes);
            etTags.setText(editingTask.tags);
            selectedPriority = editingTask.priority;
            selectedRecurring = editingTask.recurringType != null ? editingTask.recurringType : Task.RECURRING_NONE;

            if (editingTask.dueDate > 0) {
                selectedDate = Calendar.getInstance();
                selectedDate.setTimeInMillis(editingTask.dueDate);
                updateDateLabel();
            }

            if (editingTask.reminderTime > 0) {
                selectedReminderTime = Calendar.getInstance();
                selectedReminderTime.setTimeInMillis(editingTask.reminderTime);
                updateReminderLabel();
            }

            updateRepeatLabel();
            updatePriorityChips();
            btnSave.setText(getString(R.string.save));
            TextView tvTitle = view.findViewById(R.id.tv_sheet_title);
            if (tvTitle != null) tvTitle.setText(getString(R.string.edit_task));
        } else {
            // New task: Pre-populate date based on current context
            Long contextDate = viewModel.getSelectedDate().getValue();
            if (contextDate != null && contextDate > 0) {
                 selectedDate = Calendar.getInstance();
                 selectedDate.setTimeInMillis(contextDate);
                 // Set default time to 23:59 (all-day task) since only date was selected
                 selectedDate.set(Calendar.HOUR_OF_DAY, 23);
                 selectedDate.set(Calendar.MINUTE, 59);
                 selectedDate.set(Calendar.SECOND, 0);
                 updateDateLabel();
            }
        }

        // Date picker - now on the card
        View cardDatePicker = view.findViewById(R.id.card_date_picker);
        if (cardDatePicker != null) {
            cardDatePicker.setOnClickListener(v -> showDatePicker());
        }
        btnDatePicker.setOnClickListener(v -> showDatePicker());

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

        // NLP Quick Add with Delay to support Vietnamese TELEX
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (hasComposingText(etTitle)) {
                    // Đang gõ tiếng Việt (composition); không chạm vào text để tránh mất dấu
                    return;
                }

                if (nlpRunnable != null) nlpHandler.removeCallbacks(nlpRunnable);

                nlpRunnable = () -> {
                    String input = s.toString();
                    if (input.length() > 3) {
                        NLPParser.ParseResult result = NLPParser.parse(input);

                        if (result.matchStart != -1 && result.matchEnd != -1) {
                            applyHighlight(s, result.matchStart, result.matchEnd);
                        }

                        if (result.date != null) {
                            selectedDate = Calendar.getInstance();
                            selectedDate.setTimeInMillis(result.date);
                            updateDateLabel();
                        }
                    }
                };
                nlpHandler.postDelayed(nlpRunnable, 1500);
            }
        });

        etTitle.requestFocus();

        // AI suggest tags & summary from notes
        etNotes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (hasComposingText(etNotes)) {
                    return;
                }
                if (aiRunnable != null) aiHandler.removeCallbacks(aiRunnable);
                aiRunnable = () -> applyAiSuggestion(s.toString());
                aiHandler.postDelayed(aiRunnable, 1000);
            }
        });

        // Programmatic autofill hints for API 26+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            etTitle.setAutofillHints(View.AUTOFILL_HINT_NAME);
            etNotes.setAutofillHints(View.AUTOFILL_HINT_POSTAL_ADDRESS);
            etTitle.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            etNotes.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }
    }

    private void applyAiSuggestion(String notes) {
        AINoteHelper.Suggestion suggestion = AINoteHelper.suggest(notes);
        if (tvAiSummary != null) {
            tvAiSummary.setText(!TextUtils.isEmpty(suggestion.summary)
                    ? suggestion.summary
                    : getString(R.string.ai_summary_placeholder));
        }
        if (tvAiTags != null) {
            tvAiTags.setText(suggestion.tags);
        }
        if (etTags != null && TextUtils.isEmpty(etTags.getText())) {
            etTags.setText(suggestion.tags);
        }
    }

    private void applyHighlight(Editable s, int start, int end) {
        // Skip if still composing (Vietnamese input)
        if (hasComposingText(etTitle)) {
            return;
        }
        
        // Validate bounds
        if (start < 0 || end > s.length() || start >= end) {
            return;
        }
        
        // Remove existing ForegroundColorSpan only (not composing spans)
        ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            s.removeSpan(span);
        }

        // Apply primary color to the recognized part
        int highlightColor = getResources().getColor(R.color.color_primary);
        s.setSpan(new ForegroundColorSpan(highlightColor),
                 start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private boolean hasComposingText(EditText editText) {
        Editable text = editText.getText();
        if (text == null) return false;
        Object[] spans = text.getSpans(0, text.length(), Object.class);
        for (Object span : spans) {
            int flags = text.getSpanFlags(span);
            if ((flags & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
                return true;
            }
        }
        return false;
    }

    // ---- Date & Time ----

    private void showDatePicker() {
        Calendar cal = selectedDate != null ? selectedDate : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, day, 23, 59, 0);
                    showTimePicker();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = selectedDate != null ? selectedDate : Calendar.getInstance();
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int initialHour = (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59) ? currentHour : cal.get(Calendar.HOUR_OF_DAY);
        int initialMinute = (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) == 59) ? currentMinute : cal.get(Calendar.MINUTE);

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                requireContext(),
                (v, hourOfDay, minute) -> {
                    if (selectedDate != null) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        selectedDate.set(Calendar.SECOND, 0);
                    }
                    updateDateLabel();
                },
                initialHour,
                initialMinute,
                true // 24 hour format
        );
        timePickerDialog.setOnCancelListener(di -> updateDateLabel());
        timePickerDialog.show();
    }

    private void updateDateLabel() {
        if (selectedDate != null) {
            String formatPattern = "EEE, MMM d";
            if (selectedDate.get(Calendar.HOUR_OF_DAY) != 23 || selectedDate.get(Calendar.MINUTE) != 59) {
                formatPattern = "EEE, MMM d, HH:mm";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(formatPattern, Locale.getDefault());
            tvDueDateLabel.setText(sdf.format(selectedDate.getTime()));
            tvDueDateLabel.setTextColor(requireContext().getColor(R.color.color_primary));
        }
    }

    // ---- Reminder ----

    private void showReminderTimePicker() {
        Calendar cal = selectedReminderTime != null ? selectedReminderTime : Calendar.getInstance();
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                requireContext(),
                (v, hourOfDay, minute) -> {
                    // Ask for date if not set yet
                    if (selectedReminderTime == null) {
                        selectedReminderTime = Calendar.getInstance();
                    }
                    // Show a DatePicker for the reminder date first
                    showReminderDatePicker(hourOfDay, minute);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.setTitle("Chọn giờ nhắc nhở");
        timePickerDialog.show();
    }

    private void showReminderDatePicker(int hour, int minute) {
        Calendar cal = selectedReminderTime != null ? selectedReminderTime : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> {
                    selectedReminderTime = Calendar.getInstance();
                    selectedReminderTime.set(year, month, day, hour, minute, 0);
                    updateReminderLabel();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.setTitle("Chọn ngày nhắc nhở");
        dialog.show();
    }

    private void updateReminderLabel() {
        if (selectedReminderTime != null && tvReminderLabel != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
            tvReminderLabel.setText(sdf.format(selectedReminderTime.getTime()));
            tvReminderLabel.setTextColor(requireContext().getColor(R.color.color_primary));
        }
    }

    // ---- Repeat ----

    private void showRepeatDialog() {
        String[] options = {"Không lặp", "Hằng ngày", "Hằng tuần", "Hằng tháng"};
        String[] values = {Task.RECURRING_NONE, Task.RECURRING_DAILY, Task.RECURRING_WEEKLY, Task.RECURRING_MONTHLY};

        int current = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selectedRecurring)) { current = i; break; }
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Lặp lại công việc")
                .setSingleChoiceItems(options, current, (dialog, which) -> {
                    selectedRecurring = values[which];
                    updateRepeatLabel();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateRepeatLabel() {
        if (tvRepeatLabel == null) return;
        String label;
        switch (selectedRecurring) {
            case Task.RECURRING_DAILY: label = "Hằng ngày"; break;
            case Task.RECURRING_WEEKLY: label = "Hằng tuần"; break;
            case Task.RECURRING_MONTHLY: label = "Hằng tháng"; break;
            default: label = "Lặp lại"; break;
        }
        tvRepeatLabel.setText(label);
        boolean active = !Task.RECURRING_NONE.equals(selectedRecurring);
        tvRepeatLabel.setTextColor(requireContext().getColor(active ? R.color.color_primary : R.color.color_text_secondary));
    }

    // ---- Priority ----

    private void updatePriorityChips() {
        int chipId = R.id.chip_priority_none;
        if (selectedPriority == Task.PRIORITY_HIGH) chipId = R.id.chip_priority_high;
        else if (selectedPriority == Task.PRIORITY_MEDIUM) chipId = R.id.chip_priority_medium;
        else if (selectedPriority == Task.PRIORITY_LOW) chipId = R.id.chip_priority_low;
        priorityChipGroup.check(chipId);
    }

    // ---- Save ----

    private void saveTask() {
        Log.d(TAG, "saveTask() invoked");
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            etTitle.setError(getString(R.string.task_title_hint));
            return;
        }

        if (editingTask != null) {
            // Cancel old alarm before updating
            ReminderHelper.cancelReminder(requireContext(), editingTask.id);

            editingTask.title = title;
            editingTask.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            editingTask.tags = etTags.getText() != null ? etTags.getText().toString().trim() : "";
            editingTask.priority = selectedPriority;
            editingTask.dueDate = selectedDate != null ? selectedDate.getTimeInMillis() : 0;
            editingTask.reminderTime = selectedReminderTime != null ? selectedReminderTime.getTimeInMillis() : 0;
            editingTask.recurringType = selectedRecurring;

            viewModel.update(editingTask);

            // Re-schedule reminder if set
            if (editingTask.reminderTime > 0) {
                ReminderHelper.scheduleReminder(requireContext(), editingTask.id, editingTask.title, editingTask.reminderTime);
            }
        } else {
            Task task = new Task(title);
            task.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            task.tags = etTags.getText() != null ? etTags.getText().toString().trim() : "";
            task.priority = selectedPriority;
            task.dueDate = selectedDate != null ? selectedDate.getTimeInMillis() : 0;
            task.reminderTime = selectedReminderTime != null ? selectedReminderTime.getTimeInMillis() : 0;
            task.recurringType = selectedRecurring;
            task.listId = null;

            viewModel.insert(task, id -> {
                Log.d(TAG, "Insert callback: task added with id=" + id);
                if (task.reminderTime > 0) {
                    ReminderHelper.scheduleReminder(requireContext(), (int) id, task.title, task.reminderTime);
                }
                Toast.makeText(requireContext(), R.string.task_added, Toast.LENGTH_SHORT).show();
                dismiss();
            });
            return;
        }
        dismiss();
    }
}
