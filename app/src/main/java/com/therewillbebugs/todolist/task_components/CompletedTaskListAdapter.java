package com.therewillbebugs.todolist.task_components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.therewillbebugs.todolist.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//An adapter class to hold the recycler view components
public class CompletedTaskListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //Callback interface for activity
    public interface OnCardViewAdapterClickListener{
        void onCardViewAdapterClicked(View v, int position);
    }

    //Class members
    private ArrayList<Task> allCompletedTasks;
    private OnCardViewAdapterClickListener cbClickListener;
    private Map<String,ArrayList<Task>> completedTasksByDate;
    private Iterator mapIterator;
    private int itemSize;

    //Reference to the views
    public static class EmptyViewHolder extends RecyclerView.ViewHolder{
        public EmptyViewHolder(View v){
            super(v);
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public CardView cv;
        public TextView title_date, ratio;
        public ListView completed_list;
        public String title;
        public double ratioVal;

        public interface OnCompleteCardViewClickListener{
            void cardViewOnClick(View v, int position);
        }

        private OnCompleteCardViewClickListener clickListener;

        public ViewHolder(View view, ArrayList<Task> completed, OnCompleteCardViewClickListener listener){
            super(view);
            cv = (CardView)view.findViewById(R.id.completedlist_cardview);
            title_date = (TextView)view.findViewById(R.id.completed_task_card_header);
            ratio = (TextView)view.findViewById(R.id.completed_task_card_ratio);
            completed_list = (ListView)view.findViewById(R.id.completed_task_card_listview);

            //Set up the ratio
            int totalComplete = 0;
            for(Task t : completed){
                if(t.isComplete())
                    totalComplete++;
            }

            //List setup
            CompletedTaskAdapter taskArrayAdapter = new CompletedTaskAdapter(view.getContext(),completed);
            completed_list.setAdapter(taskArrayAdapter);
            if(completed.size() > 0) {
                this.title = completed.get(0).getDateToString();
                ratioVal = ((double)(totalComplete))/((double)(completed.size()));
                ratioVal *= 100;
            }
            else{ this.title = "null";ratioVal = 0.0;}

            //Listeners
            this.clickListener = listener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            clickListener.cardViewOnClick(view, getLayoutPosition());
        }
    }

    public CompletedTaskListAdapter(ArrayList<Task> taskList, OnCardViewAdapterClickListener callbackListener){
        this.allCompletedTasks = new ArrayList<>();
        this.allCompletedTasks.addAll(taskList);
        this.cbClickListener = callbackListener;
        initCompletedMap();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        ArrayList<Task> completedTaskDay = new ArrayList<>();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.completedlist_card, parent, false);
        if(mapIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) mapIterator.next();
            completedTaskDay.addAll((ArrayList<Task>) pair.getValue());
            mapIterator.remove();

            CompletedTaskListAdapter.ViewHolder vh = new CompletedTaskListAdapter.ViewHolder(v, completedTaskDay, new CompletedTaskListAdapter.ViewHolder.OnCompleteCardViewClickListener() {
                @Override
                public void cardViewOnClick(View view, int position) {
                    cbClickListener.onCardViewAdapterClicked(view, position);
                }
            });
            return vh;
        }
        else{
            RecyclerView.ViewHolder vh = new CompletedTaskListAdapter.EmptyViewHolder(v);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder  holder, int position){
        if(holder instanceof CompletedTaskListAdapter.ViewHolder) {
            CompletedTaskListAdapter.ViewHolder vh = (CompletedTaskListAdapter.ViewHolder) holder;
            vh.title_date.setText(vh.title);
            vh.ratio.setText("Completed " + Math.round(vh.ratioVal) + "% of daily tasks");
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount(){
        return itemSize;
    }

    public void swap(ArrayList<Task> tl){
        this.allCompletedTasks.clear();
        this.allCompletedTasks.addAll(tl);
        initCompletedMap();
        notifyDataSetChanged();
    }

    private void initCompletedMap(){
        this.itemSize = 0;
        if(this.completedTasksByDate != null)
            this.completedTasksByDate.clear();
        this.completedTasksByDate = new HashMap<String,ArrayList<Task>>();
        for(Task task : allCompletedTasks){
            String key = task.getDateToDBString();
            if(!this.completedTasksByDate.containsKey(key)) {
                ArrayList<Task> temp = new ArrayList<>();
                temp.add(task);
                this.completedTasksByDate.put(key, temp);
                itemSize++;
            }
            else this.completedTasksByDate.get(key).add(task);
        }
        this.mapIterator = this.completedTasksByDate.entrySet().iterator();
    }
}

//Source: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
class CompletedTaskAdapter extends ArrayAdapter<Task>{
    public CompletedTaskAdapter(Context context, ArrayList<Task> completedTasks){
        super(context,0,completedTasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Task task = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.completedlist_card_list_item, parent, false);
        }
        ImageView image = (ImageView)convertView.findViewById(R.id.completed_task_card_listview_image);
        TextView title = (TextView)convertView.findViewById(R.id.completed_task_card_listview_title);
        TextView time = (TextView)convertView.findViewById(R.id.completed_task_card_listview_time);

        if(task.isComplete()) {
            image.setImageResource(R.drawable.ic_done_black);
            image.setColorFilter(getContext().getResources().getColor(R.color.greenHighlight));
        }
        else {
            image.setImageResource(R.drawable.ic_priority_black);
            image.setColorFilter(getContext().getResources().getColor(R.color.redHighlight));
        }
        title.setText(task.getTitle());
        time.setText(task.getTimeToString());

        return convertView;
    }
}