package com.therewillbebugs.todolist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

//An embeded list view that will expend its size based on the value for height "wrap_content" when
// It is inside of a recyclerview. Default listview must have its height set to a 'dp' amount
// this allows for wrap_content to be enabled.

//Source: http://stackoverflow.com/a/31674843
public class EmbeddedListView extends ListView {
    public EmbeddedListView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public EmbeddedListView(Context context){
        super(context);
    }
    public EmbeddedListView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}