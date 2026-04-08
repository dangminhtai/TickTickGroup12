package hcmute.edu.vn.tick_tick.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import hcmute.edu.vn.tick_tick.data.dao.TaskDao;
import hcmute.edu.vn.tick_tick.data.dao.TaskListDao;
import hcmute.edu.vn.tick_tick.data.database.AppDatabase;
import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.data.model.TaskList;

public class TaskRepository {

    private final TaskDao taskDao;
    private final TaskListDao taskListDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface OnTaskInsertedListener {
        void onInserted(long id);
    }

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        taskListDao = db.taskListDao();
        executor = AppDatabase.executor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ---- Task Operations ----

    public void insert(Task task, Runnable onSuccess) {
        insert(task, id -> {
            if (onSuccess != null) {
                mainHandler.post(onSuccess);
            }
        });
    }

    public void insert(Task task, OnTaskInsertedListener listener) {
        Log.d("TaskRepository", "insert(with listener) called for task title='" + task.title + "'");
        executor.execute(() -> {
            long id = -1;
            try {
                id = taskDao.insert(task);
                Log.d("TaskRepository", "insert() completed, rowId=" + id);
            } catch (Exception e) {
                Log.e("TaskRepository", "Error inserting task", e);
            }
            if (listener != null) {
                long finalId = id;
                mainHandler.post(() -> listener.onInserted(finalId));
            }
        });
    }

    public void update(Task task) {
        executor.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executor.execute(() -> taskDao.delete(task));
    }

    public void setCompleted(Task task, boolean completed) {
        long completedAt = completed ? System.currentTimeMillis() : 0L;
        task.isCompleted = completed;
        task.completedAt = completedAt;
        executor.execute(() -> taskDao.setTaskCompleted(task.id, completed, completedAt));
    }

    public void deleteAllCompleted() {
        executor.execute(() -> taskDao.deleteAllCompleted());
    }

    public LiveData<List<Task>> getAllActiveTasks() {
        return taskDao.getAllActiveTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTodayTasks() {
        return taskDao.getTodayTasks(getTodayMidnight(), getTomorrowMidnight());
    }

    public LiveData<List<Task>> getTodayCompletedTasks() {
        return taskDao.getTodayCompletedTasks(getTodayMidnight());
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        return taskDao.getUpcomingTasks(getTomorrowMidnight());
    }

    public LiveData<List<Task>> getTasksByList(int listId) {
        return taskDao.getTasksByList(listId);
    }

    public LiveData<List<Task>> getTasksForDate(long dateMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        return taskDao.getTasksForDate(startOfDay, endOfDay);
    }

    public LiveData<List<Task>> getInboxTasks() {
        return taskDao.getInboxTasks();
    }

    public LiveData<Integer> getTodayTaskCount() {
        return taskDao.getTodayTaskCount(getTodayMidnight(), getTomorrowMidnight());
    }

    // ---- TaskList Operations ----

    public void insertList(TaskList list) {
        executor.execute(() -> taskListDao.insert(list));
    }

    public void updateList(TaskList list) {
        executor.execute(() -> taskListDao.update(list));
    }

    public void deleteList(TaskList list) {
        executor.execute(() -> taskListDao.delete(list));
    }

    public LiveData<List<TaskList>> getAllLists() {
        return taskListDao.getAllLists();
    }

    // ---- Search & Filter ----

    public LiveData<List<Task>> searchTasks(String keyword) {
        return taskDao.searchTasks(keyword);
    }

    public LiveData<List<Task>> getActiveTasksByPriority(int priority) {
        return taskDao.getActiveTasksByPriority(priority);
    }

    // ---- Helpers ----

    private long getTodayMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getTomorrowMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    public LiveData<List<Task>> getOverdueTasks() {
        long now = System.currentTimeMillis();
        return taskDao.getOverdueTasks(now);
    }
}
