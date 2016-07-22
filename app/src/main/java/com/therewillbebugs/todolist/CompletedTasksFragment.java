package com.therewillbebugs.todolist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class CompletedTasksFragment extends android.support.v4.app.Fragment
    implements TaskListAdapter.OnCardViewAdapterClickListener {
    public static final String TAG = "CompletedTasksFragment";
    public static final String COMPLETED_TASKS_KEY = "CompletedTasks";

    private View rootView;
    private Activity activity;
    private ArrayList<Task> completedTasks;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerManager;
    private RecyclerView.Adapter recyclerAdapter;
    private ItemTouchHelper itemTouchHelper;

    public static CompletedTasksFragment newInstance(ArrayList<Task> tasks){
        CompletedTasksFragment fragment = new CompletedTasksFragment();
        if(tasks != null){
            Bundle args = new Bundle();

            ArrayList<Task> completedTasks = new ArrayList<>();
            for (Task t : tasks) {
                if (t.isComplete()) {
                    completedTasks.add(t);
                }
            }

            args.putSerializable(COMPLETED_TASKS_KEY, completedTasks);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCardViewAdapterClicked(View v, int t) {

    }
    @Override
    public void onCardViewAdapterChecked(View v, int i, boolean b) {

    }
    @Override
    public void onCardViewAdapterLongClicked(View v, int t) {

    }
    @Override
    public void onCardViewAdapterStartDrag(RecyclerView.ViewHolder vh) {

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.activity = activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnSettingsCompleteListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(COMPLETED_TASKS_KEY)) {
            ArrayList<Task> temp = (ArrayList<Task>) args.getSerializable(COMPLETED_TASKS_KEY);
            this.completedTasks = new ArrayList<>(temp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.completed_tasklist_layout, container, false);

        initRecycler();

        return rootView;
    }

    private void initRecycler(){
        recyclerView = (RecyclerView)rootView.findViewById(R.id.completed_taskview_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(recyclerManager);

        recyclerAdapter = new TaskListAdapter(completedTasks, this);
        recyclerView.setAdapter(recyclerAdapter);

        ItemTouchHelper.Callback simple = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
                //callbackListener.onTaskListDragDropSwap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                //recyclerAdapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
                //Change 0 in constructor to ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT for swipe support
            }
        };

        itemTouchHelper = new ItemTouchHelper(simple);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
