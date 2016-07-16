package com.therewillbebugs.todolist;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Task implements Serializable {
    //Priority Level enum with support for converting ints
    public enum PRIORITY_LEVEL{
        //TODO FIX THE ORDER OF THIS OR THE ORDER ON THE LAYOUT
        NONE(0), HIGH(1), MEDIUM(2), LOW(3);

        private int val;

        private PRIORITY_LEVEL(int val){
            this.val = val;
        }

        public int getVal(){return val;}

        public static PRIORITY_LEVEL get(int v){
            for(PRIORITY_LEVEL level : PRIORITY_LEVEL.values()){
                if(level.getVal() == v)
                    return level;
            }
            return null;
        }
    }

    //class members
    //-------------------------------------
    //TODO Change description into title, add description (notes)
    private String description;
    private Calendar time, date;
    private PRIORITY_LEVEL priorityLevel;
    private boolean complete, notifications;

    //public functions
    //-------------------------------------
    public Task(){
        this.description = "";
        this.priorityLevel = PRIORITY_LEVEL.NONE;
        this.complete = false;
        this.notifications = true;
        this.time = null;
        this.date = null;
    }

    public Task(String description, PRIORITY_LEVEL priorityLevel){
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.complete = false;
        this.notifications = true;
        this.time = null;
        this.date = null;
    }

    //Mutators
    //-------------------------------------
    public void setDescription(String in){
        this.description = in;
    }
    public void setPriorityLevel(PRIORITY_LEVEL in){
        this.priorityLevel = in;
    }
    public void setComplete(boolean in){
        this.complete = in;
    }
    public void setNotificationsEnabled(boolean in){
        this.notifications = in;
    }
    public void setTime(Calendar in){this.time = in;}
    public void setDate(Calendar in){this.date = in;}

    //Accessors
    //-------------------------------------
    public String getDescription(){
        return description;
    }

    public PRIORITY_LEVEL getPriorityLevel(){
        return priorityLevel;
    }

    public boolean isComplete(){
        return complete;
    }

    public boolean isNotificationsEnabled(){
        return notifications;
    }

    public Calendar getTime(){return time;}
    public Calendar getDate(){return date;}
    public String getTimeToString(){
        if(time != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            return sdf.format(time.getTime());
        }
        else return "";
    }

    public String getDateToString(){
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            return sdf.format(date.getTime());
        }
        else return "";
    }

    public String getDateTimeString(){
        if(time == null && date == null)
            return "";
        String tempTime = "", tempDate = "";
        tempTime = getTimeToString();
        tempDate = getDateToString();
        return "Complete By: " + tempDate + " at " + tempTime;
    }

    //private functions
    //-------------------------------------
}
