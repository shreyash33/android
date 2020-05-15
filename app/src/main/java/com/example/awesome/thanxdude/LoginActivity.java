package com.example.awesome.thanxdude;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmailText = findViewById(R.id.reg_email);
        loginPassText = findViewById(R.id.reg_pass);
        loginBtn = findViewById(R.id.reg_btn);
        loginRegBtn = findViewById(R.id.reg_login_btn);
        loginProgress = findViewById(R.id.regprogressBar);

        mAuth = FirebaseAuth.getInstance();

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regintent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regintent);
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass) )
                {
                    loginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail, loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                sendtomain();

                            }
                            else
                            {
                                String errormessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"error:" + errormessage,Toast.LENGTH_LONG).show();
                                loginProgress.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }
                loginProgress.setVisibility(View.INVISIBLE);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser = mAuth.getCurrentUser();

        if(currentuser != null)
        {
           sendtomain();
        }
    }

    private void sendtomain() {
        Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();

    }
}
