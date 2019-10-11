package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.dialogs.SaveIdFieldDialog;
import com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import static com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager.entryNo;

public class IdScanActivity extends AppCompatActivity implements IPickResult{
    CropImageView cropImageView;
    Button getFieldButton,doneButton;
    TextRecognizer textRecognizer;
    Bitmap image;
    String fields[];
    int counter;
    SQLmanager sqLmanager;
    boolean alreadyInserted;
    int Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_scan);
        cropImageView=(CropImageView)findViewById(R.id.cropImageView);
        getFieldButton=(Button)findViewById(R.id.getFieldBtn);
        doneButton=(Button)findViewById(R.id.saveIdBtn);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        fields=new String[entryNo];counter=0;
        setClicker();
        sqLmanager=new SQLmanager(this);
        alreadyInserted=false;
        Id=1;

        new AlertDialog.Builder(this).setMessage("To manually enter profile data, load any picture and keep pressing Get Field").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                pickImage();
            }
        }).
                show();
        //Toast.makeText(this, "To manually enter profile data, load any picture and keep pressing Get Field", Toast.LENGTH_LONG).show();
    }

    private void pickImage() {
        PickImageDialog.build(new PickSetup().setGalleryIcon(R.drawable.gallerycust)
                .setCameraIcon(R.drawable.cameracust)).setOnPickCancel(new IPickCancel() {
            @Override
            public void onCancelClick() {

            }
        }).show(this);
    }

    private void setClicker() {
        getFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter==entryNo)
                {
                    Toast.makeText(IdScanActivity.this, "Cannot save more than "+entryNo+" fields", Toast.LENGTH_SHORT).show();
                }
                image=cropImageView.getCroppedImage();
                if(image==null)
                {pickImage();return;}
                Frame frame = new Frame.Builder().setBitmap(image).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                String textFound="";
                for(int i=0;i<items.size();i++)
                {
                    textFound=textFound+items.valueAt(i).getValue();
                    textFound=textFound+" ";

                }
                textFound=textFound.trim();
                String [] textsFound;
                textsFound=textFound.split(":|-",2);

                String fieldName,field;
                field=textFound;
                fieldName="";
                if(textsFound.length>1)
                {
                    fieldName=textsFound[0];
                    field=textsFound[1];
                }
                new SaveIdFieldDialog(IdScanActivity.this,field,fieldName).show();




            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(IdScanActivity.this).setMessage("Scan another side of the card?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                pickImage();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        showSavedIds();
                    }
                }).show();



            }

            private void showSavedIds() {
                if(!alreadyInserted)
                    insertData();
                alreadyInserted=true;
                Cursor allData = sqLmanager.getAllData();
                final StringBuffer result= new StringBuffer();
                String temp;
                while(allData.moveToNext())
                {
                    String id=Integer.toString(Id);
                    result.append("ID: "+id+"\n");
                    for(int i=1;i<=entryNo;i++)
                    {

                        temp=allData.getString(i);
                        if(temp!=null) {
                            String [] textsFound=temp.split("~@", 2);
                            if(textsFound.length>1)
                                result.append(textsFound[0] + ": " + textsFound[1] + "\n");
                            else
                                result.append(textsFound[0]);

                        }

                    }
                    result.append("\n\n");
                    Id++;
                }
                new AlertDialog.Builder(IdScanActivity.this).setMessage(result.toString()).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                        startActivity(new Intent(IdScanActivity.this,SelectionActivity.class));
                    }
                }).show();
            }
        });
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            cropImageView.setImageBitmap(r.getBitmap());
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(IdScanActivity.this,SelectionActivity.class));
        finish();
        super.onBackPressed();
    }
    public void insertData()
    {
        Cursor allData = sqLmanager.getAllData();
        if(allData.getCount()>=10)
        {
            Toast.makeText(this, "Cannot save more than 10 IDs", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean empty=true;
        for(String field:fields)
        {
            if(field!=null)
                empty=false;
        }
        if(!empty)
        sqLmanager.insertData(fields);
    }
    public void insertField(String fieldName,String field)
    {
        if(counter>=entryNo)
        {
            Toast.makeText(this, "Cannot save more than"+entryNo+"fields", Toast.LENGTH_SHORT).show();
            return;
        }
        fields[counter]=fieldName+"~@"+field;
        counter++;
        Toast.makeText(this, "Field Saved", Toast.LENGTH_SHORT).show();
    }
}
