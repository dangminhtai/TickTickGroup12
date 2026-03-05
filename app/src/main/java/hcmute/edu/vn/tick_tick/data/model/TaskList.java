package hcmute.edu.vn.tick_tick.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_lists")
public class TaskList {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "color")
    public String color; // hex string e.g. "#C9943A"

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // Default lists
    public static final int INBOX_ID = 1;
    public static final String INBOX_COLOR = "#1A2744";
    public static final String PERSONAL_COLOR = "#27AE60";
    public static final String WORK_COLOR = "#2980B9";

    public TaskList() {
        this.createdAt = System.currentTimeMillis();
        this.color = "#9E9E9E";
    }

    @Ignore
    public TaskList(String name, String color) {
        this();
        this.name = name;
        this.color = color;
    }
}
