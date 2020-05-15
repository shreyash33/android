package com.example.awesome.thanxdude;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar maintoolbar;


    private  FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private String current_user_id;

    private FloatingActionButton addPostBtn;

    private BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;

    private AccountFragment accountFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        maintoolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(maintoolbar);
        getSupportActionBar().setTitle("ThanxDude");

        if(mAuth.getCurrentUser() != null)
        {



            mainBottomNav = findViewById(R.id.mainBottomView);

            //FRAGMENTS
            homeFragment = new HomeFragment();

            accountFragment = new AccountFragment();

            //replaceFragment(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId())
                    {
                        case R.id.bottom_home:
                            replaceFragment(homeFragment);
                            return true;

                        case R.id.bottom_account:
                            replaceFragment(accountFragment);
                            return true;

                        default:
                            return false;
                    }
                }
            });

            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newpostintent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newpostintent);
                    finish();

                }
            });

        }

    }

    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null)
        {
            sendtologin();
        }
        else
        {
            current_user_id = mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            Intent setupintent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupintent);
                            finish();
                        }
                    }
                    else
                    {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error: " + error,Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        switch(item.getItemId())
        {
            case R.id.action_logout_btn:
                logout();
                return  true;
            case R.id.action_accountsetup_btn:
                Intent setupintent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(setupintent);
                finish();
                return  true;


            default:
                return  false;

        }

    }
    private void sendtologin() {
        Intent loginintent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginintent);
        finish();

    }

    private void logout() {
        mAuth.signOut();
        sendtologin();
    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

}
