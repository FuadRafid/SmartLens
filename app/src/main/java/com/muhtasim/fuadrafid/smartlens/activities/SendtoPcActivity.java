package com.muhtasim.fuadrafid.smartlens.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendtoPcActivity extends AppCompatActivity {
    private static final String TAG ="Network" ;
    EditText e2;
    Button sendBtn;
    private static Socket s;
    private static PrintWriter printWriter;
    private static String ip="192.168.1.1";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendto_pc);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending text, please wait. Make sure your PC Client is running....");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.icontry)
                .setTitle("Smart Lens")
                .setMessage("Connect to the same wifi as pc or via hotspot. Make sure the PC Client is running on your pc")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton("Download PC Client", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/open?id=0B2TskRdZU2QcM0ViaUdZWWlUblU"));
                startActivity(browserIntent);
            }
        }).show();

        e2=(EditText)findViewById(R.id.ip_address);
        sendBtn=(Button)findViewById(R.id.sendPcBtn);
        //new NetworkSniffTask(this).execute();
        String netIp=getIp(this);
        if(netIp.equals("0.0.0."))netIp="192.168.43.";
        Log.e("ip",netIp);
        e2.setText(netIp);
        e2.setSelection(netIp.length());


    }

    public void send_text(View v)
    {
        myTask mt= new myTask();
        mt.execute();
        ip=e2.getText().toString();


    }
    class myTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            SendtoPcActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
            try {
                s = new Socket();
                s.setTcpNoDelay(true);
                s.connect(new InetSocketAddress(ip, 5000),5000);
                printWriter=new PrintWriter(s.getOutputStream());
                printWriter.write(OcrDetectorProcessor.finalText);
                printWriter.flush();
                printWriter.close();
                s.close();

                SendtoPcActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SendtoPcActivity.this, "Text sent", Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                });


            }
            catch (Exception e) {
               SendtoPcActivity.this.runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(SendtoPcActivity.this, "No device found at IP: "+ip , Toast.LENGTH_SHORT).show();
                       progressDialog.hide();
                   }
               });
            }
            return null;
        }
    }
//    class NetworkSniffTask extends AsyncTask<Void, Void, Void> {
//
//        private static final String TAG = "nstask";
//
//        private Context mContextRef;
//
//        public NetworkSniffTask(Context context) {
//            mContextRef = context;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            Log.e(TAG, "Let's sniff the network");
//
//            try {
//                Context context = mContextRef;
//
//
//
//            } catch (Throwable t) {
//                Log.e(TAG, "Well that's not good.", t);
//            }
//
//            return null;
//        }
//    }
    private String getIp(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        String ipString = Formatter.formatIpAddress(ipAddress);


        Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));

        Log.d(TAG, "ipString: " + String.valueOf(ipString));

        String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
        Log.d(TAG, "prefix: " + prefix);
        return prefix;
    }

    @Override
    public void onBackPressed() {
        try
        {
            s.shutdownOutput();
            s.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        progressDialog.cancel();
        if(getIntent().getBooleanExtra("shared",false))
        {finish();return;}
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        startActivity(new Intent(SendtoPcActivity.this,SelectionActivity.class));

    }
}
