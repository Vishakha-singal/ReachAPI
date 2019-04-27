package edu.asu.heal.core.api.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Generated;
import edu.asu.heal.reachv3.api.notification.ActivityList;

@SuppressWarnings("unused")
public class NotificationData implements IHealModelType {

    private String detail;
    private int id;
    private String title;
    private String url;
    private Integer levelOfNotification;
    private List<ActivityList> activities;
    private boolean sudsConfig;
  

	private static ArrayList<Integer> values = new ArrayList<Integer>();
    private static int MAX_VAL = 10000000; // this number can be configurable .
    private static int index = 0;
    
    public NotificationData() {
        super();
        id = values.get(index);
        index++;
        activities = new ArrayList<>();
    }
    

    public NotificationData(String detail, String title, String url, 
    		Integer levelOfNotification, List<ActivityList> list, boolean sudsConfig) {
        super();
        id = values.get(index);
        index++;
        this.detail = detail;
        this.title = title;
        this.url=url;
        this.levelOfNotification=levelOfNotification;
        this.activities=list;
        this.sudsConfig=sudsConfig;
    }


    public boolean isSudsConfig() {
		return sudsConfig;
	}


	public void setSudsConfig(boolean sudsConfig) {
		this.sudsConfig = sudsConfig;
	}


	public List<ActivityList> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityList> activities) {
        this.activities = activities;
    }

    public Integer getLevelOfNotification() {
		return levelOfNotification;
	}


	public void setLevelOfNotification(Integer levelOfNotification) {
		this.levelOfNotification = levelOfNotification;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    static {
    	
    	for(int i=0; i<MAX_VAL; i++) {
    		values.add(i);
    	}
    	Collections.shuffle(values);
    	
    }

    @Override
    public String toString() {
        return "NotificationData{" +
                ", url=" + url +
                ", details=" + detail +
                ", levelOfNotification=" + levelOfNotification +
                ", title=" + title +
                ", activities" + activities.toString() +
                ", sudsConfig" + sudsConfig +
                '}';
    }
}
