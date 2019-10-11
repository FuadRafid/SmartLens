package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.muhtasim.fuadrafid.smartlens.R;

import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.finalText;

/**
 * Created by Fuad Rafid on 9/1/2017.
 */

public class SaveTextDialog {
    AlertDialog dialog;
    Context context;
    public static String path;
    public SaveTextDialog(final Context context, DialogInterface.OnClickListener listener,
                          DialogInterface.OnClickListener listener2,DialogInterface.OnClickListener listener3) {

        this.context=context;
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
        View mView=((Activity)context).getLayoutInflater().inflate(R.layout.save_text_dialog,null);
        final EditText foundText=(EditText)mView.findViewById(R.id.foundText);
        foundText.setText(finalText);
        mBuilder.setView(mView);
        mBuilder.setTitle("Smart Lens")
                .setIcon(R.mipmap.icontry)
                .setPositiveButton(R.string.label_save, listener)
                .setNegativeButton(R.string.send_to_pc,listener3)
                .setNeutralButton("Try Again",listener2)
                .setCancelable(false);

        dialog= mBuilder.create();

    }
    public void show()
    {
        dialog.show();
    }

}
