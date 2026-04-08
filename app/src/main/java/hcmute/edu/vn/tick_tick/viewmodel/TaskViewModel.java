package hcmute.edu.vn.tick_tick.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hcmute.edu.vn.tick_tick.data.model.Task;
import hcmute.edu.vn.tick_tick.data.model.TaskList;
import hcmute.edu.vn.tick_tick.data.repository.TaskRepository;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;

    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> todayTasks;
    private final LiveData<List<Task>> todayCompletedTasks;
    private final LiveData<List<Task>> upcomingTasks;
    private final LiveData<List<Task>> inboxTasks;
    private final LiveData<List<TaskList>> allLists;
    private final LiveData<Integer> todayTaskCount;

    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        todayTasks = repository.getTodayTasks();
        todayCompletedTasks = repository.getTodayCompletedTasks();
        upcomingTasks = repository.getUpcomingTasks();
        inboxTasks = repository.getInboxTasks();
        allLists = repository.getAllLists();
        todayTaskCount = repository.getTodayTaskCount();
    }

    public void insert(Task task) {
        repository.insert(task, (Runnable) null);
    }

    // Insert with a callback that runs on the main thread after insertion completes
    public void insert(Task task, Runnable onSuccess) {
        repository.insert(task, onSuccess);
    }

    public void insert(Task task, TaskRepository.OnTaskInsertedListener listener) {
        repository.insert(task, listener);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public void setCompleted(Task task, boolean completed) {
        repository.setCompleted(task, completed);
    }

    public void deleteAllCompleted() {
        repository.deleteAllCompleted();
    }

    public void insertList(TaskList list) {
        repository.insertList(list);
    }

    public void deleteList(TaskList list) {
        repository.deleteList(list);
    }

    public LiveData<List<Task>> getAllTasks() { return allTasks; }
    public LiveData<List<Task>> getTodayTasks() { return todayTasks; }
    public LiveData<List<Task>> getTodayCompletedTasks() { return todayCompletedTasks; }
    public LiveData<List<Task>> getUpcomingTasks() { return upcomingTasks; }
    public LiveData<List<Task>> getInboxTasks() { return inboxTasks; }
    public LiveData<List<TaskList>> getAllLists() { return allLists; }
    public LiveData<Integer> getTodayTaskCount() { return todayTaskCount; }

    public LiveData<List<Task>> getTasksByList(int listId) {
        return repository.getTasksByList(listId);
    }

    public LiveData<List<Task>> getTasksForDate(long dateMillis) {
        return repository.getTasksForDate(dateMillis);
    }

    public void setSelectedDate(long dateMillis) {
        selectedDate.setValue(dateMillis);
    }

    public LiveData<Long> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<Task>> searchTasks(String keyword) {
        return repository.searchTasks(keyword);
    }

    public LiveData<List<Task>> getActiveTasksByPriority(int priority) {
        return repository.getActiveTasksByPriority(priority);
    }
}
