package com.project3.step_counter;

import java.util.Date;

/**
 * This class is created as entity to store values
 * with sets of GETTER && SETTER methods and other APIs
 * to interact with the backend
 * Created by lxybi_000 on 2015/3/15.
 */
public class Counter {
    private int steps;
    private int segment_id;
    private Date startDate;

    // public constructors used to stores values and init
    public Counter() {

    }
    public Counter(int steps, int id, Date date) {
        this.steps = steps;
        segment_id = id;
        startDate = date;
    }

    // getters && setters
    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public void setSegment(int id) {
        segment_id = id;
    }

    public int getSegment() {
        return segment_id;
    }

    public void setStartDate(Date date) {
        startDate = date;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getDuration(int endMilli) {
        return (int) (endMilli - startDate.getTime()) / 1000;
    }
}
