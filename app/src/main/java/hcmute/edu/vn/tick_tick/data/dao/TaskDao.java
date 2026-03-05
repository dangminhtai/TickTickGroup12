package hcmute.edu.vn.tick_tick.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.tick_tick.data.model.Task;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskById(int taskId);

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY priority DESC, due_date ASC, created_at DESC")
    LiveData<List<Task>> getAllActiveTasks();

    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, priority DESC, due_date ASC, created_at DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE list_id = :listId ORDER BY is_completed ASC, priority DESC, created_at DESC")
    LiveData<List<Task>> getTasksByList(int listId);

    // Home: all active tasks — due today, overdue, OR no date at all
    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY " +
           "CASE WHEN due_date > 0 AND due_date < :tomorrowMidnight THEN 0 " +   // overdue/today first
           "WHEN due_date = 0 THEN 1 " +                                          // undated next
           "ELSE 2 END ASC, " +                                                   // future last
           "priority DESC, created_at DESC")
    LiveData<List<Task>> getTodayTasks(long tomorrowMidnight);

    // Completed tasks for today
    @Query("SELECT * FROM tasks WHERE is_completed = 1 AND completed_at >= :todayMidnight ORDER BY completed_at DESC")
    LiveData<List<Task>> getTodayCompletedTasks(long todayMidnight);

    // Upcoming tasks (tomorrow and beyond, not completed)
    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND due_date >= :tomorrowMidnight ORDER BY due_date ASC, priority DESC")
    LiveData<List<Task>> getUpcomingTasks(long tomorrowMidnight);

    // Tasks for a specific date
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay ORDER BY is_completed ASC, priority DESC")
    LiveData<List<Task>> getTasksForDate(long startOfDay, long endOfDay);

    // Inbox: tasks with listId = 1 or null
    @Query("SELECT * FROM tasks WHERE (list_id = 1 OR list_id IS NULL) AND is_completed = 0 ORDER BY priority DESC, created_at DESC")
    LiveData<List<Task>> getInboxTasks();

    @Query("UPDATE tasks SET is_completed = :completed, completed_at = :completedAt WHERE id = :taskId")
    void setTaskCompleted(int taskId, boolean completed, long completedAt);

    @Query("DELETE FROM tasks WHERE is_completed = 1")
    void deleteAllCompleted();

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND due_date > 0 AND due_date < :tomorrowMidnight")
    LiveData<Integer> getTodayTaskCount(long tomorrowMidnight);
}
