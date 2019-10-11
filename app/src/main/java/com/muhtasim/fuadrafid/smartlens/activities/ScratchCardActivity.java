/**
 * Created by Fuad Rafid on 7/17/2017.
 */
package com.muhtasim.fuadrafid.smartlens.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.camera.AutoFocusManager;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSource;
import com.muhtasim.fuadrafid.smartlens.camera.CameraSourcePreview;
import com.muhtasim.fuadrafid.smartlens.camera.GraphicOverlay;
import com.muhtasim.fuadrafid.smartlens.camera.SimpleOrientationListener;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrGraphic;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

/**
 * Activity for the multi-tracker app.  This app detects text and displays the value with the
 * rear facing com.example.fuadrafid.smartlens.camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class ScratchCardActivity extends AppCompatActivity {
    private static final String TAG = "";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static int selectedOperator=0;
    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private Button zoomIn,zoomOut,flashBtn,gallery;
    private Spinner Operators;
    boolean flashOn;
    private LinearLayout Indicator;
    LinearLayout touchFocus;
    SimpleOrientationListener mOrientationListener;
    int otherNum;
    String otherPref;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle ocr) {
        super.onCreate(ocr);
        setContentView(R.layout.activity_scratch_card);
        mPreview = (CameraSourcePreview) findViewById(R.id.previewScrCard);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlayScrCard);
        flashOn=false;
        // read parameters from the intent used to launch the activity.
        Initialize();
        int rc = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false);
            //Toast.makeText(this, "Hold the phone steady", Toast.LENGTH_LONG).show();
        } else {
            requestCameraPermission();
        }

        setListeners();
        disableIndicator();
        hideTouchIcon();

    }

    private void Initialize() {
        zoomIn=(Button)findViewById(R.id.zoomIn);
        zoomOut=(Button)findViewById(R.id.zoomOut);
        flashBtn=(Button)findViewById(R.id.flashBtn);
        Operators=(Spinner)findViewById(R.id.opertators);
        Indicator=(LinearLayout)findViewById(R.id.opIndicator);
        Operators.setSelection(selectedOperator);
        touchFocus=(LinearLayout)findViewById(R.id.touctoFocusSc);
        gallery=(Button)findViewById(R.id.gallery);
        otherNum=getIntent().getIntExtra("otherOp",0);
        otherPref=getIntent().getStringExtra("otherPref");
    }

    private void disableIndicator() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Indicator.setVisibility(View.GONE);
            }
        },8000);



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


    private void setListeners() {
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                mCameraSource.doZoom(2);
                }
                catch (Exception e)
                {
                    new AlertDialog.Builder(ScratchCardActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
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
                    new AlertDialog.Builder(ScratchCardActivity.this).setMessage("Sorry,an unexpected error occured. Please" +
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


        Operators.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        OcrDetectorProcessor.numLength=17;
                        OcrDetectorProcessor.numLength2=10;
                        OcrDetectorProcessor.prefix="*555*";
                        break;
                    case 1:
                        OcrDetectorProcessor.numLength=15;
                        OcrDetectorProcessor.numLength2=10;
                        OcrDetectorProcessor.prefix="*123*";
                        break;
                    case 2:
                        OcrDetectorProcessor.numLength=13;
                        OcrDetectorProcessor.numLength2=10;
                        OcrDetectorProcessor.prefix="*151*";
                        break;
                    case 3:
                        OcrDetectorProcessor.numLength=16;
                        OcrDetectorProcessor.numLength2=10;
                        OcrDetectorProcessor.prefix="*111*";
                        break;
                    case 4:
                        OcrDetectorProcessor.numLength=16;
                        OcrDetectorProcessor.numLength2=10;
                        OcrDetectorProcessor.prefix="*787*";
                        break;
                    case 5:
                        if(otherNum==0)
                            Toast.makeText(ScratchCardActivity.this, "Card Number Length Unknown", Toast.LENGTH_SHORT).show();
                        OcrDetectorProcessor.numLength=otherNum;
                        OcrDetectorProcessor.numLength2=otherNum;
                        OcrDetectorProcessor.prefix=otherPref;
                        break;

                }
//                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.blue));
//                ((TextView) parent.getChildAt(0)).setTypeface(null, Typeface.BOLD);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScratchCardActivity.this,GalleryImageActivity.class).putExtra("recharge",true));
                finish();
            }
        });

        mOrientationListener = new SimpleOrientationListener(
                ScratchCardActivity.this) {

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
        Context context = getApplicationContext();
        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay, this,false) {
            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                mGraphicOverlay.clear();
                SparseArray<TextBlock> items = detections.getDetectedItems();
                String cardNumber;
                for (int i = 0; i < items.size(); ++i) {
                    TextBlock item = items.valueAt(i);
                    for (Text t : item.getComponents()) {
                        cardNumber = t.getValue();
                        if(!cardNumber.contains(" "))continue;
                        cardNumber = cardNumber.replaceAll("\\s+", "");
                        cardNumber=cardNumber.toLowerCase();
                        if ((cardNumber.length() <= numLength && cardNumber.length() >=numLength2) && cardNumber.matches("[0-9sod]+")) {
                            Log.e("msg", cardNumber);
                            cardNumber=cardNumber.replaceAll("s","5");
                            cardNumber=cardNumber.replaceAll("d","0");
                            cardNumber=cardNumber.replaceAll("o","0");
                            if(!cardNumber.equals(prevCardNumber))
                            {
                                prevCardNumber=cardNumber;
                                tryCount++;
                                if(tryCount<2)
                                return;
                            }
                            if(CardNumfoundOnce){
                                CardNumfoundOnce=false;
                                return;
                            }
                            else
                            {
                                CardNumfoundOnce=true;
                            final Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+prefix+cardNumber+"%23"));
                            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            contextHere.startActivity(dialIntent);

                            }
                            return;
                        }

                    }
                    OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                    mGraphicOverlay.add(graphic);
                }
            }
        });

        if (!textRecognizer.isOperational()) {
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
                        .setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
                        .build();
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
                .setCancelable(false)
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

            Log.e("Preview size",mPreview.getWidth()+" "+mPreview.getHeight());

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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        startActivity(new Intent(ScratchCardActivity.this,SelectionActivity.class));
    }

}
