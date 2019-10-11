package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.others.FloatingViewService;
import com.muhtasim.fuadrafid.smartlens.others.IdCardListAdapter;
import com.muhtasim.fuadrafid.smartlens.others.IdData;
import com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager;

import java.util.ArrayList;

import static com.muhtasim.fuadrafid.smartlens.activities.SelectionActivity.CODE_DRAW_OVER_OTHER_APP_PERMISSION;
import static com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager.entryNo;

public class WebFormActivity extends AppCompatActivity {
    SQLmanager sqLmanager;
    ArrayList<IdData> idDatas;
    ListView idList;
    IdCardListAdapter idCardListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_form);
        sqLmanager=new SQLmanager(this);
        idList=(ListView)findViewById(R.id.idCardList);

        idDatas=new ArrayList<>();
        getAlldata();

        idCardListAdapter =new IdCardListAdapter(this,idDatas);
        idList.setAdapter(idCardListAdapter);
        setClickers();

    }

    private void setClickers() {
        idList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                view.setBackgroundColor(getResources().getColor(R.color.blue));
                new AlertDialog.Builder(WebFormActivity.this).setMessage("Delete this profile?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLmanager.deleteData(Integer.toString(idDatas.get(position).getId()));
                        idDatas.remove(position);
                        idCardListAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        view.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                }).show();

                return true;
            }
        });

        idList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent popUpServiceIntent=new Intent(WebFormActivity.this, FloatingViewService.class);
                view.setBackgroundColor(getResources().getColor(R.color.blue));
                FloatingViewService.DataHolder=idDatas.get(position);
                startPopUp(popUpServiceIntent);



            }
        });
    }

    private void getAlldata() {
        Cursor allData = sqLmanager.getAllData();
        while(allData.moveToNext())
        {

            IdData dataNow=new IdData();
            dataNow.setId(Integer.parseInt(allData.getString(0)));
            for(int i=1;i<=entryNo;i++)
            {

                String fieldName,field;
                fieldName="";field="";
                String temp=allData.getString(i);
                if(temp!=null) {
                    String [] textsFound=temp.split("~@", 2);
                    if(textsFound.length>1)
                    {
                        fieldName=textsFound[0];
                        field= textsFound[1];
                    }
                    else
                        field=textsFound[0];
                }
                dataNow.addField(field);
                dataNow.addFieldName(fieldName);
            }
            idDatas.add(dataNow);

        }
        if(idDatas.isEmpty())
            Toast.makeText(this, "No profile found, scan ID cards to get profiles", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(WebFormActivity.this,SelectionActivity.class));
        finish();
    }
    private void startPopUp(Intent popUpintent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(WebFormActivity.this)) {
            startService(popUpintent);
            finish();
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(WebFormActivity.this))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
        else
        {
            startService(popUpintent);
            finish();
        }
    }
}
