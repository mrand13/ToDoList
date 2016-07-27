package com.therewillbebugs.todolist;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Source: https://firebase.google.com/docs/auth/android/password-auth
public class LoginActivity extends AppCompatActivity {
    //Activity class members
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthListener;
    private AppCompatButton loginButton;
    private ProgressDialog progressDialog;
    private EditText email, password;
    private TextView link_signup, help;

    //Result calllbacks
    private static final int REQUEST_SIGNUP_RESULT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        initFirebase();
        initComponents();
    }

    @Override
    public void onStart(){
        super.onStart();
        fbAuth.addAuthStateListener(fbAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        hideProgressDialog();
        if(fbAuthListener != null)
            fbAuth.removeAuthStateListener(fbAuthListener);
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_SIGNUP_RESULT){
            if(resultCode == RESULT_OK){
            }
        }
    }

    private void initFirebase(){
        fbAuth = FirebaseAuth.getInstance();
        fbAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //User is signed in
                    //Launch Task Actvitiy
                    startTaskActivity();
                }
                else{
                    //User is signed out
                }
            }
        };
    }

    private void signIn(String email, String password){
        if(validateLogin()) {
            showProgressDialog();
            fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                    }
                    hideProgressDialog();
                }
            });
        }
    }

    //ValidateLogin: Validetes the form for empty fields only
    private boolean validateLogin(){
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

    private void initComponents(){
        this.email = (EditText)findViewById(R.id.login_input_email);
        this.password = (EditText)findViewById(R.id.login_input_password);
        this.loginButton = (AppCompatButton)findViewById(R.id.login_btn_login);
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(email.getText().toString(),password.getText().toString());

            }
        });
        this.link_signup = (TextView)findViewById(R.id.login_link_signup);
        this.link_signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
                startActivityForResult(intent,REQUEST_SIGNUP_RESULT);
            }
        });
    }

    private void startTaskActivity(){
        this.finish();
        Intent taskIntent = new Intent(getApplicationContext(),TaskActivity.class);
        startActivity(taskIntent);
    }

    //Progress Dialog
    private void showProgressDialog(){
        if(progressDialog == null) {
            progressDialog = ProgressDialog.show(LoginActivity.this, "Please Wait ...", "Logging you in ...", true);
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
