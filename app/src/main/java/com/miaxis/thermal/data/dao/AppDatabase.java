package com.miaxis.thermal.data.dao;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.miaxis.thermal.data.dao.converter.DateConverter;
import com.miaxis.thermal.data.dao.converter.StringListConverter;
import com.miaxis.thermal.data.entity.Calibration;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.util.FileUtil;

import java.io.File;

@Database(entities = {Config.class, Person.class, Record.class, Calibration.class}, version = 7)
@TypeConverters(value = {StringListConverter.class, DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DBName = FileUtil.MAIN_PATH + File.separator + "Thermal.db";

    private static AppDatabase instance;

    public static AppDatabase getInstance () {
        return instance;
    }

    //should be init first
    public static void initDB(Application application) {
        instance = createDB(application);
    }

    private static AppDatabase createDB(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, DBName)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                    }
                })
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build();
    }

    private static Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("create table Calibration (id INTEGER primary key, xhnEmissivity INTEGER not null, xhnModel INTEGER not null)");
        }
    };

    private static Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN forcedMask INTEGER not null default 0");
        }
    };

    private static Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN strangerRecord INTEGER not null default 0");
            database.execSQL("ALTER TABLE Config ADD COLUMN tempRealTime INTEGER not null default 0");
        }
    };

    private static Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN deviceMode INTEGER not null default 0");
            database.execSQL("ALTER TABLE Config ADD COLUMN failedVerifyCold INTEGER not null default 2");
        }
    };

    public abstract ConfigDao configDao();

    public abstract PersonDao personDao();

    public abstract RecordDao recordDao();

    public abstract CalibrationDao calibrationDao();

}
