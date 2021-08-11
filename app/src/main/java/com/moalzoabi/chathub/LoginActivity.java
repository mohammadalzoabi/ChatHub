package com.moalzoabi.chathub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.firebase.database.annotations.NotNull;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    EditText email;
    EditText password;
    Button loginButton;
    TextView newMember;
    TextView resetPassword;

    ConstraintLayout cl_login;
    ImageView logo;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.emailLoginEditText);
        password = findViewById(R.id.passwordLoginEditText);
        loginButton = findViewById(R.id.LoginButton);
        newMember = findViewById(R.id.RegisterTextView);
        resetPassword = findViewById(R.id.forgotPasswordTextView);

        cl_login = findViewById(R.id.cl_login);
        logo = findViewById(R.id.imageView2);
        cl_login.setOnClickListener(this);
        logo.setOnClickListener(this);

        mProgress = new ProgressDialog(LoginActivity.this);
        mProgress.setTitle("Logging In");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainHomeActivity.class);
            startActivity(intent);
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.show();
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(LoginActivity.this, "Please Enter the Required Info", Toast.LENGTH_SHORT).show();
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches() && !TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(LoginActivity.this, "Please Enter A Valid Email", Toast.LENGTH_SHORT).show();
                }

                if(Patterns.EMAIL_ADDRESS.matcher(emailText).matches() && !TextUtils.isEmpty(passwordText)) {
                    auth.signInWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if(firebaseUser.isEmailVerified()){
                                            mProgress.dismiss();
                                            Intent intent = new Intent(getApplicationContext(), MainHomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else{
                                            Toast.makeText(LoginActivity.this, "Please Verify Your Email Address to Login.", Toast.LENGTH_SHORT).show();
                                            mProgress.dismiss();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        newMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageView2 || v.getId() == R.id.cl_login) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focusedView = this.getCurrentFocus();
            if (focusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
        }
        return false;
    }
}