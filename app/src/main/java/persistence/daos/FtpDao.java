package persistence.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import persistence.entities.ClientData;

@Dao
public interface FtpDao {
    int SQL_BOOL_TRUE = 1;
    int SQL_BOOL_FALSE = 0;

    @Insert
    void instertFtp(ClientData newFtp);

    @Update
    void updateClient(ClientData ftp);

    @Query("SELECT * FROM " + ClientData.TABLE_NAME)
    ClientData[] loadAllFtps();

    @Query("SELECT * FROM ftpclient WHERE id = :id")
    ClientData getClient(int id);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN " + SQL_BOOL_TRUE + " ELSE " + SQL_BOOL_FALSE + " END FROM ftpclient")
    boolean anyFtp();

    @Query("SELECT * FROM ftpclient WHERE IsActive = " + SQL_BOOL_TRUE)
    ClientData getActiveFtp();
}
