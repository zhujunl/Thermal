package com.miaxis.thermal.data.model;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.entity.Person;

import java.util.List;

public class PersonModel {

    public static List<Person> loadAll() {
        return AppDatabase.getInstance().personDao().loadAll();
    }

    public static List<Person> loadPersonByPage(int pageNum, int pageSize) {
        return AppDatabase.getInstance().personDao().loadPersonByPage(pageNum, pageSize);
    }

    public static Person findPerson(String field) {
        return AppDatabase.getInstance().personDao().findPerson(field);
    }

    public static int loadPersonCount() {
        return AppDatabase.getInstance().personDao().loadPersonCount();
    }

    public static Person findOldestPerson() {
        return AppDatabase.getInstance().personDao().findOldestPerson();
    }

    public static void savePerson(Person person) {
        AppDatabase.getInstance().personDao().insert(person);
    }

    public static void deletePerson(Person person) {
        AppDatabase.getInstance().personDao().delete(person);
    }

    public static void deleteAll() {
        AppDatabase.getInstance().personDao().deleteAll();
    }

}
