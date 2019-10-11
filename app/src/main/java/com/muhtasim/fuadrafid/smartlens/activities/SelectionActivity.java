/**
 * Created by Fuad Rafid on 7/17/2017.
 */
package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.others.Encryptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.muhtasim.fuadrafid.smartlens.R.id.Drawer;

public class SelectionActivity extends AppCompatActivity implements AnimationFragment.OnFragmentInteractionListener,SelectionFragment.OnFragmentInteractionListener{
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION =45 ;
    Context context;
    public DrawerLayout drawerLayout;
    private ActionBarDrawerToggle DrawerToggle;
    public GoogleApiClient mGoogleApiClient;
    int timesRun;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        //code for drawer or navigation bar
        //make button visible
        setSelectionFragment();
        setUpNavBar();
        setKey();
        context=this;
        getRating();
        GoogleSignInSetup();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);

        }

    }

    private void getRating() {
        timesRun=getTimesRun();
        if(timesRun%4==0)
        {
            new AlertDialog.Builder(this).setMessage("Like our app? Give us a 5 star rating.").setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                    }
                    timesRun=0;
                }
            }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timesRun=0;
                    dialog.cancel();
                }
            }).setNeutralButton("Rated Already", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timesRun=-1;
                }
            }).show();
        }
        if(timesRun>-1)
            setTimesRun();
    }

    private void setTimesRun() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("timesRun", timesRun+1);
        editor.commit();
    }

    private int getTimesRun() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 0;
        timesRun = sharedPref.getInt("timesRun", defaultValue);
        return timesRun;
    }

    private void setKey() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String key = sharedPref.getString("encKey", "nil");
        if(key.equals("nil"))
        {   key=Encryptor.randomString(5 + (int)(Math.random() * ((10 - 5) + 1)));
            editor.putString("encKey", key);
            editor.commit();
        }
        Encryptor.key=key;

    }

    private void setSelectionFragment() {
        SelectionFragment TargetFragment = new SelectionFragment();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container,TargetFragment);
        fragmentTransaction.commit();
    }

    private void setUpNavBar()
    {
        drawerLayout=(DrawerLayout)findViewById(Drawer);
        DrawerToggle=new ActionBarDrawerToggle(this,drawerLayout, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(DrawerToggle);
        DrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//make button visible
    }


    private void GoogleSignInSetup() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(context, "Failed to connect to google", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    public void LogOut(MenuItem item) {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null)
        {
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            if(LoginManager.getInstance()!=null)
            {
                LoginManager.getInstance().logOut();
            }
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(context,LoginActivity.class));
        }
        else
            Toast.makeText(context, "Not Logged In", Toast.LENGTH_SHORT).show();
    }
    public void LogIn(MenuItem item)
    {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(context,LoginActivity.class));
        }
        else
            Toast.makeText(context, "Logeed In Already", Toast.LENGTH_SHORT).show();
    }
    public void ShowProfile(MenuItem item){

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null)
        {
        String email=firebaseAuth.getCurrentUser().getDisplayName();
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
        View mView=getLayoutInflater().inflate(R.layout.profile_layout,null);
        final TextView emailView=(TextView) mView.findViewById(R.id.PersonEmail);
        emailView.setText("Account: "+email);
        mBuilder.setView(mView);
        final AlertDialog ProfileView= mBuilder.create();
        ProfileView.show();
            return;
        }
        else
            Toast.makeText(context, "Not Logged In", Toast.LENGTH_SHORT).show();
    }
    public void goToDatabase(MenuItem item)
    {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent=new Intent(SelectionActivity.this,DataBaseActivity.class);
            intent.putExtra("hidePanel",true);
            startActivity(intent);
            finish();
        }
        else
            Toast.makeText(context, "Login go see online files", Toast.LENGTH_SHORT).show();
    }
    public void ShowAboutUs(MenuItem item)
    {
       new AlertDialog.Builder(this).setMessage("Developed by:\n\n Muhtasim Fuad Rafid\n Afiya Fahmida Sarah\n\nSpecial thanks to:\n Capt Awal Uz Zaman\n Sadman Islam\n\n" +
               "Dept Of CSE, MIST").setTitle("About Us").setIcon(R.mipmap.icontry).show();
    }
    public void DeleteAccount(MenuItem item)
    {
        final boolean[] failed = {false};
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/" + firebaseAuth.getCurrentUser().getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChildren())
                    {

                    }
                    else
                        {
                            failed[0] =deleteDataOnline(databaseReference);
                        }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            if(failed[0])return;




            FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {

                        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        if(LoginManager.getInstance()!=null)
                        {
                            LoginManager.getInstance().logOut();
                        }
                        FirebaseAuth.getInstance()
                                .signOut();
                        Toast.makeText(context, "User deleted, logging out", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(SelectionActivity.this,LoginActivity.class));
                    }
                    else
                    {Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            });



        }
        else
            Toast.makeText(context, "Login first", Toast.LENGTH_SHORT).show();
    }

    private boolean deleteDataOnline(DatabaseReference databaseReference)
    {

        final boolean[] failed = new boolean[1];
        failed[0]=false;
        databaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    failed[0]=false;
                }
                else
                {
                    Toast.makeText(context, "Cannot delete online files", Toast.LENGTH_SHORT).show();
                    failed[0]=true;
                }

            }
        });
        return failed[0];
    }

    public void ShowPrivacyPolicy(MenuItem item)
    {
        String policyStr="";
        BufferedReader reader = null;
        try {
                InputStream is= getAssets().open("policy.txt");
                InputStreamReader streamReader=new InputStreamReader(is);
                reader = new BufferedReader(streamReader);

                String line;

                while ((line = reader.readLine()) != null) {
                    policyStr=policyStr+line+"\n";
                }
            }
     catch (Exception e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new AlertDialog.Builder(this).setMessage(policyStr).setTitle("Privacy Policy").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(DrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        UnlockDrawer();
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            UnlockDrawer();
            super.onBackPressed();
            return;

        }
        super.onBackPressed();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    public void lockDrawer()
    {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    public void UnlockDrawer()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            Log.e("TAG",""+resultCode);
            //Check if the permission is granted or not.
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available.Web form fill up won't work",
                        Toast.LENGTH_SHORT).show();


            }
        }
        
    }
}
