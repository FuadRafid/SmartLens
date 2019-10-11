/**
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
package com.muhtasim.fuadrafid.smartlens.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseAuth;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.camera.AutoFocusManager;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSource;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSourcePreview;
import com.muhtasim.fuadrafid.smartlens.camera.GraphicOverlay;
import com.muhtasim.fuadrafid.smartlens.camera.SimpleOrientationListener;
import com.muhtasim.fuadrafid.smartlens.dialogs.SaveTextDialog;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrGraphic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing com.example.fuadrafid.smartlens.camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class DocScanActivity extends AppCompatActivity {
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

    // Helper objects for detecting taps and pinches.
    private Button zoomIn,zoomOut,flashBtn,ReadBtn,gallery;
    boolean flashOn;
    Context context;
    LinearLayout touchFocus;
    SimpleOrientationListener mOrientationListener;
    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle ocr) {
        super.onCreate(ocr);
        setContentView(R.layout.activity_doc_scan);
        context=this;
        Initialize();
        //ask camera permission on first use
        int rc = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false);
            //Toast.makeText(this, "Hold the phone steady", Toast.LENGTH_LONG).show();
        } else {
            requestCameraPermission();
        }

        setListeners();

        hideTouchIcon();

        try {
            OcrGraphic.width = mCameraSource.getPreviewSize().getWidth();
        }
        catch (Exception e)
        {
            OcrGraphic.width= Resources.getSystem().getDisplayMetrics().widthPixels;
        }
    }

    private void hideTouchIcon() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                touchFocus.setVisibility(View.GONE);
            }
        },4500);
    }

    private void Initialize() {
        mPreview = (CameraSourcePreview) findViewById(R.id.previewDoc);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlayDoc);
        flashOn=false;
        zoomIn=(Button)findViewById(R.id.zoomInDoc);
        zoomOut=(Button)findViewById(R.id.zoomOutDoc);
        flashBtn=(Button)findViewById(R.id.flashBtnDoc);
        ReadBtn=(Button)findViewById(R.id.readDocBtn);
        touchFocus=(LinearLayout)findViewById(R.id.touctoFocusDs);
        gallery=(Button)findViewById(R.id.galleryDoc);
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
                    new AlertDialog.Builder(DocScanActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
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
                    new AlertDialog.Builder(DocScanActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
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
                        mCameraSource.setFlashMode(FLASH_MODE_TORCH);
                        flashBtn.setBackground(getResources().getDrawable(R.drawable.flashon));
                        flashOn=true;
                    }
                    else
                    {
                        mCameraSource.setFlashMode(FLASH_MODE_OFF);
                        flashBtn.setBackground(getResources().getDrawable(R.drawable.flashoff));
                        flashOn=false;
                    }
                }
                else
                    return;
            }
        });
        ReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OcrDetectorProcessor.readClicked=true;

            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DocScanActivity.this,GalleryImageActivity.class));
                finish();
            }
        });

        mOrientationListener = new SimpleOrientationListener(
                context) {

            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    CameraSource.mRotation =0;
                }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
                    CameraSource.mRotation =1;

                }
            }
        };
        mOrientationListener.enable();

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

    /**
     * Handles the requesting of the com.example.fuadrafid.smartlens.camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, RC_HANDLE_CAMERA_PERM);


    }



    /**
     * Creates and starts the com.example.fuadrafid.smartlens.camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        final Context context = getApplicationContext();
        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay, this,false) {

            //maim job done here
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                mGraphicOverlay.clear();
                String FoundText="";
                HashMap<Float, Text> foundText = new HashMap<Float, Text>();
                SparseArray<TextBlock> items = detections.getDetectedItems();

                for (int i = 0; i < items.size(); ++i) {

                    TextBlock itemBlock = items.valueAt(i);
                    for (Text item : itemBlock.getComponents()) {
                        float y = item.getBoundingBox().centerY();
                        foundText.put(y, item);

                    }
                    OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, itemBlock);
                    mGraphicOverlay.add(graphic);
                }


                if(readClicked)
                {

                    float prevY=0,prevX=0;
                    String prevString="";
                    boolean trailingText=false;
                    Map<Float, Text> map = new TreeMap<Float, Text>(foundText);
                    Set set2 = map.entrySet();
                    Iterator iterator2 = set2.iterator();
                    while(iterator2.hasNext()) {
                        Map.Entry me2 = (Map.Entry)iterator2.next();
                        FoundText=FoundText+" ";

                        if(Math.abs(((Text)me2.getValue()).getBoundingBox().centerY()-prevY)<=30)
                        {
                            Log.e("found1",me2.getKey()+" "+((Text)me2.getValue()).getValue());
                            Log.e("found2",prevY+" "+prevString);
                            if(((Text)me2.getValue()).getBoundingBox().centerX()<prevX)
                            {
                                FoundText=FoundText+ ((Text)me2.getValue()).getValue();
                                Log.e("Added first1",((Text)me2.getValue()).getBoundingBox().centerX()+" "+((Text)me2.getValue()).getValue());
                                Log.e("Added 2nd1",prevX+" "+prevString);
                            }
                            else
                            {
                                FoundText=FoundText+prevString;
                                prevString=((Text)me2.getValue()).getValue();
                                Log.e("Added first2",prevX+" "+prevString);
                                Log.e("Added 2nd2",((Text)me2.getValue()).getBoundingBox().centerX()+" "+((Text)me2.getValue()).getValue());
                            }
                            trailingText=true;
                        }
                        else
                        {
                            if(trailingText)
                                FoundText=FoundText+"          ";
                            FoundText=FoundText+"\n";
                            FoundText=FoundText+prevString;
                            prevY=((Text)me2.getValue()).getBoundingBox().centerY();
                            prevX=((Text)me2.getValue()).getBoundingBox().centerX();
                            prevString=((Text)me2.getValue()).getValue();
                        }

                        Log.e("found",me2.getKey()+" "+((Text)me2.getValue()).getValue());


                    }
                    FoundText=FoundText+prevString;
                    finalText=FoundText;
                    boolean textfound=true;
                    if(finalText.isEmpty())
                    {finalText="No text found";
                        textfound=false;
                    }

                    final boolean finalTextfound = textfound;
                    ((Activity)contextHere).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(finalTextfound)
                                    {
                                        if(FirebaseAuth.getInstance().getCurrentUser()==null)
                                        {
                                            startActivity(new Intent(contextHere, StorageActivity.class));
                                            ((Activity) contextHere).finish();
                                        }
                                        else
                                        {
                                            dialog.cancel();
                                            new AlertDialog.Builder(contextHere)
                                                    .setIcon(R.mipmap.icontry)
                                                    .setMessage("Select option")
                                                    .setPositiveButton("Save Offline", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            startActivity(new Intent(contextHere, StorageActivity.class));
                                                            ((Activity) contextHere).finish();
                                                        }
                                                    })
                                                    .setNegativeButton("Save Online", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            startActivity(new Intent(contextHere, DataBaseActivity.class));
                                                            ((Activity) contextHere).finish();
                                                        }
                                                    }).show();
                                        }
                                    }
                                    else
                                        Toast.makeText(contextHere, "Cannot save empty text", Toast.LENGTH_SHORT).show();
                                }
                            };

                            DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            };
                            DialogInterface.OnClickListener listener3 = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(finalTextfound)
                                    {
                                        startActivity(new Intent(contextHere, SendtoPcActivity.class));
                                        ((Activity) contextHere).finish();
                                    }
                                    else
                                        Toast.makeText(contextHere, "Cannot save empty text", Toast.LENGTH_SHORT).show();

                                }
                            };
                            new SaveTextDialog(contextHere,listener,listener2,listener3).show();

                            if(finalTextfound){

                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) contextHere.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", finalText);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(contextHere, "Text Copied to clipboard", Toast.LENGTH_SHORT).show();
                            }
                            if(flashOn)
                                flashBtn.callOnClick();
                            readClicked=false;

                        }
                    });

                }
            }
        });

        if (!textRecognizer.isOperational())
        {


            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            downloadCheck();
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
            new AlertDialog.Builder(this).setMessage("Files for text recognition are being downloaded. Please wait. This may take several minutes depending on your phone. " +
                    "You can close the app and come back later. Once download" +
                    " is finished, the text recognizer will be functional. Please make sure google play services is updated.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();

        }



        // Creates and starts the com.example.fuadrafid.smartlens.camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.\

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(2592,4608)
                        .setRequestedFps(2.0f)
                        .setFlashMode(null)
                        .setFocusMode(autoFocus? Camera.Parameters.FOCUS_MODE_AUTO:null)
                        .build();


    }

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
                    .setCancelable(false)
                    .setMessage("Active internet connection needed on first run to download files for " +
                            "text recognition. Please enable wifi or data")
                    .setPositiveButton("Okay", listener)
                    .setNegativeButton("Exit",listener2)
                    .show();


        }
    }

    /**
     * Restarts the com.example.fuadrafid.smartlens.camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }


    /**
     * Stops the com.example.fuadrafid.smartlens.camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the com.example.fuadrafid.smartlens.camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
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
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // boolean b = scaleGestureDetector.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_DOWN){

            float x = event.getX();
            float y = event.getY();


            Rect touchRect = new Rect(
                    (int)(x - 100),
                    (int)(y - 100),
                    (int)(x + 100),
                    (int)(y + 100));

            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000/mPreview.mSurfaceView.getWidth() - 1000,
                    touchRect.top * 2000/mPreview.mSurfaceView.getHeight() - 1000,
                    touchRect.right * 2000/mPreview.mSurfaceView.getWidth() - 1000,
                    touchRect.bottom * 2000/mPreview.mSurfaceView.getHeight() - 1000);

            try {
                AutoFocusManager focusManager = new AutoFocusManager(mCameraSource.mCamera);
                focusManager.doTouchFocus(targetFocusRect);
            }
            catch (Exception e)
            {
               try {
                   mCameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
               }
               catch (Exception e1)
               {

                   try {
                       mCameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                   }
                   catch (Exception e2)
                   {
                       Toast.makeText(this, "failed to focus", Toast.LENGTH_SHORT).show();
                   }
               }
            }


        }
        return true;
        // return b  || super.onTouchEvent(event);

    }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        startActivity(new Intent(DocScanActivity.this,SelectionActivity.class));
    }
}
