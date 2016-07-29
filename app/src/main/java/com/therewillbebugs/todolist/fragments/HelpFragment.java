package com.therewillbebugs.todolist.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.therewillbebugs.todolist.R;

import org.w3c.dom.Text;

//Help fragment for displaying information about how to use the app
public class HelpFragment extends android.support.v4.app.Fragment {
    public interface OnHelpCompleteListener {
        void onHelpComplete(boolean settingsSaved);
    }

    public static final String TAG = "HelpFragment";

    private View rootView;
    private Context context;
    private OnHelpCompleteListener listener;
    private AppCompatButton closeButton;
    private TextView github,portal;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = context;
            listener = (OnHelpCompleteListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHelpCompleteListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.help_layout, container, false);

        //Init the view components here
        closeButton = (AppCompatButton)rootView.findViewById(R.id.help_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback listener with value true to signal fragment completion
                listener.onHelpComplete(true);
            }
        });

        portal = (TextView)rootView.findViewById(R.id.portal);
        github = (TextView)rootView.findViewById(R.id.github);
        portal.setMovementMethod(LinkMovementMethod.getInstance());
        github.setMovementMethod(LinkMovementMethod.getInstance());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}