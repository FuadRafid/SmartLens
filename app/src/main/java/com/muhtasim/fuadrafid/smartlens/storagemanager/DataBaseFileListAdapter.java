package com.muhtasim.fuadrafid.smartlens.storagemanager;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.DataBaseActivity;
import com.muhtasim.fuadrafid.smartlens.others.Encryptor;

import java.util.ArrayList;

/**
 * Created by Fuad Rafid on 8/20/2017.
 */

public class DataBaseFileListAdapter extends ArrayAdapter<FileData> {
    private Activity context;
    ArrayList<FileData> fileList;
    public DataBaseFileListAdapter(Activity context, ArrayList<FileData> fileList) {
        super(context, R.layout.fileitem_database ,fileList);

        this.context=context;
        this.fileList=fileList;

    }



    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=context.getLayoutInflater();

        View listItem=layoutInflater.inflate(R.layout.fileitem_database,null);
        LinearLayout itemView=(LinearLayout)listItem.findViewById(R.id.listItems);
        TextView tvFileName = (TextView) listItem.findViewById(R.id.fileNameView);
        TextView tvFileContent = (TextView) listItem.findViewById(R.id.fileContent);

        final FileData fileData=fileList.get(position);

        tvFileName.setText("File Name: "+fileData.getName());
        String content=fileData.getFileData();
        if(content.length()>50)
            content=content.substring(0,50)+"...";
        else if(content.isEmpty())
            content="No text in file";
        tvFileContent.setText(content);
        final String finalContent = content;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateData(fileData,position);
            }
        });

        return listItem;
    }

    private void updateData(final FileData fileData,final int position) {
        AlertDialog dialog;
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
        View mView= context.getLayoutInflater().inflate(R.layout.save_text_dialog,null);
        final EditText foundText=(EditText)mView.findViewById(R.id.foundText);
        foundText.setText(fileData.getFileData());
        mBuilder.setView(mView);
        mBuilder.setTitle(fileData.getName())
                .setPositiveButton(R.string.label_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((DataBaseActivity)context).dataBaseManager.updateData
                                (new FileData(fileData.getName(),Encryptor.encrypt(foundText.getText().toString(),Encryptor.key)),position);
                        dialog.cancel();
                    }
                }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((DataBaseActivity)context).dataBaseManager.updateData
                        (null,position);
                dialog.cancel();

            }
        });
        dialog= mBuilder.create();
        dialog.show();
    }


}
