package edu.asu.heal.core.api.models;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Domain {

    public static String ID_ATTRIBUTE = "_id";
    public static String TITLE_ATTRIBUTE = "title";
    public static String DESCRIPTION_ATTRIBUTE = "description";
    public static String STATE_ATTRIBUTE = "state";
    public static String ACTIVITIES_ATTRIBUTE = "activities";
    public static String TRIALS_ATTRIBUTE = "trials";

    private ObjectId id;
    private String title;
    private String description;
    private String state;
    private ArrayList<ObjectId> activities;
    private ArrayList<ObjectId> trials;

    public Domain(){
        // default constructor
    }

    public Domain(String title, String description, String state){
        this.title = title;
        this.description = description;
        this.state = state;
    }

    // getters and setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ArrayList<ObjectId> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<ObjectId> activities) {
        this.activities = activities;
    }

    public ArrayList<ObjectId> getTrials() {
        return trials;
    }

    public void setTrials(ArrayList<ObjectId> trials) {
        this.trials = trials;
    }
}