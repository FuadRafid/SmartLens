package com.muhtasim.fuadrafid.smartlens.activities; /**
 * Created by Fuad Rafid on 7/17/2017.
 */
/*
 * Copyright [2017] [Muhtasim Fuad Rafid]
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSource;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSourcePreview;
import com.muhtasim.fuadrafid.smartlens.camera.GraphicOverlay;
import com.muhtasim.fuadrafid.smartlens.camera.SimpleOrientationListener;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrGraphic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;
import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.finalText;
import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.readClicked;

public final class TextToSpeechActivity extends AppCompatActivity {
    private static final String TAG = "";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    private Button zoomIn,zoomOut,flashBtn;
    boolean flashOn;
    Context context;
    LinearLayout touchFocus;
    SimpleOrientationListener mOrientationListener;
    private TextToSpeech toSpeech;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle ocr) {
        super.onCreate(ocr);
        setContentView(R.layout.activity_text_to_speech);
        context=this;
        Initialize();
        int rc = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false);
        } else {
            requestCameraPermission();
        }
        setUpSpeech();
        setListeners();
        hideTouchIcon();

    }

    private void setUpSpeech() {
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            toSpeech.setLanguage(Locale.US);
                            toSpeech.speak("Touch anywhere on the screen, to read. \n" +
                                    "While speaking, touch the screen to stop", TextToSpeech.QUEUE_FLUSH, null,"DEFAULT");
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        toSpeech = new TextToSpeech(this.getApplicationContext(), listener);
        toSpeech.setSpeechRate((float) 0.40);
    }

    private void hideTouchIcon() {
                touchFocus.setVisibility(View.GONE);

    }

    private void Initialize() {
        mPreview = (CameraSourcePreview) findViewById(R.id.previewDoc);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlayDoc);
        flashOn=false;
        zoomIn=(Button)findViewById(R.id.zoomInScCrd);
        zoomOut=(Button)findViewById(R.id.zoomOutScCrd);
        flashBtn=(Button)findViewById(R.id.flashBtnScCrd);
        touchFocus=(LinearLayout)findViewById(R.id.touctoFocusScCrd);

    }

    private void setListeners() {
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mCameraSource.doZoom(2);
                }
                catch (Exception e)
                {
                    new AlertDialog.Builder(TextToSpeechActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
                            " contact the developer ar fuadrafid.dev@gmail.com for feedback. ")
                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                }
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mCameraSource.doZoom(-2);
                }
                catch (Exception e)
                {
                    new AlertDialog.Builder(TextToSpeechActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
                            " contact the developer ar fuadrafid.dev@gmail.com for feedback. ")
                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                }
            }
        });
        zoomIn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(zoomIn.isPressed()) {
                            mCameraSource.doZoom(2);
                        }
                        else
                            timer.cancel();
                    }
                },100,200);

                return true;
            }
        });

        zoomOut.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(zoomOut.isPressed()) {
                            mCameraSource.doZoom(-2);
                        }
                        else
                            timer.cancel();
                    }
                },100,200);

                return true;
            }
        });

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {

                if(hasFlash()){
                    if(!flashOn)
                    {
                        turnOnFlash();
                    }
                    else
                    {
                        turnOffFlash();
                    }
                }
                else
                    return;
            }
        });


    }

    public void turnOffFlash() {
        try {
            mCameraSource.setFlashMode(FLASH_MODE_OFF);
            flashBtn.setBackgroundResource(R.drawable.flashoff);
            flashOn = false;
        }
        catch (Exception e)
        {}
    }

    public void turnOnFlash() {
        try {
            mCameraSource.setFlashMode(FLASH_MODE_TORCH);
            flashBtn.setBackgroundResource(R.drawable.flashon);
            flashOn=true;
        }
        catch (Exception e)
        {}

    }

    //find if device has flash
    public boolean hasFlash() {
        try {
            Camera camera = mCameraSource.mCamera;
            if (camera == null) {

                return false;
            }

            Camera.Parameters parameters = camera.getParameters();

            if (parameters.getFlashMode() == null) {
                //camera.release();
                return false;
            }

            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(FLASH_MODE_OFF)) {
                //camera.release();
                return false;
            }

            //camera.release();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, RC_HANDLE_CAMERA_PERM);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        final Context context = getApplicationContext();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay, this,true) {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                int wordCount = 0, itemCount = 0;
                TextToSpeechActivity activity = (TextToSpeechActivity) contextHere;
                mGraphicOverlay.clear();
                SparseArray<TextBlock> items = detections.getDetectedItems();
                String FoundText = "";
                HashMap<Float, Text> foundText = new HashMap<Float, Text>();
                for (int i = 0; i < items.size(); ++i) {
                    TextBlock itemBlock = items.valueAt(i);
                    for (Text item : itemBlock.getComponents()) {
                        float y = item.getBoundingBox().centerY();
                        foundText.put(y, item);
                        StringTokenizer stringTokenizer = new StringTokenizer(item.getValue(), " ");

                        while (stringTokenizer.hasMoreTokens()) {
                            String token = stringTokenizer.nextToken();
                            if (wordChecker.contains(token)) {
                                wordCount++;
                                Log.e("wordFound", token);
                            }
                            itemCount++;

                        }
                        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, itemBlock);
                        mGraphicOverlay.add(graphic);
                    }

                }
                if(readClicked && !doneOnce)
                {
                    scanDone=false;
                    chagneCount=0;
                    doneOnce=true;
                }
                if(wordCount!=0){
                    chagneCount = 0;
                    scanDone=true;
                }
                Log.e("scandone",scanDone+"");
                if (!scanDone) {
                    Log.e("status",wordCount+" "+chagneCount+" "+itemCount);
                    if (chagneCount >= 4 )
                    {
                        int max=0;
                        int maxIndx=0;
                        for(int i=0;i<4;i++)
                        {
                            if(modes[i]>max)
                            {
                                max=modes[i];
                                maxIndx=i;

                                Log.e("found text",max+" "+maxIndx);
                            }
                        }
                        CameraSource.mRotation =maxIndx;
                        scanDone=true;
                        return;
                    }
                    chagneCount++;
                    if (readClicked && !scanDone) {
                        if(!activity.isSpeaking())
                        {finalText = "Finding text, please wait..";
                            activity.SpeakAdd();
                        }
                    }
                    modes[CameraSource.mRotation]=itemCount;
                    CameraSource.mRotation++;
                    CameraSource.mRotation=CameraSource.mRotation%4;
                    return;
                }

                if (readClicked ) {
                    doneOnce=false;
                    float prevY = 0, prevX = 0;
                    String prevString = "";
                    Map<Float, Text> map = new TreeMap<Float, Text>(foundText);
                    Set set2 = map.entrySet();
                    Iterator iterator2 = set2.iterator();
                    while (iterator2.hasNext()) {
                        Map.Entry me2 = (Map.Entry) iterator2.next();
                        FoundText = FoundText + " ";

                        if (Math.abs(((Text) me2.getValue()).getBoundingBox().centerY() - prevY) <= 30) {
                            Log.e("found1", me2.getKey() + " " + ((Text) me2.getValue()).getValue());
                            Log.e("found2", prevY + " " + prevString);
                            if (((Text) me2.getValue()).getBoundingBox().centerX() < prevX) {
                                FoundText = FoundText + ((Text) me2.getValue()).getValue();
                                Log.e("Added first", ((Text) me2.getValue()).getBoundingBox().centerX() + " " + ((Text) me2.getValue()).getValue());
                                Log.e("Added 2nd", prevX + " " + prevString);
                            } else {
                                FoundText = FoundText + prevString;
                                prevString = ((Text) me2.getValue()).getValue();
                                Log.e("Added first", prevX + " " + prevString);
                                Log.e("Added 2nd", ((Text) me2.getValue()).getBoundingBox().centerX() + " " + ((Text) me2.getValue()).getValue());
                            }
                        } else {
                            FoundText = FoundText + prevString;
                            prevY = ((Text) me2.getValue()).getBoundingBox().centerY();
                            prevX = ((Text) me2.getValue()).getBoundingBox().centerX();
                            prevString = ((Text) me2.getValue()).getValue();
                        }
                        FoundText = FoundText + "\n";
                        Log.e("found", me2.getKey() + " " + ((Text) me2.getValue()).getValue());
                    }
                    FoundText = FoundText + prevString;
                    finalText = FoundText;
                    if (finalText.isEmpty()) {
                        finalText = "No text found";
                    }
                    readClicked = false;
                    activity.Speak();
                    scanDone=false;
                    chagneCount=0;
                }
            }
        });
        if (!textRecognizer.isOperational())
        {


            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
            downloadCheck();
            Log.w(TAG, "Detector dependencies are not yet available.");
            new AlertDialog.Builder(this).setMessage("Files for text recognition are being downloaded. Please wait. This may take several minutes depending on your phone. " +
                    "You can close the app and come back later. Once download" +
                    " is finished, the text recognizer will be functional. Please make sure google play services is updated.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
            return;

        }


        // Creates and starts the com.example.fuadrafid.smartlens.camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.\

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(2592,4608)
                        .setRequestedFps(2.0f)
                        .setFlashMode(null)
                        .setFocusMode(autoFocus? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE:null)
                        .build();


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void downloadCheck()
    {


        if(!isNetworkAvailable(this))
        {

            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
            DialogInterface.OnClickListener listener2=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Smart Lens")
                    .setMessage("Active internet connection needed on first run to download files for " +
                            "text recognition. Please enable wifi or data")
                    .setPositiveButton("Okay", listener)
                    .setNegativeButton("Exit",listener2)
                    .show();


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
        toSpeech.stop();
    }

    /**
     * Releases the resources associated with the com.example.fuadrafid.smartlens.camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        toSpeech.stop();
        toSpeech.shutdown();
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the com.example.fuadrafid.smartlens.camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smart Lens")
                .setCancelable(false)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton("OK", listener)
                .show();

    }

    /**
     * Starts or restarts the com.example.fuadrafid.smartlens.camera source, if it exists.  If the com.example.fuadrafid.smartlens.camera source doesn't exist yet
     * (e.g., because onResume was called before the com.example.fuadrafid.smartlens.camera source was created), this will be called
     * again when the com.example.fuadrafid.smartlens.camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start com.example.fuadrafid.smartlens.camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isSpeaking())
            toSpeech.stop();
        else
            readClicked=true;
        return super.onTouchEvent(event);

    }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        if(toSpeech.isSpeaking())
        {
            toSpeech.stop();
            return;
        }
        toSpeech.shutdown();
        finish();
        startActivity(new Intent(TextToSpeechActivity.this,SelectionActivity.class));
    }

    @Override
    protected void onStop() {
        toSpeech.stop();
        super.onStop();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void Speak()
    {

        toSpeech.speak(finalText, TextToSpeech.QUEUE_FLUSH, null, "DEFAULT");
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void SpeakAdd()
    {

        toSpeech.speak(OcrDetectorProcessor.finalText, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
    }

    public boolean isSpeaking()
    {
        boolean speaking=true;
        try
        {
            speaking=toSpeech.isSpeaking();

        }
        catch (NullPointerException ex)
        {
            Log.e(TAG,"NullPointerException here");
        }
        return speaking;
    }

}
