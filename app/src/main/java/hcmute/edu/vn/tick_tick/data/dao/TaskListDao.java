package hcmute.edu.vn.tick_tick.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.tick_tick.data.model.TaskList;

@Dao
public interface TaskListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TaskList taskList);

    @Update
    void update(TaskList taskList);

    @Delete
    void delete(TaskList taskList);

    @Query("SELECT * FROM task_lists ORDER BY created_at ASC")
    LiveData<List<TaskList>> getAllLists();

    @Query("SELECT * FROM task_lists WHERE id = :id")
    TaskList getListById(int id);

    @Query("SELECT COUNT(*) FROM task_lists")
    int getCount();
}
