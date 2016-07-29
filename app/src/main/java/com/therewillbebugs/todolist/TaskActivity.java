package com.therewillbebugs.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.therewillbebugs.todolist.notification_utils.NotificationService;
import com.therewillbebugs.todolist.fragments.CompletedTasksFragment;
import com.therewillbebugs.todolist.fragments.HelpFragment;
import com.therewillbebugs.todolist.fragments.SettingsFragment;
import com.therewillbebugs.todolist.fragments.TaskListFragment;
import com.therewillbebugs.todolist.fragments.TaskViewFragment;
import com.therewillbebugs.todolist.task_components.Task;
import com.therewillbebugs.todolist.task_components.TaskManager;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity
        implements TaskListFragment.OnTaskListItemClicked,
        TaskViewFragment.OnTaskCreationCompleteListener,
        SettingsFragment.OnSettingsCompleteListener,
        TaskManager.OnDatabaseUpdate,
        HelpFragment.OnHelpCompleteListener{

    //Drawer Members
    private Class currentFragmentClass;
    private DrawerLayout drawerLayout;
    private NavigationView navDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem previousMenuItem;

    //Toolbar menu
    private Toolbar toolbar;

    //Basic list
    private TaskManager taskManager;
    private Task selectedTask;

    //NotificationService
    private NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        //Init Task List
        selectedTask = null;
        notificationService = new NotificationService(this);
        taskManager = new TaskManager(this,notificationService);

        //Init Database
        taskManager.initDatabase();

        //Init toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init Drawer
        initDrawer();

        initTaskListView();
        currentFragmentClass = TaskListFragment.class;
    }

    //Handles the option menu for the navigation drawer and the action bar items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //Action Bar/Toolbar selection handlers
        int id = item.getItemId();
        if (id == R.id.action_settings)
            initSettingsView();
        else if(id == R.id.action_sort_tasks)
            initSortDialog();
        else if(id == R.id.action_delete_task)
            deleteSelectedTask();
        return super.onOptionsItemSelected(item);
    }

    //Handles the backpressed state for both the drawer and the task list
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        //If class is taskListFragment, disable the back button to prevent jumping
        // to other activities
        if(!currentFragmentClass.equals(TaskListFragment.class)){
            swapBackToList();
        }
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

    //Sign out of firebase when app is closed
    @Override
    public void onDestroy(){
        FirebaseAuth.getInstance().signOut();
        super.onDestroy();
    }

    //region CALLBACK HANDLERS
    //---------------------------------------------
    // TaskListFragment
    @Override
    public void onTaskListItemClick(int position){
        //Swap fragments to the TaskView for the given task
        selectedTask = (taskManager.get(position));
        initTaskView(taskManager.get(position));
    }

    @Override
    public void onTaskListItemLongClick(int position){}

    @Override
    public void onTaskListItemChecked(int position, boolean checked){
        selectedTask = taskManager.get(position);
        taskManager.setTaskChecked(selectedTask, checked);
    }

    @Override
    public void onTaskListAddButtonClick(){
        initTaskView();
        currentFragmentClass = TaskViewFragment.class;
    }

    @Override
    public void onTaskListDragDropSwap(int positionA, int positionB){
        taskManager.swapPositions(positionA, positionB);
        syncTaskList();
    }

    //TaskViewFragment - When task creation is complete, handle signal
    @Override
    public void onTaskCreationComplete(boolean success, Task t, boolean newTaskCreated){
        //If the task creation was successful, add it to the list
        if(success && newTaskCreated) {
            taskManager.add(t);
            if (SettingsFragment.getNotificationsEnabled() && t.isNotificationsEnabled()) {
                notificationService.createNotification(t);
            }
        }
        else if(success)
            taskManager.update(t);
        swapBackToList();
    }

    //TaskManager
    @Override
    public void onDatabaseUpdate(){
        if(currentFragmentClass.equals(CompletedTasksFragment.class))
            syncCompletedTaskList();
        else syncTaskList();
    }

    //SettingsFragment
    @Override
    public void onSettingsComplete(boolean settingsSaved) {
        swapBackToList();
    }

    //Help Fragment
    @Override
    public void onHelpComplete(boolean result){
        swapBackToList();
    }
    //endregion

    //region DRAWER
    //Source: https://developer.android.com/training/implementing-navigation/nav-drawer.html
    private void initDrawer() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navDrawer = (NavigationView)findViewById(R.id.nav_drawer);
        navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem){
                menuItem.setChecked(true);
                if(previousMenuItem != null)
                    previousMenuItem.setChecked(false);
                previousMenuItem = menuItem;
                selectDrawerItem(menuItem);
                return true;
            }
        });
        navDrawer.getMenu().getItem(0).setChecked(true);
        previousMenuItem = navDrawer.getMenu().getItem(0);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);

        //Setup current user text
        TextView headerCurrentUser = (TextView)navDrawer.findViewById(R.id.nav_header_currentuser);
        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if(cUser != null)
            headerCurrentUser.setText("Welcome " + cUser.getEmail());
    }

    private void selectDrawerItem(MenuItem menuItem) {
        //Swaps the fragments in the main content frame based on selection
        int id = menuItem.getItemId();
        if(id == R.id.nav_drawer_tasklist){
            if(!currentFragmentClass.equals(TaskListFragment.class)) {
                initTaskListView();
                syncTaskList();
                currentFragmentClass = TaskListFragment.class;
            }
        }
        else if(id == R.id.nav_drawer_completed_tasks){
            if(!currentFragmentClass.equals(CompletedTasksFragment.class)) {
                initCompletedTaskListView(taskManager.getTaskList());
                currentFragmentClass = CompletedTasksFragment.class;
            }
        }
        else if(id == R.id.nav_drawer_settings){
            if(!currentFragmentClass.equals(SettingsFragment.class)){
                initSettingsView();
                currentFragmentClass = SettingsFragment.class;
            }
        }
        else if(id == R.id.nav_drawer_help){
            if(!currentFragmentClass.equals(HelpFragment.class)){
                initHelpView();
                currentFragmentClass = HelpFragment.class;
            }
        }
        else if(id == R.id.nav_drawer_signout){
            taskManager.cleanupDatabse();
            this.finish();
            FirebaseAuth.getInstance().signOut();
            Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginIntent);
        }
        else currentFragmentClass = TaskListFragment.class;

        menuItem.setCheckable(true);
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }
    //endregion

    //region FRAGMENT INITIALIZATION
    //Initializes an empty tasklist view
    private void initTaskListView() {
        if (findViewById(R.id.content_frame) != null) {
            //Create a new Fragment
            TaskListFragment fragment = new TaskListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, fragment, TaskListFragment.TAG);

            transaction.addToBackStack(TaskListFragment.TAG);
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    //Initializes the settings fragment
    private void initSettingsView() {
        if(findViewById(R.id.content_frame) != null){
            //Swap fragments using Replace so that we can return to previous views
            SettingsFragment fragment = new SettingsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, SettingsFragment.TAG);
            transaction.addToBackStack(SettingsFragment.TAG);
            transaction.commit();
        }
    }

    //Initializes the completed tasklist view fragmetn with a list of completed tasks
    private void initCompletedTaskListView(ArrayList<Task> taskList) {
        if (findViewById(R.id.content_frame) != null) {
            //Swap fragments using Replace so that we can return to previous views
            CompletedTasksFragment fragment = CompletedTasksFragment.newInstance(taskList);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, CompletedTasksFragment.TAG);
            transaction.addToBackStack(CompletedTasksFragment.TAG);
            transaction.commit();
        }
    }

    //Creates the help view Fragment and adds it to the content frame, adds to back stack so
    //navigation up the stack is possible to return to previous views
    private void initHelpView(){
        if(findViewById(R.id.content_frame) != null){
            HelpFragment fragment = new HelpFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.content_frame, fragment, HelpFragment.TAG);
            transaction.addToBackStack(HelpFragment.TAG);
            transaction.commit();
        }
    }

    //Initializes an empty taskview fragment
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

    //Initializes the main taskview fragment and popualtes it with a given task
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

    //Delete the current task opened in taskview by showing an alert dialog
    private void deleteSelectedTask(){
        if(selectedTask != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AppCompatAlertDialog);
            builder.setTitle("Confirm Task Delete");
            builder.setMessage("The following task will be deleted:\n\n" + selectedTask.getTitle());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    taskManager.remove(selectedTask.getTaskKey());
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

    //Swaps back the currently opened fragments to the tasklistfragment by navigating the
    //fragment stack.
    private void swapBackToList(){
        //Remove the TaskViewFragment, change the view back to the TaskListFragment
        for(int i = 0; i<getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().executePendingTransactions();
        }
        getSupportFragmentManager().executePendingTransactions();

        //Set the menu items to be checked/reset old checked state
        navDrawer.getMenu().getItem(0).setChecked(true);
        if(previousMenuItem != null)
            previousMenuItem.setChecked(false);
        previousMenuItem = navDrawer.getMenu().getItem(0);

        //Change the action bar title
        setTitle(getResources().getString(R.string.nav_item_1));
        currentFragmentClass = TaskListFragment.class;

        //Init tasklist view and sync the task
        initTaskListView();
        syncTaskList();
    }

    //Syncs the tasklist from taskmanager with the recycler view in tasklistview
    private void syncTaskList(){
        //refresh the taskview, notify data changed
        FragmentManager man = this.getSupportFragmentManager();
        TaskListFragment frag = (TaskListFragment) man.findFragmentByTag(TaskListFragment.TAG);
        //TODO Fix this error handling, its gross
        if (frag != null) {
            taskManager.sort();
            frag.refreshRecyclerList(taskManager.getTaskList());
        }
        else Toast.makeText(this, "Error couldn't refresh", Toast.LENGTH_SHORT).show();
    }

    //Syncs the completedtasklist fragment with the updated data from database
    private void syncCompletedTaskList(){
        //refresh the taskview, notify data changed
        FragmentManager man = this.getSupportFragmentManager();
        CompletedTasksFragment frag = (CompletedTasksFragment) man.findFragmentByTag(CompletedTasksFragment.TAG);
        //TODO Fix this error handling, its gross
        if (frag != null) {
            taskManager.sort();
            frag.refreshRecyclerList(taskManager.getTaskList());
        }
        else Toast.makeText(this, "Error couldn't refresh", Toast.LENGTH_SHORT).show();
    }

    //Creates the dialog for the sort options in the TaskListFragment (BY Date and BY Priority)
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