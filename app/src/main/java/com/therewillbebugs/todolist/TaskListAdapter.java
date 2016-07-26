package com.therewillbebugs.todolist;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//Source:
//https://developer.android.com/training/material/lists-cards.html
//http://code.tutsplus.com/tutorials/getting-started-with-recyclerview-and-cardview-on-android--cms-23465

//An adapter class to hold the recycler view components
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder>{
    //Callback interface for activity
    public interface OnCardViewAdapterClickListener{
        void onCardViewAdapterClicked(View v, int position);
        void onCardViewAdapterLongClicked(View v, int position);
        void onCardViewAdapterChecked(View v, int position, boolean checked);
        void onCardViewAdapterStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    //Class members
    private ArrayList<Task> taskList;
    private OnCardViewAdapterClickListener cbClickListener;

    //Reference to the views
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{
        public CardView cv;
        public TextView title, description, priority, timedate;
        public CheckBox complete;
        private View view;

        public interface OnCardViewClickListener{
            void cardViewOnClick(View v, int position);
            void cardViewOnLongClick(View v, int position);
            void cardViewOnChecked(View v, int position);
        }

        private OnCardViewClickListener clickListener;

        public ViewHolder(View view, OnCardViewClickListener listener){
            super(view);
            cv = (CardView)view.findViewById(R.id.taskview_cardview);
            title = (TextView)view.findViewById(R.id.textview_task_title);
            description = (TextView)view.findViewById(R.id.textview_task_description);
            priority = (TextView)view.findViewById(R.id.textview_task_priority);
            timedate = (TextView)view.findViewById(R.id.textview_task_timedate);
            complete =  (CheckBox)view.findViewById(R.id.tasklist_item_cbox_complete);

            this.clickListener = listener;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            this.view = view;

            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.cardViewOnChecked(v, getLayoutPosition());
                }
            });
        }

        @Override
        public void onClick(View view){
            clickListener.cardViewOnClick(view, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View view){
            clickListener.cardViewOnLongClick(view, getLayoutPosition());
            return true;
        }

        //Change the fade based on completeness
        public void toggleComplete(boolean complete){
            if(complete) {
                view.setAlpha(0.5f);
                this.complete.setChecked(true);
            }
            else{
                view.setAlpha(1.0f);
                this.complete.setChecked(false);
            }
        }
    }

    public TaskListAdapter(ArrayList<Task> taskList, OnCardViewAdapterClickListener callbackListener){
        this.taskList = new ArrayList<>();
        this.taskList.addAll(taskList);
        this.cbClickListener = callbackListener;
    }

    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasklist_item,parent,false);
        TaskListAdapter.ViewHolder  vh = new TaskListAdapter.ViewHolder(v, new ViewHolder.OnCardViewClickListener(){
            @Override
            public void cardViewOnClick(View view, int position){
                cbClickListener.onCardViewAdapterClicked(view,position);
            }

            @Override
            public void cardViewOnLongClick(View view, int position){
                cbClickListener.onCardViewAdapterLongClicked(view, position);
            }

            @Override
            public void cardViewOnChecked(View view, int position){
                boolean checked = ((CheckBox)view).isChecked();
                cbClickListener.onCardViewAdapterChecked(view,position,checked);
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.ViewHolder  holder, int position){
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.timedate.setText(task.getDateTimeString());

        //priority
        if(task.getPriorityLevel() != Task.PRIORITY_LEVEL.NONE)
            holder.priority.setText(task.getPriorityLevel().toString());
        else holder.priority.setText("");

        //complete status
        holder.toggleComplete(task.isComplete());

        //Drag and Drop Support
        final TaskListAdapter.ViewHolder tempHolder = holder;
        holder.cv.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                    cbClickListener.onCardViewAdapterStartDrag(tempHolder);
                return false;
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount(){
        return taskList.size();
    }

    public void swap(ArrayList<Task> tl){
        this.taskList.clear();
        this.taskList.addAll(tl);
        notifyDataSetChanged();
    }
}