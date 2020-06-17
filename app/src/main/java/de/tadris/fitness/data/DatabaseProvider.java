package de.tadris.fitness.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseProvider {
    private static final String DATABASE_NAME = "fito-track";

    public static AppDatabase provide(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("ALTER table workout add descent REAL NOT NULL DEFAULT 0;");
                            database.execSQL("ALTER table workout add ascent REAL NOT NULL DEFAULT 0");

                            database.execSQL("ALTER TABLE workout_sample RENAME TO workout_sample2;");

                            database.execSQL("CREATE TABLE workout_sample (" +
                                    "id INTEGER NOT NULL DEFAULT NULL PRIMARY KEY," +
                                    "relativeTime INTEGER NOT NULL DEFAULT NULL," +
                                    "elevation REAL NOT NULL DEFAULT NULL," +
                                    "absoluteTime INTEGER NOT NULL DEFAULT NULL," +
                                    "lat REAL NOT NULL DEFAULT NULL," +
                                    "lon REAL NOT NULL DEFAULT NULL," +
                                    "speed REAL NOT NULL DEFAULT NULL," +
                                    "workout_id INTEGER NOT NULL DEFAULT NULL," +
                                    "FOREIGN KEY (workout_id) REFERENCES workout(id) ON DELETE CASCADE);");

                            database.execSQL("INSERT INTO workout_sample (id, relativeTime, elevation, absoluteTime, lat, lon, speed, workout_id) " +
                                    "SELECT id, relativeTime, elevation, absoluteTime, lat, lon, speed, workout_id FROM workout_sample2");

                            database.execSQL("DROP TABLE workout_sample2");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(2, 3) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("ALTER table workout add COLUMN edited INTEGER not null default 0");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(3, 4) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("CREATE TABLE interval (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    delay_millis integer NOT NULL,\n" +
                                    "    queue_id integer NOT NULL,\n" +
                                    "   FOREIGN KEY (queue_id) \n" +
                                    "      REFERENCES interval_queue (id) \n" +
                                    "         ON DELETE CASCADE \n" +
                                    "         ON UPDATE NO ACTION" +
                                    ");");

                            database.execSQL("CREATE TABLE interval_queue (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    state integer not null\n" +
                                    ");");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(3, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("CREATE TABLE interval (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    delay_millis integer NOT NULL,\n" +
                                    "    set_id integer NOT NULL,\n" +
                                    "   FOREIGN KEY (set_id) \n" +
                                    "      REFERENCES interval_set (id) \n" +
                                    "         ON DELETE CASCADE \n" +
                                    "         ON UPDATE NO ACTION" +
                                    ");");

                            database.execSQL("CREATE TABLE interval_set (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    state integer not null\n" +
                                    ");");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(4, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("drop table interval");

                            database.execSQL("drop table interval_queue");

                            database.execSQL("CREATE TABLE interval (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    delay_millis integer NOT NULL,\n" +
                                    "    set_id integer NOT NULL,\n" +
                                    "   FOREIGN KEY (set_id) \n" +
                                    "      REFERENCES interval_set (id) \n" +
                                    "         ON DELETE CASCADE \n" +
                                    "         ON UPDATE NO ACTION" +
                                    ");");

                            database.execSQL("CREATE TABLE interval_set (\n" +
                                    "    id integer NOT NULL primary key,\n" +
                                    "    name text,\n" +
                                    "    state integer not null\n" +
                                    ");");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(5, 6) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("ALTER table workout add COLUMN interval_set_used_id INTEGER not null default 0");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                }, new Migration(6, 7) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        try {
                            database.beginTransaction();

                            database.execSQL("ALTER table workout add COLUMN interval_set_include_pauses INTEGER not null default 0");

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                })
                .allowMainThreadQueries()
                .build();
    }
}
