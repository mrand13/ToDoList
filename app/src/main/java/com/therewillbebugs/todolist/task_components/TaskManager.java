package com.therewillbebugs.todolist.task_components;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.therewillbebugs.todolist.notification_utils.NotificationService;

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
    private ArrayList<String> taskListIDs;
    private ArrayList<Task> taskList;
    private int sortLevel;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private OnDatabaseUpdate dbListener;
    private ChildEventListener childEventListener;
    private Context context;
    private NotificationService notificationService;

    private final String dbRefTag = "/user-tasks/";

    //Constructor
    public TaskManager(Context context, NotificationService notificationService){
        this.context = context;
        taskListIDs = new ArrayList<>();
        taskList = new ArrayList<>();
        sortLevel = 0;
        dbListener = (OnDatabaseUpdate)this.context;
        this.notificationService = notificationService;
    }

    //Initialize the database and the auth for current user
    public void initDatabase(){
        fbAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //TODO: Throw exception if null pointer or if Auth fails
        dbRef = database.getReference(dbRefTag + fbAuth.getCurrentUser().getUid() + "/");
        initChildListeners();
    }

    //Create database child event listeners
    private void initChildListeners(){
        this.childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                //Get value from database and create task with that value
                DB_Task db_task = dataSnapshot.getValue(DB_Task.class);
                Task task = new Task(db_task);

                //Add to the main lists
                taskListIDs.add(dataSnapshot.getKey());
                taskList.add(task);
                dbListener.onDatabaseUpdate();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChild) {
                //Get value from database and create task with that value
                DB_Task db_task = dataSnapshot.getValue(DB_Task.class);
                Task task = new Task(db_task);
                String task_key = dataSnapshot.getKey();

                //Find the key and then update the list based on the value
                int index = taskListIDs.indexOf(task_key);
                if(index >= 0){
                    taskList.set(index, task);
                    dbListener.onDatabaseUpdate();
                }
                else Log.w("TaskListAdapterFragment","taskList:onChildChanged:unknown_child: " + task_key);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Find the given key in the list of IDs and then remove it
                String task_key = dataSnapshot.getKey();
                int index = taskListIDs.indexOf(task_key);
                if(index >= 0){
                    notificationService.deleteNotification(taskList.get(index));
                    taskListIDs.remove(index);
                    taskList.remove(index);
                    dbListener.onDatabaseUpdate();
                }
                else Log.w("TaskListAdapterFragment","taskList:onChildRemoved:unknown_child: " + task_key);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChild) {
                //Get value from database and create task with that value
                DB_Task db_task = dataSnapshot.getValue(DB_Task.class);
                Task task = new Task(db_task);
                String task_key = dataSnapshot.getKey();

                int index = taskListIDs.indexOf(task_key);
                if(index >=0){
                    //TODO implement or remove this function
                    //dbListener.onDatabaseUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TaskListAdapterFragment","taskList:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to load task list!", Toast.LENGTH_SHORT).show();
            }
        };
        dbRef.addChildEventListener(childEventListener);
    }

    //Removes the listener from the database upon view switching to prevent errors (Switching contexts)
    //Is called when TaskManager should be deleted
    public void cleanupDatabse(){
        if(childEventListener != null)
            dbRef.removeEventListener(childEventListener);
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
                if (t1.isComplete() ^ t2.isComplete()) {
                    if (t1.isComplete())
                        return 1;
                    else return -1;
                } else {
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

    public void sort(){
        if(sortLevel == 0)
            sortByTimeDate();
        else if(sortLevel == 1)
            sortByPriority();
    }

    public void swapPositions(int positionA, int positionB){
        Collections.swap(taskList, positionA, positionB);
    }

    public void checkedChangePosition(Task selectedTask, boolean checked){
        selectedTask.setComplete(checked);
        update(selectedTask);
    }

    //Mutators
    //------------------------------------
    //Adds the task to the given database reference by pushing a new key() and appending content
    //to that key.
    public boolean add(Task t){
        String key = dbRef.child("tasks").push().getKey();
        t.setTaskKey(key);
        Map<String, Object> taskValues = t.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key,taskValues);
        dbRef.updateChildren(childUpdates);
        return true;
    }

    //Updates the Database with the given task, updatesChildren() based on given key in the database
    public boolean update(Task t){
        String key = t.getTaskKey();
        Map<String, Object> taskValues = t.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key,taskValues);
        dbRef.updateChildren(childUpdates);
        return true;
    }

    public void add(int position, Task t){
        taskList.add(position,t);
    }

    public boolean remove(Task t){
        if(taskList.contains(t)){
            notificationService.deleteNotification(t);
            return taskList.remove(t);
        }
        else return false;
    }

    //Removes the given 'key' from the database by calling a transaction on the data, setting the
    //mutabledata to null removes it from the database
    public boolean remove(final String task_key){
        DatabaseReference taskRef = database.getReference(dbRefTag + fbAuth.getCurrentUser().getUid() + "/" + task_key);
        taskRef.runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction(MutableData mutableData){
                DB_Task db_task = mutableData.getValue(DB_Task.class);
                if(db_task == null)
                    return Transaction.success(mutableData);

                mutableData.setValue(null);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot){}
        });
        return true;
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

//Helper class for the Database, The variable names and constructor variable names are CASE SENSITIVE
//They must be named exactly what the variables are in the database, Portal pushes to these exact variables
class DB_Task{
    //Class members public because db_task is only used in creation of main Task then garbage collected
    public String task_key, title, description, dateStr, timeStr;
    public int priority;
    public boolean isComplete;
    private DB_Task(){} //Default constructor required

    public DB_Task(String task_key, String title, String description, int priority, String dateStr, String timeStr, boolean isComplete){
        this.task_key = task_key;
        this.title = title;
        this.description =  description;
        this.priority = priority;
        this.dateStr = dateStr;
        this.timeStr = timeStr;
        this.isComplete = isComplete;
    }
}