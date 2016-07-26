package com.therewillbebugs.todolist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private EditText email, password;
    private AppCompatButton signupButton;
    private TextView link_login;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        initComponents();
    }

    @Override
    public void onBackPressed(){
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void initComponents(){
        this.email = (EditText)findViewById(R.id.signup_input_email);
        this.password = (EditText)findViewById(R.id.signup_input_password);
        this.signupButton = (AppCompatButton)findViewById(R.id.signup_btn_signup);
        this.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(email.getText().toString(), password.getText().toString());

            }
        });
        this.link_login = (TextView)findViewById(R.id.signup_link_login);
        this.link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void signUp(final String email, final String pass){
        if(validateSignup()){
            showProgressDialog();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(SignupActivity.this, "Creating Account failed!", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                    else{
                        hideProgressDialog();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }
            });
        }
    }

    //validateSignup: Validetes the form for empty fields only
    private boolean validateSignup(){
        boolean flag = true;
        //check for empty email field, set/reset error
        if(email.getText().toString().isEmpty()){
            email.setError("Required");
            flag = false;
        } else email.setError(null);
        //check for empty password field, set/reset error
        if(password.getText().toString().isEmpty()){
            password.setError("Required");
            flag = false;
        }
        else if(password.getText().toString().length() < 6){
            password.setError("Password must be at least 6 characters");
            flag = false;
        }
        else password.setError(null);
        return flag;
    }

    //Progress Dialog
    private void showProgressDialog(){
        if(progressDialog == null) {
            progressDialog = ProgressDialog.show(SignupActivity.this, "Please Wait ...", "Creating Account ...", true);
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    private void hideProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
