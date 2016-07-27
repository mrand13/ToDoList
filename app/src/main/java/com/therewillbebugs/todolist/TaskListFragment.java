package com.therewillbebugs.todolist;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import java.util.ArrayList;

public class TaskListFragment extends android.support.v4.app.Fragment
        implements TaskListAdapter.OnCardViewAdapterClickListener{
    //Callback setup for Activity communication
    public interface OnTaskListItemClicked{
        public void onTaskListItemClick(int position);
        public void onTaskListItemLongClick(int position);
        public void onTaskListItemChecked(int position, boolean checked);
        public void onTaskListAddButtonClick();
        public void onTaskListDragDropSwap(int positionA, int positionB);
    }

    //Class members
    public static final String TAG = "TaskListFragment";
    public static final String SERIAL_LIST_KEY = "SerialTaskList";
    private View rootView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerManager;
    private RecyclerView.Adapter recyclerAdapter;
    private ItemTouchHelper itemTouchHelper;
    private FloatingActionButton fab;
    private OnTaskListItemClicked callbackListener;

    private ArrayList<Task> taskList;

    public TaskListFragment(){}

    public static TaskListFragment newInstance(ArrayList<Task> taskList){
        TaskListFragment fragment = new TaskListFragment();
        if(taskList != null){
            Bundle args = new Bundle();
            args.putSerializable(SERIAL_LIST_KEY,taskList);
            fragment.setArguments(args);
        }
        return fragment;
    }

    //Override functions
    //-------------------------------------
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Activity a = (Activity)context;
        try{
            callbackListener = (OnTaskListItemClicked)a;
        } catch (ClassCastException e){
            throw new ClassCastException(a.toString() + " must implement OnTaskListItemClicked");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.taskList = new ArrayList<Task>();
        Bundle args = getArguments();
        if(args != null && args.containsKey(SERIAL_LIST_KEY)) {
            ArrayList<Task> temp = (ArrayList<Task>) args.getSerializable(SERIAL_LIST_KEY);
            this.taskList.addAll(temp);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.tasklist_layout,container,false);

        initRecycler();
        initFAB();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.tasklist_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Callback listener
    @Override
    public void onCardViewAdapterClicked(View view, int position){
        //handle changes to the fragment

        // signal to the activity that an item was clicked
        callbackListener.onTaskListItemClick(position);
    }

    @Override
    public void onCardViewAdapterLongClicked(View view, int position){
        callbackListener.onTaskListItemLongClick(position);
    }

    @Override
    public void onCardViewAdapterChecked(View view, int position, boolean checked){
        callbackListener.onTaskListItemChecked(position, checked);
    }

    @Override
    public void onCardViewAdapterStartDrag(RecyclerView.ViewHolder viewHolder){
        itemTouchHelper.startDrag(viewHolder);
    }

    //private functions
    //-------------------------------------

    //region RECYCLER
    private void initRecycler(){
        recyclerView = (RecyclerView)rootView.findViewById(R.id.taskview_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(recyclerManager);

        recyclerAdapter = new TaskListAdapter(taskList,this);
        recyclerView.setAdapter(recyclerAdapter);

        ItemTouchHelper.Callback simple = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0){
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
                callbackListener.onTaskListDragDropSwap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
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

    public void refreshRecyclerList(ArrayList<Task> tl){
        this.taskList.clear();
        this.taskList.addAll(tl);
        ((TaskListAdapter)recyclerAdapter).swap(this.taskList);
        recyclerAdapter.notifyDataSetChanged();
    }
    //endregion

    public void initFAB(){
        fab = (FloatingActionButton)rootView.findViewById(R.id.tasklist_floating_action_button);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                callbackListener.onTaskListAddButtonClick();
            }
        });
    }
}
