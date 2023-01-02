package persistence.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import persistence.entities.Directory;

@Dao
public interface DirectoryDao {
    @Query("SELECT * FROM " + Directory.TABLE_NAME)
    Directory[] getAllDirectories();

    @Query("SELECT * FROM " + Directory.TABLE_NAME + " WHERE phoneDirectory = :directory")
    Directory getDirectoryByName(String directory);

    @Insert
    void insertDirectory(Directory directory);

    @Delete
    void deleteDirectory(Directory directory);
}
