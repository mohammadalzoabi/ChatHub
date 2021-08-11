package com.moalzoabi.chathub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

    EditText username;
    EditText email;
    EditText password;
    Button registerButton;
    TextView oldMember;

    FirebaseAuth auth;
    DatabaseReference myRef;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView imageViewLogo = findViewById(R.id.imageView);
        imageViewLogo.setOnClickListener(this);

        ConstraintLayout backgroundLayout = findViewById(R.id.cl_signup);
        backgroundLayout.setOnClickListener(this);

        username = findViewById(R.id.usernameEditText);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.RegisterButton);
        oldMember = findViewById(R.id.LoginTextView);

        mProgress = new ProgressDialog(RegisterActivity.this);
        mProgress.setTitle("Signing Up");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);


        auth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("MyUsers");



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.show();

                String usernameText = username.getText().toString();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if(TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)){
                    Toast.makeText(RegisterActivity.this, "Please Fill the Required Info", Toast.LENGTH_SHORT).show();
                }
                if (!TextUtils.isEmpty(usernameText) && !TextUtils.isEmpty(passwordText) && !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please Enter A Valid Email", Toast.LENGTH_SHORT).show();
                }
                if (!TextUtils.isEmpty(usernameText) &&
                        !TextUtils.isEmpty(emailText) &&
                        Patterns.EMAIL_ADDRESS.matcher(emailText).matches() && passwordText.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "The Password Must Be At Least 8 Characters Long", Toast.LENGTH_SHORT).show();
                }

                if(!TextUtils.isEmpty(usernameText) && !TextUtils.isEmpty(emailText) &&
                        Patterns.EMAIL_ADDRESS.matcher(emailText).matches() && passwordText.length()>=8){
                    Register(usernameText, emailText, passwordText);
                }
            }
        });

        oldMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void Register(String username, String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            myRef = FirebaseDatabase.getInstance()
                                    .getReference("MyUsers")
                                    .child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "Offline");
                            hashMap.put("typing", "noOne");
                            hashMap.put("bio", "I'm on ChatHub!");

                            myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        if (firebaseUser.isEmailVerified()) {
                                            mProgress.dismiss();
                                            Intent intent = new Intent(getApplicationContext(), MainHomeActivity.class);
                                            startActivity(intent);
                                        } else {
                                            mProgress.dismiss();
                                            firebaseUser.sendEmailVerification();
                                            Toast.makeText(RegisterActivity.this, "Check you email for verification.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                        finish();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            Register(username.getText().toString(), email.getText().toString(), password.getText().toString());
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageView || v.getId() == R.id.cl_signup) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focusedView = this.getCurrentFocus();
            if (focusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}