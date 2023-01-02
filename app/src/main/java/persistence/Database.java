package persistence;

import androidx.room.RoomDatabase;

import persistence.daos.DirectoryDao;
import persistence.daos.FtpDao;
import persistence.entities.ClientData;
import persistence.entities.Directory;

@androidx.room.Database(entities = {ClientData.class, Directory.class}, version = 1, exportSchema = true)
public abstract class Database extends RoomDatabase {
    public static final String DATABASE_NAME = "file_uploading";

    public abstract FtpDao ftpDao();

    public abstract DirectoryDao directoryDao();
}
