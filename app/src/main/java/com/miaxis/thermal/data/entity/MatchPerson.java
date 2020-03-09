package com.miaxis.thermal.data.entity;

public class MatchPerson {

    private Person person;
    private float score;

    public MatchPerson(Person person, float score) {
        this.person = person;
        this.score = score;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
