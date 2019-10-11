package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.SelectionActivity;

/**
 * Created by Fuad Rafid on 8/15/2017.
 */

public class SignInDialog {
    Context context;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    SharedPreferences sharedPref;
    AlertDialog dialog;
    public SignInDialog(final Context context)
    {
        this.context=context;
        progressDialog=new ProgressDialog(context);
        firebaseAuth=FirebaseAuth.getInstance();
        sharedPref= ((Activity)context).getPreferences(Context.MODE_PRIVATE);

        if (firebaseAuth.getCurrentUser() != null ){
            firebaseAuth.signOut();
        }
        String email=sharedPref.getString("User Email",null);

        if(email!=null)
            Log.e("tag",email);


        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);

        View mView=((Activity)context).getLayoutInflater().inflate(R.layout.sign_up_layout,null);
        final EditText etEmail=(EditText) mView.findViewById(R.id.emailET);
        final EditText etPass=(EditText) mView.findViewById(R.id.passET);

        Button signUpBtn=(Button) mView.findViewById(R.id.signupButton);
        signUpBtn.setText("Sign In");

        etEmail.setText(email);
        mBuilder.setView(mView);
        dialog= mBuilder.create();
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin(etEmail,etPass,dialog);

            }
        });

    }

    private void userLogin(EditText editTextEmail, EditText editTextPassword, final AlertDialog SignUp) {

        //getting all the inputs from the user
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //validation
        if (TextUtils.isEmpty(email)){
            Toast.makeText(context, "Please enter email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)){
            Toast.makeText(context, "Please enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() <= 5){
            Toast.makeText(context, "Password must be more than 5 characters!", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("User Email",email);
        Log.e("em",email);
        editor.commit();

        progressDialog.setMessage("Signing in, Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            if(firebaseAuth.getCurrentUser().isEmailVerified())
                            {
                                progressDialog.hide();
                                progressDialog.cancel();
                                SignUp.cancel();
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("User Email", email);
                                Log.e("em", email);
                                editor.commit();
                                ((Activity) context).finish();
                                context.startActivity(new Intent(context, SelectionActivity.class));
                            }
                            else
                            {Toast.makeText(context, "Email not verified!", Toast.LENGTH_SHORT).show();
                                progressDialog.hide();progressDialog.cancel();
                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context,
                                                    "Verification email sent to " + firebaseAuth.getCurrentUser().getEmail(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(context, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });

    }
    public void show()
    {
        dialog.show();

    }
}
