package com.therewillbebugs.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity
        implements TaskListFragment.OnTaskListItemClicked,
        TaskViewFragment.OnTaskCreationCompleteListener,
        TaskManager.OnDatabaseUpdate{

    //private members, this should be changed to R.array, temp
    //Drawer Members
    private String[] drawerContent = {"Test 1", "Settings", "Sign Out", "Exit"};
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

    //Toolbar menu
    private Toolbar toolbar;

    //Basic list
    private TaskManager taskManager;
    private Task selectedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        //Init Task List
        selectedTask = null;
        taskManager = new TaskManager(this);

        //Init Database
        taskManager.initDatabase();

        //Init toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init Drawer
        initDrawer();

        initTaskListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        //Action Bar/Toolbar selection handlers
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_sort_tasks){
            initSortDialog();
        }
        else if(id == R.id.action_delete_task){
            deleteSelectedTask();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    //region CALLBACK HANDLERS
    @Override
    public void onTaskListItemClick(int position){
        //Swap fragments to the TaskView for the given task
        selectedTask = (taskManager.get(position));
        initTaskView(taskManager.get(position));
    }

    @Override
    public void onTaskListItemLongClick(int position){
        //Toast.makeText(this,"Long click",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskListItemChecked(int position, boolean checked){
        selectedTask = taskManager.get(position);
        if(checked) {
            Toast.makeText(this, "Task Complete!", Toast.LENGTH_SHORT).show();
            selectedTask.setComplete(true);
            taskManager.remove(selectedTask);
            taskManager.add(selectedTask);
        }
        else{
            selectedTask.setComplete(false);
            taskManager.remove(selectedTask);
            taskManager.add(0,selectedTask);
        }
        syncTaskList();
    }

    @Override
    public void onTaskCreationComplete(boolean success, Task t, boolean newTaskCreated){
        //If the task creation was successful, add it to the list
        if(success && newTaskCreated) {
            taskManager.add(t);
        }
        swapBackToList();
    }

    @Override
    public void onTaskListAddButtonClick(){
        initTaskView();
    }

    @Override
    public void onTaskListDragDropSwap(int positionA, int positionB){
        taskManager.swapPositions(positionA, positionB);
        syncTaskList();
    }

    @Override
    public void onDatabaseUpdate(){
        syncTaskList();
    }

    //endregion

    //region DRAWER
    private void initDrawer() {
        //Reference: https://developer.android.com/training/implementing-navigation/nav-drawer.html

        //Initialize the Drawer Layout content
        //should call drawerContent = getResources().getStringArray(R.array.xxx); in the future
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
            }

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerSlide(View view, float slideOffset) {
                super.onDrawerSlide(view, slideOffset);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerContent));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    private void selectDrawerItem(int position) {
        //Swaps the fragments in the main content frame based on selection
        if(position == 0){

        }
        else if(position == 1){

        }
        else if(position == 2){
            //Sign out
            this.finish();
            FirebaseAuth.getInstance().signOut();
            Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginIntent);
        }
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }
    //endregion

    //region FRAGMENT INITIALIZATION
    private void initTaskListView() {
        if (findViewById(R.id.content_frame) != null) {
            //Create a new Fragment, using ADD because this will always be the first view ran
            TaskListFragment fragment = new TaskListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, fragment, TaskListFragment.TAG);
            transaction.addToBackStack(TaskListFragment.TAG);
            transaction.commit();
        }
    }

    private void initTaskListView(ArrayList<Task> taskList) {
        if (findViewById(R.id.content_frame) != null) {
            Log.d("taskActivity", "tasksize: " + taskList.size());
            //Create a new Fragment, using ADD because this will always be the first view ran
            TaskListFragment fragment = TaskListFragment.newInstance(taskList);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.add(R.id.content_frame, fragment, TaskListFragment.TAG);
            transaction.addToBackStack(TaskListFragment.TAG);
            transaction.commit();
        }
    }

    private void initTaskView(){
        if(findViewById(R.id.content_frame) != null){
            //Swap fragments using Replace so that we can return to previous views
            TaskViewFragment fragment = new TaskViewFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, TaskViewFragment.TAG);
            transaction.addToBackStack(TaskViewFragment.TAG);
            transaction.commit();
        }
    }

    private void initTaskView(Task task) {
        if(findViewById(R.id.content_frame) != null){
            //Swap fragments using Replace so that we can return to previous views
            TaskViewFragment fragment = TaskViewFragment.newInstance(task);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, TaskViewFragment.TAG);
            transaction.addToBackStack(TaskViewFragment.TAG);
            transaction.commit();
        }
    }
    //endregion

    private void deleteSelectedTask(){
        //Delete the current task opened in taskview
        if(selectedTask != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppCompatAlertDialog);
            builder.setTitle("Confirm Task Delete");
            builder.setMessage("The following task will be deleted:\n\n" + selectedTask.getTitle());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    taskManager.remove(selectedTask);
                    dialog.dismiss();
                    swapBackToList();
                    selectedTask = null;
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else Toast.makeText(this,"Error, Could not delete task!", Toast.LENGTH_SHORT).show();
    }

    private void swapBackToList(){
        //Remove the TaskViewFragment, change the view back to the TaskListFragment
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().executePendingTransactions();
        syncTaskList();
    }

    //Syncs the tasklist from taskmanager with the recycler view
    private void syncTaskList(){
        //refresh the taskview, notify data changed
        FragmentManager man = this.getSupportFragmentManager();
        TaskListFragment frag = (TaskListFragment) man.findFragmentByTag(TaskListFragment.TAG);
        //TODO Fix this error handling, its gross
        if (frag != null) {
            Log.d("taskActivity", "sync size: " + taskManager.size());
            frag.refreshRecyclerList(taskManager.getTaskList());
        }
        else Toast.makeText(this, "Error couldn't refresh", Toast.LENGTH_SHORT).show();
    }

    private void initSortDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialog);
        builder.setTitle("Select Sort Type");
        builder.setSingleChoiceItems(taskManager.getSortLevels(),taskManager.getSortLevel(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                if(item == 0){
                    taskManager.sortByTimeDate();
                    syncTaskList();
                }
                else if(item == 1){
                    taskManager.sortByPriority();
                    syncTaskList();
                }
                dialogInterface.dismiss();

            }
        });
        builder.create().show();
    }
}
