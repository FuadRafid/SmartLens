package com.muhtasim.fuadrafid.smartlens.storagemanager;

import android.app.Activity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhtasim.fuadrafid.smartlens.others.Encryptor;

import java.util.ArrayList;

/**
 * Created by Fuad Rafid on 8/20/2017.
 */

public class DataBaseManager {
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FileData fileData;
    Activity context;
    ArrayList<FileData> fileDatas;
    ArrayList<String> keys;
    ListView list;
    boolean hidePanel;

    public DataBaseManager(Activity context, ListView list,boolean hidePanel)
    {
        this.context=context;
        this.list=list;
        this.fileDatas=new ArrayList<FileData>();
        keys=new ArrayList<String>();
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference("users/"+firebaseAuth.getCurrentUser().getUid());
        this.hidePanel=hidePanel;
        setListener();
    }

    private void setListener() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fileDatas.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {

                    FileData fileData = snapshot.getValue(FileData.class);
                    keys.add(snapshot.getKey());
                    fileDatas.add(fileData);


                }
                if(fileDatas.isEmpty() && hidePanel)
                {
                    Toast.makeText(context, "No files saved online", Toast.LENGTH_SHORT).show();
                }
                DataBaseFileListAdapter dbAdapter = new DataBaseFileListAdapter(context,fileDatas);
                list.setAdapter(dbAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean sendData()
    {

        for(FileData filedata: fileDatas)
        {
            if(filedata.getName().equals(fileData.getName()))
            {
                Toast.makeText(context, "File already exists", Toast.LENGTH_SHORT).show();
                return  false;
            }
        }
        try{
        String id = databaseReference.push().getKey();
            Log.e("fileData",fileData.getFileData()+" "+id);
        databaseReference.child(id).setValue(fileData);
        return true;
        }
        catch (Exception e)
        {
            return false;
        }


    }
    public boolean updateData(FileData updateData,int pos)
    {
        try {

            databaseReference.child(keys.get(pos)).setValue(updateData);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }
}
