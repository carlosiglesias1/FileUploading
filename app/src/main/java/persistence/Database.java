package persistence;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import persistence.daos.DirectoryDao;
import persistence.daos.FtpDao;
import persistence.entities.ClientData;
import persistence.entities.Directory;

@androidx.room.Database(version = 2,
        entities = {ClientData.class, Directory.class})
public abstract class Database extends RoomDatabase {
    public static final String DATABASE_NAME = "file_uploading";

    public abstract FtpDao ftpDao();

    public abstract DirectoryDao directoryDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE " + ClientData.TABLE_NAME + " ADD COLUMN FtpPort INTEGER NOT NULL DEFAULT 21");
        }
    };
}
