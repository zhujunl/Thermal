package com.miaxis.thermal.data.entity;

public class MatchPerson {

    private Person person;
    private float score;
    private boolean mask;

    public MatchPerson(Person person, float score, boolean mask) {
        this.person = person;
        this.score = score;
        this.mask = mask;
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

    public boolean isMask() {
        return mask;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }
}
