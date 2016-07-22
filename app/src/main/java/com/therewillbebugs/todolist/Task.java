package com.therewillbebugs.todolist;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {
    //Priority Level enum with support for converting ints
    public enum PRIORITY_LEVEL{
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
    private String title;
    private String description;
    private Calendar time, date;
    private PRIORITY_LEVEL priorityLevel;
    private boolean complete, notifications;

    //public functions
    //-------------------------------------
    public Task(){
        this.title = "";
        this.description = "";
        this.priorityLevel = PRIORITY_LEVEL.NONE;
        this.complete = false;
        this.notifications = true;
        this.time = null;
        this.date = null;
    }

    public Task(String title, String description, PRIORITY_LEVEL priorityLevel){
        this.title = title;
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.complete = false;
        this.notifications = true;
        this.time = null;
        this.date = Calendar.getInstance();
    }

    public Task(String uid, String title, String description, int priority, String date, String time){
        this.title = title;
        this.description = description;
        this.priorityLevel = PRIORITY_LEVEL.get(priority);
        //TODO FIX DATE/TIME
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        try {
            this.date = Calendar.getInstance();
            this.date.setTime(sdf.parse(date));
        }
        catch(Exception e){this.date = null;}
        sdf = new SimpleDateFormat("h:mm a");
        try {
            this.time = Calendar.getInstance();
            this.time.setTime(sdf.parse(time));
        }
        catch(Exception e){this.time = null;}
        this.notifications = true;
        this.complete = false;
    }

    public Task(DB_Task db_task){
        this(db_task.uid, db_task.title, db_task.description, db_task.priority, db_task.dateStr, db_task.timeStr);
    }

    //Mutators
    //-------------------------------------
    public void setTitle(String in){this.title = in;}
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
    public String getTitle(){return title;}
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
        if(!tempTime.isEmpty())
        return "Complete By: " + tempDate + " at " + tempTime;
        else return "Complete By: " + tempDate;
    }

    @Override
    public String toString(){
        return "Title: " + title + " Desc: " + description + " priority: " + priorityLevel.getVal();
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid","temp");
        result.put("title",title);
        result.put("description",description);
        result.put("priority",priorityLevel.getVal());
        result.put("dateStr",getDateToString());
        result.put("timeStr",getTimeToString());
        return result;
    }

    //private functions
    //-------------------------------------
}
