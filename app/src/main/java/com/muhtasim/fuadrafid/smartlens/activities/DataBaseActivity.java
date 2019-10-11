package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;
import com.muhtasim.fuadrafid.smartlens.others.Encryptor;
import com.muhtasim.fuadrafid.smartlens.storagemanager.DataBaseManager;
import com.muhtasim.fuadrafid.smartlens.storagemanager.FileData;

public class DataBaseActivity extends AppCompatActivity {
    public DataBaseManager dataBaseManager;
    EditText fileNameEt;
    Button saveOnlineButton;
    ListView list;
    FirebaseAuth firebaseAuth;
    LinearLayout savePanel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!isNetworkAvailable(this))
        {
            Toast.makeText(this, "Internet connection required", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DataBaseActivity.this,SelectionActivity.class));
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base);

        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null)
        {
            Toast.makeText(this, "Login to save online", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DataBaseActivity.this,LoginActivity.class));
            finish();
            return;
        }
        savePanel=(LinearLayout)findViewById(R.id.savePanel);

        boolean hidePanel=getIntent().getBooleanExtra("hidePanel",false);
        list=(ListView)findViewById(R.id.fileList);
        fileNameEt=(EditText)findViewById(R.id.EtFileName);
        saveOnlineButton=(Button)findViewById(R.id.dataBaseSaveBtn);
        dataBaseManager=new DataBaseManager(DataBaseActivity.this,list,hidePanel);
        if(hidePanel)
        {

            savePanel.setVisibility(View.GONE);
            return;
        }
        saveOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataBaseManager.setFileData(new FileData(fileNameEt.getText().toString(),OcrDetectorProcessor.finalText));
                if(dataBaseManager.sendData())
                    Toast.makeText(DataBaseActivity.this, "Data Saved Online", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(DataBaseActivity.this,SelectionActivity.class));

    }
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
