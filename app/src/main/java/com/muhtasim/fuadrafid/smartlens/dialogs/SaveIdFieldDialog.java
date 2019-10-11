package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.IdScanActivity;

/**
 * Created by Fuad Rafid on 9/3/2017.
 */

public class SaveIdFieldDialog {
    AlertDialog dialog;
    Context context;
    public static String path;
    public SaveIdFieldDialog(final Context context, final String field, String fieldName) {

        this.context=context;
        final Activity activity=(Activity)context;
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
        View mView=((Activity)context).getLayoutInflater().inflate(R.layout.id_field_dialog,null);
        final EditText fieldNameEt=(EditText)mView.findViewById(R.id.fieldNameEt);
        final EditText fieldEt=(EditText)mView.findViewById(R.id.fieldContentEt);
        fieldEt.setText(field);
        fieldNameEt.setText(fieldName);
        Button saveFieldButton=(Button)mView.findViewById(R.id.saveFieldButton);
        mBuilder.setView(mView);
        dialog= mBuilder.create();
        saveFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(fieldNameEt.getText().toString())
                        ||TextUtils.isEmpty(fieldEt.getText().toString()))
                {
                    Toast.makeText(activity, "Insert proper info to save", Toast.LENGTH_SHORT).show();
                    return;
                }
                ((IdScanActivity)activity).insertField(fieldNameEt.getText().toString(),fieldEt.getText().toString());

                dialog.cancel();
            }
        });

    }
    public void show()
    {
        dialog.show();
    }

}
