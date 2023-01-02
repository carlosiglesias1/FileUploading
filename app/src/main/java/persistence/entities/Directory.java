package persistence.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "directory")
public class Directory {
    public static final String TABLE_NAME = "directory";

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String phoneDirectory;
}
