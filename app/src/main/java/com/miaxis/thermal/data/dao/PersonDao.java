package com.miaxis.thermal.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.miaxis.thermal.data.entity.Person;

import java.util.List;

@Dao
public interface PersonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Person person);

    @Query("select * from Person where Person.faceFeature is not null")
    List<Person> loadAll();

    @Query("select * from Person order by Person.updateTime desc limit :pageSize offset :pageSize * (:pageNum - 1)")
    List<Person> loadPersonByPage(int pageNum, int pageSize);

    @Query("select * from Person where Person.identifyNumber == :field or Person.phone == :field")
    Person findPerson(String field);

    @Query("select count(*) from Person")
    int loadPersonCount();

    @Query("select * from Person where Person.upload = 0 order by Person.updateTime asc limit 1")
    Person findOldestPerson();

    @Query("delete from Person")
    void deleteAll();

    @Delete
    void delete(Person person);

    @Query("select * from Person where Person.invalidTime <= :now")
    List<Person> findOverduePerson(long now);

}
