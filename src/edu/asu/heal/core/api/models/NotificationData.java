package edu.asu.heal.core.api.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import javax.annotation.Generated;

@SuppressWarnings("unused")
public class NotificationData implements IHealModelType {

    private String detail;
    private int id;
    private String title;
    private static ArrayList<Integer> values = new ArrayList<Integer>();
    private static int MAX_VAL = 10000000; // this number can be configurable .
    private static int index = 0;
    public NotificationData() {
        super();
        id = values.get(index);
        index++;
    }

    public NotificationData(String detail, String title) {
        super();
        id = values.get(index);
        index++;
        this.detail = detail;
        this.title = title;
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
}
