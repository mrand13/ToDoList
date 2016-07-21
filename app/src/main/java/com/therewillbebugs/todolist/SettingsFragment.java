package com.therewillbebugs.todolist;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;

public class SettingsFragment extends android.support.v4.app.Fragment {
    public interface OnSettingsCompleteListener {
        void onSettingsComplete(boolean settingsSaved);
    }

    public static final String TAG = "SettingsFragment";
    public static final String NOTIFICATIONS_ENABLED_KEY = "NotificationsEnabled";
    public static final String DEFAULT_PRIORITY_KEY = "DefaultPriority";

    private static boolean notificationsEnabled;
    private static Task.PRIORITY_LEVEL defaultPriorityLevel;

    private View rootView;
    private OnSettingsCompleteListener listener;
    private RadioGroup defaultPriorityGroup;
    private CheckBox notificationsEnabledCheckbox;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            listener = (OnSettingsCompleteListener)activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnSettingsCompleteListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            notificationsEnabled = savedInstanceState.getBoolean(NOTIFICATIONS_ENABLED_KEY);
            defaultPriorityLevel
                    = Task.PRIORITY_LEVEL.get(savedInstanceState.getInt(DEFAULT_PRIORITY_KEY));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.settings_layout, container, false);

        notificationsEnabledCheckbox = (CheckBox)rootView.findViewById(R.id.enable_notifications);
        defaultPriorityGroup = (RadioGroup)rootView.findViewById(R.id.default_priority);

        notificationsEnabled = notificationsEnabledCheckbox.isChecked();
        defaultPriorityLevel = getSelectedPriorityLevel();
        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(
            NOTIFICATIONS_ENABLED_KEY,
            ((CheckBox)getView().findViewById(R.id.enable_notifications)).isChecked()
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
        int radioButtonID = defaultPriorityGroup.getCheckedRadioButtonId();
        View radioButton = defaultPriorityGroup.findViewById(radioButtonID);
        int index = defaultPriorityGroup.indexOfChild(radioButton);
        return Task.PRIORITY_LEVEL.get(index);
    }
}
