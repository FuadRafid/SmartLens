package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.SelectionActivity;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;
import com.snatik.storage.Storage;

/**
 * Created by Fuad Rafid on 8/16/2017.
 */

public class SaveFileDialog {
    AlertDialog dialog;
    Context context;
    public static String path;
    public SaveFileDialog(final Context context, final String savePath) {

        this.context=context;
        path=savePath+"/text.txt";
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
        mBuilder.setIcon(R.mipmap.icontry);
        mBuilder.setTitle("Smart Lens");
        View mView=((Activity)context).getLayoutInflater().inflate(R.layout.save_file_dialog,null);
        final Spinner fileTypes=(Spinner)mView.findViewById(R.id.fileTypes);
        final EditText fileName=(EditText)mView.findViewById(R.id.etFilename);
        DialogInterface.OnClickListener listener= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 path=savePath+"/"+fileName.getText().toString()+"."+fileTypes.getSelectedItem().toString();
                 new Storage(context).createFile(path, OcrDetectorProcessor.finalText);
                 dialog.cancel();
                 Toast.makeText(context, "File Saved", Toast.LENGTH_LONG).show();
                 ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                context.startActivity(new Intent(((Activity) context), SelectionActivity.class));
            }
        };
        mBuilder.setView(mView).setTitle("Save Text").setPositiveButton("Save",listener);
        dialog= mBuilder.create();

    }
    public void show()
    {
        dialog.show();
    }

}
