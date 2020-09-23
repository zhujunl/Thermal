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

@Database(entities = {Config.class, Person.class, Record.class, Calibration.class}, version = 13)
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
                .addMigrations(MIGRATION_7_8)
                .addMigrations(MIGRATION_8_9)
                .addMigrations(MIGRATION_9_10)
                .addMigrations(MIGRATION_10_11)
                .addMigrations(MIGRATION_11_12)
                .addMigrations(MIGRATION_12_13)
//                .fallbackToDestructiveMigration()
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

    private static Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Record ADD COLUMN attendance TEXT default '1'");
            database.execSQL("ALTER TABLE Config ADD COLUMN gateLimit INTEGER not null default 0");
        }
    };

    private static Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN tempScore REAL not null default 34.0");
        }
    };

    private static Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN accessSign INTEGER not null default 1");
            database.execSQL("ALTER TABLE Record ADD COLUMN access TEXT default '0'");
        }
    };

    private static Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP INDEX index_Person_identifyNumber_phone");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Person_identifyNumber` ON Person (`identifyNumber`)");
        }
    };

    private static Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN idCardEntry INTEGER not null default 0");
            database.execSQL("ALTER TABLE Config ADD COLUMN idCardVerify INTEGER not null default 1");
        }
    };

    private static Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Config ADD COLUMN timingSwitch INTEGER not null default 0");
            database.execSQL("ALTER TABLE Config ADD COLUMN switchStartTime TEXT default '08:00'");
            database.execSQL("ALTER TABLE Config ADD COLUMN switchEndTime TEXT default '18:00'");
        }
    };

    public abstract ConfigDao configDao();

    public abstract PersonDao personDao();

    public abstract RecordDao recordDao();

    public abstract CalibrationDao calibrationDao();

}
