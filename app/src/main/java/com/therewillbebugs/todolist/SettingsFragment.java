package com.therewillbebugs.todolist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;

public class SettingsFragment extends android.support.v4.app.Fragment {
    public interface OnSettingsCompleteListener {
        public void onSettingsComplete(boolean settingsSaved);
    }

    public static final String TAG = "SettingsFragment";
    private static boolean notificationsEnabled;
    private static Task.PRIORITY_LEVEL defaultPriorityLevel;

    private OnSettingsCompleteListener listener;
    private RadioGroup defaultPriorityGroup;
    private CheckBox notificationsEnabledCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.settings_layout, container, false);

        notificationsEnabledCheckbox = (CheckBox)rootView.findViewById(R.id.enable_notifications);
        defaultPriorityGroup = (RadioGroup)rootView.findViewById(R.id.default_priority);

        notificationsEnabled = notificationsEnabledCheckbox.isChecked();
        defaultPriorityLevel = getSelectedPriorityLevel();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            listener = (OnSettingsCompleteListener)activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnSettingsCompleteListener");
        }
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
