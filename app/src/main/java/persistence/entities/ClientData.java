package persistence.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ftpclient")
public class ClientData {
    public static final String TABLE_NAME = "ftpclient";

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String FtpName;
    public String FtpHostName;
    public String FtpUser;
    public String FtpPassword;
    public boolean IsActive;
}
