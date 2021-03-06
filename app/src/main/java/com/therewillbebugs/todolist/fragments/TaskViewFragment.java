package com.therewillbebugs.todolist.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.therewillbebugs.todolist.R;
import com.therewillbebugs.todolist.task_components.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskViewFragment extends android.support.v4.app.Fragment {
    //Callback setup for Activity communication
    public interface OnTaskCreationCompleteListener{
        public void onTaskCreationComplete(boolean success, Task t, boolean newTaskCreated);
    }

    //Class members
    public static String TAG = "TaskViewFragment";
    public static final String SERIAL_LIST_KEY = "SerialTask";

    private View rootView;
    private static Task task;  //The 'new' task to be added to the global task list
    private OnTaskCreationCompleteListener callbackListener;
    private Button createButton, cancelButton, pickTimeButton, pickDateButton;
    private EditText editTextTitle, editTextDescription;
    private RadioGroup priorityRadioGroup;
    private TextView notificationText;
    private ImageView notifcationEnabledIV;

    private static TextView timeDateTV;
    private static String timeString, dateString;
    private boolean initNewTask, notificationSelector;

    public static TaskViewFragment newInstance(Task t){
        TaskViewFragment fragment = new TaskViewFragment();
        if(t != null){
            Bundle args = new Bundle();
            args.putSerializable(SERIAL_LIST_KEY,t);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public TaskViewFragment(){}

    //Override functions
    //-------------------------------------
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity a = (Activity)context;
        try{
            callbackListener = (OnTaskCreationCompleteListener)a;
        } catch (ClassCastException e){
            throw new ClassCastException(a.toString() + " must implement OnTaskCreationCompleteListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null && args.containsKey(SERIAL_LIST_KEY)) {
            this.task = (Task) args.getSerializable(SERIAL_LIST_KEY);
            initNewTask = false;
        }
        else{ task = new Task();initNewTask = true;}

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.taskview_layout,container,false);

        editTextTitle = (EditText)rootView.findViewById(R.id.taskview_create_title);
        editTextDescription = (EditText)rootView.findViewById(R.id.taskview_create_desc);
        priorityRadioGroup = (RadioGroup)rootView.findViewById(R.id.taskview_create_radiogrp);
        createButton = (Button)rootView.findViewById(R.id.taskview_create_btn_createtask);
        cancelButton = (Button)rootView.findViewById(R.id.taskview_create_btn_canceltask);
        pickTimeButton = (Button)rootView.findViewById(R.id.taskview_create_btn_picktime);
        pickDateButton = (Button)rootView.findViewById(R.id.taskview_create_btn_pickdate);
        notificationText = (TextView)rootView.findViewById(R.id.taskview_create_notifcations);
        notifcationEnabledIV = (ImageView)rootView.findViewById(R.id.taskview_create_notifcations_iv);

        timeDateTV = (TextView)rootView.findViewById(R.id.taskview_create_tv_datetime);
        timeString = "";
        dateString = "";

        // set default priority level from settings if user has enabled one
        Task.PRIORITY_LEVEL defaultPriority = SettingsFragment.getDefaultPriorityLevel();

        if (defaultPriority != null) {
            RadioButton btn = (RadioButton)priorityRadioGroup.getChildAt(defaultPriority.getVal());
            btn.setChecked(true);
        }

        // set text displaying whether or not notifications are enabled
        notificationSelector = !SettingsFragment.getNotificationsEnabled();
        toggleNotifications();

        notificationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNotifications();
            }
        });
        notifcationEnabledIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNotifications();
            }
        });
        ///////////////////////////////////////////////////////////////////

        if(!initNewTask)
            populateView();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask(v);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCreateTask(v);
            }
        });
        pickTimeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showTimePickerDialog(view);
            }
        });
        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if(initNewTask)
            inflater.inflate(R.menu.menu_main, menu);   //Show empty menu if new task
        else inflater.inflate(R.menu.taskview_menu, menu);  //Show delete menu if old task
        super.onCreateOptionsMenu(menu, inflater);
    }

    //functions
    //-------------------------------------

    private void toggleNotifications(){
        notificationSelector = !notificationSelector;
        if(notificationSelector)
            notifcationEnabledIV.setImageResource(R.drawable.ic_notifications_active_black);
        else notifcationEnabledIV.setImageResource(R.drawable.ic_notifications_off_black);
        String msg = notificationSelector ? getString(R.string.notifications_on)
                : getString(R.string.notifications_off);
        notificationText.setText(msg);
    }

    private void populateView(){
        editTextTitle.setText(task.getTitle());
        editTextDescription.setText(task.getDescription());
        ((RadioButton)priorityRadioGroup.getChildAt(task.getPriorityLevel().getVal())).setChecked(true);

        timeDateTV.setText(task.getDateTimeString());
        ((TextView)rootView.findViewById(R.id.create_task_header)).setText("Edit Task");
        notificationSelector = !task.isNotificationsEnabled();
        toggleNotifications();
    }

    public void createNewTask(View view){
        //Send task back to TaskActivity so it can be added to the list
        //TODO: add checks for complete form, require description
        task.setTitle(editTextTitle.getText().toString());
        task.setDescription(editTextDescription.getText().toString());

        //Get the index of the radiobutton (if there are other children, this will fail)
        int radioButtonID = priorityRadioGroup.getCheckedRadioButtonId();
        View rdoButton = priorityRadioGroup.findViewById(radioButtonID);
        int index = priorityRadioGroup.indexOfChild(rdoButton);
        task.setPriorityLevel(Task.PRIORITY_LEVEL.get(index));
        task.setComplete(false);
        task.setNotificationsEnabled(notificationSelector);

        if(task != null) {
            if(initNewTask)
                callbackListener.onTaskCreationComplete(true, task, true);
            else callbackListener.onTaskCreationComplete(true,task,false);  //Update task, don't add to list
        }
    }

    public void cancelCreateTask(View view){
        //return back to previous fragment
        callbackListener.onTaskCreationComplete(false,null,false);
    }

    //region TIME AND DATE
    public void showTimePickerDialog(View view){
        DialogFragment timePickFragment = new TimePickerFragment();
        timePickFragment.show(getActivity().getSupportFragmentManager(),"timePicker");
    }

    public void showDatePickerDialog(View view){
        DialogFragment datePickFragment = new DatePickerFragment();
        datePickFragment.show(getActivity().getSupportFragmentManager(),"datePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(),TimePickerDialog.THEME_HOLO_LIGHT,this,hour,minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute){
            //set the time in the new task
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            task.setTime(c);
            timeDateTV.setText(task.getDateTimeString());
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(),this,year,month,day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            //Set the date in the new task
            Calendar c = Calendar.getInstance();
            c.set(year,month,day);
            task.setDate(c);
            timeDateTV.setText(task.getDateTimeString());
        }
    }
    //endregion

}
