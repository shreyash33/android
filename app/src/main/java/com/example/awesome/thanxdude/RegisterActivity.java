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
import com.google.firestore.admin.v1beta1.Progress;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email;
    private EditText reg_pass;
    private EditText reg_conf_pass;
    private Button regbtn;
    private Button reg_login_btn;
    private ProgressBar regprogress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email = findViewById(R.id.reg_email);
        reg_pass = findViewById(R.id.reg_pass);
        reg_conf_pass = findViewById(R.id.reg_confPass);
        regbtn = findViewById(R.id.reg_btn);
        reg_login_btn = findViewById(R.id.reg_login_btn);
        regprogress = findViewById(R.id.regprogressBar);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginintent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginintent);
                finish();

            }
        });

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = reg_email.getText().toString();
                String pass = reg_pass.getText().toString();
                String confpass = reg_conf_pass.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && pass.equals(confpass))
                {
                    regprogress.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Intent setupintent = new Intent(RegisterActivity.this,SetupActivity.class);
                                startActivity(setupintent);
                                finish();

                            }
                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this,"Error: " + error,Toast.LENGTH_LONG).show();
                                regprogress.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Check your Fields bro! ",Toast.LENGTH_LONG).show();

                }
                regprogress.setVisibility(View.INVISIBLE);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if(currentuser != null)
        {
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();

    }
}