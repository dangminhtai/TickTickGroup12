package hcmute.edu.vn.tick_tick.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.tick_tick.data.dao.TaskDao;
import hcmute.edu.vn.tick_tick.data.dao.TaskListDao;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.data.model.TaskList;

@Database(entities = {Task.class, TaskList.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract TaskListDao taskListDao();

    private static volatile AppDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ticktick_database"
                    )
                    .addCallback(sRoomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Populate default lists: Inbox, Personal, Work
            databaseWriteExecutor.execute(() -> {
                TaskListDao dao = INSTANCE.taskListDao();
                if (dao.getCount() == 0) {
                    TaskList inbox = new TaskList("Inbox", "#1A2744");
                    TaskList personal = new TaskList("Personal", "#27AE60");
                    TaskList work = new TaskList("Work", "#2980B9");
                    dao.insert(inbox);
                    dao.insert(personal);
                    dao.insert(work);
                }
            });
        }
    };
}
