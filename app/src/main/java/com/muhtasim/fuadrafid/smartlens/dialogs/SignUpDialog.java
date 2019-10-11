package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

public class SignUpDialog {
    Context context;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    SharedPreferences sharedPref;
    AlertDialog dialog;
    public SignUpDialog(final Context context)
    {
        this.context=context;
        progressDialog=new ProgressDialog(context);
        firebaseAuth=FirebaseAuth.getInstance();
        sharedPref= ((Activity)context).getPreferences(Context.MODE_PRIVATE);

        //making the dialog from xml
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);

        View mView=((Activity)context).getLayoutInflater().inflate(R.layout.sign_up_layout,null);
        final EditText etEmail=(EditText) mView.findViewById(R.id.emailET);
        final EditText etPass=(EditText) mView.findViewById(R.id.passET);
        Button signUpBtn=(Button) mView.findViewById(R.id.signupButton);
        mBuilder.setView(mView);
        dialog= mBuilder.create();
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser(etEmail,etPass,dialog);
            }
        });

    }
    public void registerUser(EditText editTextEmail, EditText editTextPassword, final AlertDialog SignUp) {

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

        if (password.length() <= 5) {
            Toast.makeText(context, "Password must be more than 5 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering, Please Wait...");
        progressDialog.show();



        //now we can create the user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context, "Successfully Registered, verify email", Toast.LENGTH_SHORT).show();
                            firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context,
                                                "Verification email sent to " +email,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            progressDialog.hide();progressDialog.cancel();SignUp.cancel();
                            firebaseAuth.signOut();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("User Email",email);
                            Log.e("em",email);
                            editor.commit();




                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, "Email already exists!", Toast.LENGTH_SHORT).show();
                                progressDialog.hide();
                                return;
                            }
                            Toast.makeText(context, "Something went terrible wrong!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                            return;
                        }
                    }
                });
    }
    public void show()
    {
        dialog.show();

    }
}
