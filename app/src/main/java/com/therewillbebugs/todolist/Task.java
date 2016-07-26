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
    private String task_key;
    private String title;
    private String description;
    private Calendar time, date;
    private PRIORITY_LEVEL priorityLevel;
    private boolean complete, notifications;

    //Constructors
    //-------------------------------------
    public Task(){
        this.task_key = "";
        this.title = "";
        this.description = "";
        this.priorityLevel = PRIORITY_LEVEL.NONE;
        this.complete = false;
        this.notifications = true;
        this.time = null;
        this.date = Calendar.getInstance();
    }

    public Task(String task_key, String title, String description, int priority, String date, String time){
        this.task_key = task_key;
        this.title = title;
        this.description = description;
        this.priorityLevel = PRIORITY_LEVEL.get(priority);
        this.notifications = true;
        this.complete = false;

        SimpleDateFormat sdf;
        //If the date string is somehow empty, set it to the current day
        if(date.isEmpty())
            this.date = Calendar.getInstance();
        else {
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            //Try to format the date by parsing the dateStr from DB
            try {
                this.date = Calendar.getInstance();
                this.date.setTime(sdf.parse(date));
            } catch (Exception e) {
                this.date = Calendar.getInstance();
            }
        }
        sdf = new SimpleDateFormat("hh:mm a");
        try {
            //Try to format the time by parsing the timeStr from DB
            this.time = Calendar.getInstance();
            this.time.setTime(sdf.parse(time));
        }
        catch(Exception e){this.time = null;}   //Time can be set to null, empty time is valid
    }

    //Constructor for the DB_Task, DB_Task variables are case/name sensitive
    public Task(DB_Task db_task){
        this(db_task.task_key, db_task.title, db_task.description, db_task.priority, db_task.dateStr, db_task.timeStr);
    }

    //Mutators
    //-------------------------------------
    public void setTaskKey(String in){this.task_key = in;}
    public void setTitle(String in){this.title = in;}
    public void setDescription(String in){this.description = in;}
    public void setPriorityLevel(PRIORITY_LEVEL in){
        this.priorityLevel = in;
    }
    public void setComplete(boolean in){
        this.complete = in;
    }
    public void setNotificationsEnabled(boolean in){this.notifications = in;}
    public void setTime(Calendar in){this.time = in;}
    public void setDate(Calendar in){this.date = in;}

    //Accessors
    //-------------------------------------
    public String getTaskKey(){return task_key;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public PRIORITY_LEVEL getPriorityLevel(){
        return priorityLevel;
    }
    public boolean isComplete(){
        return complete;
    }
    public boolean isNotificationsEnabled(){return notifications;}
    public Calendar getTime(){return time;}
    public Calendar getDate(){return date;}

    //Returns the string formatted in the correct way with pm/am switch 'a'
    public String getTimeToString(){
        if(time != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            return sdf.format(time.getTime());
        }
        else return "";
    }

    //Returns the date formatted in the visual way with the current day displayed
    public String getDateToString(){
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            return sdf.format(date.getTime());
        }
        else return "";
    }

    //Returns the date formatted in the correct way for the database
    public String getDateToDBString(){
        if(date != null){
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            return sdf.format(date.getTime());
        }
        return "";
    }

    //Returns both the date and time formatted in a correct string
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
        return "Title: " + title + " Desc: " + description + " priority: " + priorityLevel.getVal() + " date: " + getDateToString() + " time: " + getTimeToString();
    }

    //Function to collect the class values to be sent to the DB using hashmap
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("task_key",task_key);
        result.put("title",title);
        result.put("description",description);
        result.put("priority",priorityLevel.getVal());
        result.put("dateStr",getDateToDBString());
        result.put("timeStr",getTimeToString());
        return result;
    }
}
