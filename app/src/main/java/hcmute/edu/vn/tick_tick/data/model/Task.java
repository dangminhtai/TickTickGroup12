package hcmute.edu.vn.tick_tick.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "tasks",
    foreignKeys = @ForeignKey(
        entity = TaskList.class,
        parentColumns = "id",
        childColumns = "list_id",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {@Index("list_id")}
)
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "due_date")
    public long dueDate; // epoch millis, 0 = no date

    @ColumnInfo(name = "priority")
    public int priority; // 0=None, 1=Low, 2=Medium, 3=High

    @ColumnInfo(name = "list_id")
    public Integer listId;

    @ColumnInfo(name = "is_completed")
    public boolean isCompleted;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "completed_at")
    public long completedAt;

    @ColumnInfo(name = "tags")
    public String tags; // comma-separated strings for tags

    @ColumnInfo(name = "reminder_time")
    public long reminderTime; // epoch millis, 0 = no reminder

    @ColumnInfo(name = "recurring_type")
    public String recurringType; // e.g., "DAILY", "WEEKLY", "MONTHLY", null or empty = none


    public static final String RECURRING_NONE = "";
    public static final String RECURRING_DAILY = "DAILY";
    public static final String RECURRING_WEEKLY = "WEEKLY";
    public static final String RECURRING_MONTHLY = "MONTHLY";
    public static final int PRIORITY_NONE = 0;
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_HIGH = 3;

    public Task() {
        this.createdAt = System.currentTimeMillis();
        this.priority = PRIORITY_NONE;
        this.isCompleted = false;
        this.dueDate = 0;
        this.reminderTime = 0;
        this.recurringType = RECURRING_NONE;
    }

    @Ignore
    public Task(String title) {
        this();
        this.title = title;
    }

    public boolean isOverdue() {
        if (dueDate == 0 || isCompleted) return false;
        return dueDate < System.currentTimeMillis();
    }

    public boolean isDueToday() {
        if (dueDate == 0) return false;
        java.util.Calendar taskCal = java.util.Calendar.getInstance();
        taskCal.setTimeInMillis(dueDate);
        java.util.Calendar today = java.util.Calendar.getInstance();
        return taskCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)
                && taskCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR);
    }
}
