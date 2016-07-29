package com.therewillbebugs.todolist.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.therewillbebugs.todolist.R;
import com.therewillbebugs.todolist.task_components.CompletedTaskListAdapter;
import com.therewillbebugs.todolist.task_components.Task;
import com.therewillbebugs.todolist.task_components.TaskListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CompletedTasksFragment extends android.support.v4.app.Fragment
    implements CompletedTaskListAdapter.OnCardViewAdapterClickListener{

    public static final String TAG = "CompletedTasksFragment";
    public static final String COMPLETED_TASKS_KEY = "CompletedTasks";
    public static final String TASK_RATIO = "TaskRatio";

    private View rootView;
    private Context context;
    private ArrayList<Task> completedTasks;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerManager;
    private RecyclerView.Adapter recyclerAdapter;

    @Override
    public void onCardViewAdapterClicked(View v, int position){
    }

    public static CompletedTasksFragment newInstance(ArrayList<Task> tasks){
        CompletedTasksFragment fragment = new CompletedTasksFragment();
        if(tasks != null){
            Bundle args = new Bundle();
            args.putSerializable(COMPLETED_TASKS_KEY, tasks);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            this.context = context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement OnSettingsCompleteListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.completedTasks = new ArrayList<>();
        Bundle args = getArguments();
        if (args != null && args.containsKey(COMPLETED_TASKS_KEY)) {
            ArrayList<Task> temp = (ArrayList<Task>) args.getSerializable(COMPLETED_TASKS_KEY);
            this.completedTasks.addAll(temp);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.completed_tasklist_layout, container, false);

        initRecycler();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initRecycler(){
        recyclerView = (RecyclerView)rootView.findViewById(R.id.completed_taskview_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(recyclerManager);

        recyclerAdapter = new CompletedTaskListAdapter(completedTasks, this);
        recyclerView.setAdapter(recyclerAdapter);
    }

    public void refreshRecyclerList(ArrayList<Task> tl){
        this.completedTasks.clear();
        this.completedTasks.addAll(tl);
        ((CompletedTaskListAdapter)recyclerAdapter).swap(this.completedTasks);
        recyclerAdapter.notifyDataSetChanged();
    }
}
