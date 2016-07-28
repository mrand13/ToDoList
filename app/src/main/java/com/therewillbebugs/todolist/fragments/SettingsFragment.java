package com.therewillbebugs.todolist.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.therewillbebugs.todolist.R;
import com.therewillbebugs.todolist.task_components.Task;

public class SettingsFragment extends android.support.v4.app.Fragment {
    public interface OnSettingsCompleteListener {
        void onSettingsComplete(boolean settingsSaved);
    }

    public static final String TAG = "SettingsFragment";
    public static final String NOTIFICATIONS_ENABLED_KEY = "NotificationsEnabled";
    public static final String DEFAULT_PRIORITY_KEY = "DefaultPriority";

    private static boolean notificationsEnabled;
    private static Task.PRIORITY_LEVEL defaultPriorityLevel = Task.PRIORITY_LEVEL.NONE;

    private View rootView;
    private OnSettingsCompleteListener listener;
    private RadioGroup defaultPriorityGroup;
    private CheckBox notificationsEnabledCheckbox;
    private Button saveButton, cancelButton;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            listener = (OnSettingsCompleteListener)context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString() + " must implement OnSettingsCompleteListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            notificationsEnabled
                = savedInstanceState.getBoolean(NOTIFICATIONS_ENABLED_KEY);
            defaultPriorityLevel
                = Task.PRIORITY_LEVEL.get(savedInstanceState.getInt(DEFAULT_PRIORITY_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.settings_layout, container, false);

        notificationsEnabledCheckbox = (CheckBox)rootView.findViewById(R.id.enable_notifications);
        defaultPriorityGroup         = (RadioGroup)rootView.findViewById(R.id.default_priority);
        saveButton                   = (Button)rootView.findViewById(R.id.save_btn);
        cancelButton                 = (Button)rootView.findViewById(R.id.cancel_btn);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });

        // retrieve settings from SharedPreferences should they exist
        SharedPreferences settings = getActivity().getSharedPreferences("settings", Context.MODE_APPEND);
        if (settings != null) {
            notificationsEnabled
                = settings.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
            ((CheckBox)rootView.findViewById(R.id.enable_notifications)).setChecked(notificationsEnabled);

            defaultPriorityLevel
                = Task.PRIORITY_LEVEL.get(settings.getInt(DEFAULT_PRIORITY_KEY, 0));
            ((RadioButton)defaultPriorityGroup.getChildAt(defaultPriorityLevel.getVal())).setChecked(true);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        SharedPreferences.Editor outState
            = getActivity().getSharedPreferences("settings", Context.MODE_APPEND).edit();

        outState.putInt(DEFAULT_PRIORITY_KEY, defaultPriorityLevel.getVal());
        outState.putBoolean(
            NOTIFICATIONS_ENABLED_KEY,
            ((CheckBox)rootView.findViewById(R.id.enable_notifications)).isChecked()
        );

        outState.commit();

        super.onPause();
    }

    // should persist user settings when fragment is popped from backstack
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(
            NOTIFICATIONS_ENABLED_KEY,
            ((CheckBox)rootView.findViewById(R.id.enable_notifications)).isChecked()
        );

        outState.putInt(DEFAULT_PRIORITY_KEY, defaultPriorityLevel.getVal());
    }

    public static boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public static Task.PRIORITY_LEVEL getDefaultPriorityLevel() {
        return defaultPriorityLevel;
    }

    public void onSaveClick(View view) {
        notificationsEnabled = notificationsEnabledCheckbox.isChecked();
        defaultPriorityLevel = getSelectedPriorityLevel();

        listener.onSettingsComplete(true);
    }

    public void onCancelClick(View view) {
        listener.onSettingsComplete(false);
    }

    private Task.PRIORITY_LEVEL getSelectedPriorityLevel() {
        int  radioButtonID = defaultPriorityGroup.getCheckedRadioButtonId();
        View radioButton   = defaultPriorityGroup.findViewById(radioButtonID);
        int  index         = defaultPriorityGroup.indexOfChild(radioButton);

        return Task.PRIORITY_LEVEL.get(index);
    }
}
