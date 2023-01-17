package persistence;

import static persistence.Database.MIGRATION_1_2;

import android.content.Context;

import androidx.room.Room;

public class DatabaseAccess {
    private static DatabaseAccess instance;
    private Database database;
    private Context context;

    private DatabaseAccess(Context context) {
        this.context = context;
        this.database = Room.databaseBuilder(this.context, Database.class, Database.DATABASE_NAME)
                .addMigrations(MIGRATION_1_2).build();
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null || context != instance.context) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public Database getDatabase() {
        if(!this.database.isOpen())
            this.database = Room.databaseBuilder(this.context, Database.class, Database.DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2).build();
        return this.database;
    }

    public void close() {
        this.database.close();
    }
}
