package com.therewillbebugs.todolist;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TaskManager {
    //Database listener
    public interface OnDatabaseUpdate{
        public void onDatabaseUpdate();
    }

    //class members
    private ArrayList<Task> taskList;
    private int sortLevel;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private OnDatabaseUpdate dbListener;

    private final String dbRefTag = "/user-tasks/";
    public TaskManager(Context context){
        taskList = new ArrayList<Task>();
        sortLevel = 0;
        dbListener = (OnDatabaseUpdate)context;
    }

    public void tempInit(){
        taskList.add(new Task("First task","This is a task note", Task.PRIORITY_LEVEL.HIGH));
        taskList.add(new Task("Second task","", Task.PRIORITY_LEVEL.MEDIUM));
        taskList.add(new Task("Third task","Task note task note", Task.PRIORITY_LEVEL.LOW));
    }

    public void initDatabase(){
        fbAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //TODO: Throw exception if null pointer or if Auth fails
        dbRef = database.getReference(dbRefTag + fbAuth.getCurrentUser().getUid() + "/");
        initChildListeners();
    }

    private void initChildListeners(){
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //DB_Task db_task = dataSnapshot.getValue(DB_Task.class);
                //taskList.add(new Task(db_task));
                //dbListener.onDatabaseUpdate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DB_Task db_task = dataSnapshot.getValue(DB_Task.class);
                taskList.add(new Task(db_task));
                dbListener.onDatabaseUpdate();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dbRef.addChildEventListener(childEventListener);
    }

    //Sort Functions
    public CharSequence[] getSortLevels(){
        return new CharSequence[]{"Time & Date", "Priority"};
    }

    public void sortByTimeDate(){
        this.sortLevel = 0;
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                if(t1.isComplete() ^ t2.isComplete()){
                    if(t1.isComplete())
                        return 1;
                    else return -1;
                }
                else {
                    int dates = t1.getDate().compareTo(t2.getDate());

                    //If no time is set, return dates
                    if (t1.getTime() == null || t2.getTime() == null) {
                        if (dates == 0) //return by priority level if dates are equal
                            return t1.getPriorityLevel().compareTo(t2.getPriorityLevel());
                        else return dates;
                    }

                    int times = t1.getTime().compareTo(t2.getTime());
                    if (dates == 0) {  //If dates are equal
                        if (times == 0)
                            return t1.getPriorityLevel().compareTo(t2.getPriorityLevel());
                        else return times;
                    } else return dates;
                }
            }
        });
    }


    public void sortByPriority(){
        this.sortLevel = 1;
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                if(t1.isComplete() ^ t2.isComplete()){
                    if(t1.isComplete())
                        return 1;
                    else return -1;
                }
                else {
                    int level = t1.getPriorityLevel().compareTo(t2.getPriorityLevel());
                    if (level == 0) {
                        //Sort by time/date
                        int dates = t1.getDate().compareTo(t2.getDate());
                        if (t1.getTime() == null || t2.getTime() == null) {
                            return dates;
                        }

                        int times = t1.getTime().compareTo(t2.getTime());
                        if (dates == 0) {  //If dates are equal
                            return times;
                        } else return dates;
                    } else return level;
                }
            }
        });
    }

    public void swapPositions(int positionA, int positionB){
        Collections.swap(taskList, positionA, positionB);
    }

    //Mutators
    public boolean add(Task t){
        String key = dbRef.child("tasks").push().getKey();
        Map<String, Object> taskValues = t.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key,taskValues);
        dbRef.updateChildren(childUpdates);
        return taskList.add(t);
    }

    public void add(int position, Task t){

        taskList.add(position,t);
    }

    public boolean remove(Task t){
        if(taskList.contains(t)){
            return taskList.remove(t);
        }
        else return false;
    }

    public void clear(){
        taskList.clear();
    }

    public int size(){
        return taskList.size();
    }

    //Accessors
    public ArrayList<Task> getTaskList(){
        return taskList;
    }
    public Task get(int position){
        return taskList.get(position);
    }
    public int getSortLevel(){return sortLevel; }
}

class DB_Task{
    public String uid, title, description, dateStr, timeStr;
    public int priority;
    private DB_Task(){}

    public DB_Task(String uid, String title, String description, int priority, String dateStr, String timeStr){
        this.title = title;
        this.description =  description;
        this.priority = priority;
        this.dateStr = dateStr;
        this.timeStr = timeStr;
    }
}