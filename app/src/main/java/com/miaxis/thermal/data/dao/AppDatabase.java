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

@Database(entities = {Config.class, Person.class, Record.class, Calibration.class}, version = 4)
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
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    private static Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("create table Calibration (id INTEGER primary key, xhnEmissivity INTEGER not null, xhnModel INTEGER not null)");
        }
    };

    public abstract ConfigDao configDao();

    public abstract PersonDao personDao();

    public abstract RecordDao recordDao();

    public abstract CalibrationDao calibrationDao();

}
