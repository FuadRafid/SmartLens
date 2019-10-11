/**
 * Created by Fuad Rafid on 7/17/2017.
 */
package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.muhtasim.fuadrafid.smartlens.R;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth firebaseAuth; //firebase login variable
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SharedPreferences sharedPref =getPreferences(Context.MODE_PRIVATE);
        boolean runonce=sharedPref.getBoolean("runOnce",false);
        if(runonce)
        {
            startActivity(new Intent(SplashScreen.this,SelectionActivity.class));
            finish();
            return;
        }
        else
        {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("runOnce", true);
            editor.commit();
        }
        context=this;
        firebaseAuth = FirebaseAuth.getInstance();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (firebaseAuth.getCurrentUser() == null){
                    finish();
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));

                }
                else {
                    finish();
                    startActivity(new Intent(SplashScreen.this,SelectionActivity.class ));

                }
            }
        },1500);
    }

}
