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
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.util.FileUtil;

import java.io.File;

@Database(entities = {Config.class, Person.class, Record.class}, version = 2)
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
                .build();
    }

    private static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
//          旧表添加新的字段
//          database.execSQL("ALTER TABLE User " + " ADD COLUMN book_id TEXT");
//          创建新的数据表
//          database.execSQL("CREATE TABLE IF NOT EXISTS `book` (`book_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
        }
    };

    public abstract ConfigDao configDao();

    public abstract PersonDao personDao();

    public abstract RecordDao recordDao();

}
