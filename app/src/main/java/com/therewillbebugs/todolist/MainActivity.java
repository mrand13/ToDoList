package com.therewillbebugs.todolist;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements TaskViewFragment.OnTaskCreationCompleteListener {

    //private members, this should be changed to R.array, temp
    //Drawer Members
    private String[] drawerContent = {"Test 1", "Test 2", "Exit"};
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

    //Toolbar menu
    private Toolbar toolbar;

    //Basic list
    private ArrayList<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Task List
        taskList = new ArrayList<Task>();

        //Init toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init Drawer
        initDrawer();

        //Init main fragment, will default to creating TaskView
        initTaskView();
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
        else if(id == R.id.action_add_task){
            initTaskCreateView();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START | Gravity.LEFT)) {
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

    //region CALLBACK HANDLERS
    @Override
    public void onTaskCreationComplete(boolean result, Task t){
        //If the task creation was successful, add it to the list
        if(result) {
            taskList.add(t);
        }
        //Remove the TaskViewFragment, change the view back to the TaskListFragment
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().executePendingTransactions();

        //refresh the taskview, notify data changed
        //TODO List doesnt reset properly
        if(result) {
            FragmentManager man = this.getSupportFragmentManager();
            TaskListFragment frag = (TaskListFragment) man.findFragmentByTag(TaskListFragment.TAG);
            //TODO Fix this error handling, its gross
            if (frag != null)
                frag.refreshRecyclerList(taskList);
            else Toast.makeText(this, "Error couldn't refresh", Toast.LENGTH_SHORT).show();
        }
    }

    //endregion

    //region TASKLIST
    public void addTask(Task t){
        taskList.add(t);
    }

    public final Task getTask(int position){
        return taskList.get(position);
    }

    public final ArrayList<Task> getTaskList(){
        return taskList;
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
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        //Swaps the fragments in the main content frame based on selection

        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }
    //endregion

    //region FRAGMENT INITIALIZATION
    private void initTaskView() {
        if (findViewById(R.id.content_frame) != null) {
            //Create a new Fragment, using ADD because this will always be the first view ran
            TaskListFragment fragment = new TaskListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, fragment, TaskListFragment.TAG);
            transaction.addToBackStack(TaskListFragment.TAG);
            transaction.commit();
        }
    }

    private void initTaskCreateView(){
        if(findViewById(R.id.content_frame) != null){
            //Swap fragments using Replace so that we can return to previous views
            TaskViewFragment fragment = new TaskViewFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, TaskViewFragment.TAG);
            transaction.addToBackStack(TaskViewFragment.TAG);
            transaction.commit();
        }
    }
    //endregion
}
