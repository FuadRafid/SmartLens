package com.muhtasim.fuadrafid.smartlens.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.dialogs.SaveTextDialog;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.finalText;
import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.numLength;
import static com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor.prefix;


public class GalleryImageActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnProcess;
    Bitmap bitmap;
    private static final int RC_HANDLE_STORAGE_PERM = 2;
    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    int tryCount;
    ProgressDialog pd;
    TextRecognizer textRecognizer;
    boolean sharedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        sharedImage=false;
        pd=new ProgressDialog(this);
        pd.setMessage("Downloading files needed for text recognition....");
        pd.show();
        imageView = (ImageView)findViewById(R.id.image_view);
        btnProcess = (Button)findViewById(R.id.button_process);
        checkShare();
        if(textRecognizer.isOperational())
        {
            pd.hide();pd.cancel();
        }

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(textRecognizer.isOperational()){

                    try {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = textRecognizer.detect(frame);
                        StringBuilder stringBuilder = new StringBuilder();
                        if(getIntent().getBooleanExtra("recharge",false))
                        {
                           if(!getCardNumber(items))
                               Toast.makeText(GalleryImageActivity.this, "Wrong photo, try again!", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            readDoc(items,stringBuilder);
                        }
                    }
                    catch (NullPointerException e)
                    {
                        Log.e("detec","Error");
                        selectPicture();
                    }



                }

            }
        });

        tryCount=0;
        final Activity thisActivity = this;
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED && !sharedImage)
        {

            selectPicture();
        }
        else
        {


            final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_STORAGE_PERM);
                return;
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(thisActivity, permissions,
                            RC_HANDLE_STORAGE_PERM);
                }
            };
        }



    }

    private void checkShare() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
             if (type.startsWith("image/")) {
                handleSentImage(intent); // Handle single image being sent
            }
    }
    }

    private void handleSentImage(Intent imageReturnedIntent) {
        Uri selectedImage = (Uri) imageReturnedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(selectedImage==null)
            return;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if(cursor==null)
        {
            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            };
            AlertDialog.Builder alertExit=new AlertDialog.Builder(GalleryImageActivity.this)
                    .setMessage("Error occured while processing,the image might be too large, or permission to read images was denied")
                    .setPositiveButton("Exit",listener);
            alertExit.show();
            return;
        }
        if (cursor.moveToFirst()) {
            try {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                bitmap = BitmapFactory.decodeFile(filePath);
                ExifInterface exif = new ExifInterface(filePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                if (bitmap == null) selectPicture();
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap

            } catch (Exception e) {
                DialogInterface.OnClickListener linstener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                };
                AlertDialog.Builder alertExit = new AlertDialog.Builder(GalleryImageActivity.this)
                        .setMessage("Error occured while processing,the image might be too large")
                        .setPositiveButton("Exit", linstener);
                alertExit.show();
            }
            imageView.setImageBitmap(bitmap);
            Log.e("TAG", "found");
            sharedImage = true;

        }

    }

    private void readDoc(SparseArray<TextBlock> items, StringBuilder stringBuilder) {
        String FoundText="";
        HashMap<Float, String> foundText = new HashMap<Float, String>();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            float y=item.getBoundingBox().exactCenterY();
            foundText.put(y, item.getValue());
        }
        Map<Float, String> map = new TreeMap<Float, String>(foundText);
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            FoundText=FoundText+ me2.getValue();
            FoundText=FoundText+" ";
            Log.e("found",me2.getKey()+" "+ me2.getValue());
        }
        boolean textfound=true;
        OcrDetectorProcessor.finalText=FoundText;
        if(finalText.isEmpty())
        {finalText="No text found";
            textfound=false;
        }
        final boolean finalTextfound = textfound;
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(finalTextfound)
                {
                    Intent intent=new Intent(GalleryImageActivity.this,StorageActivity.class);
                    intent.putExtra("shared",sharedImage);
                    startActivity(intent);
                    if(!sharedImage)
                        finish();
                }
                else
                Toast.makeText(GalleryImageActivity.this, "Cannot save empty text", Toast.LENGTH_SHORT).show();
            }
        };

        DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                recreate();
            }
        };

        DialogInterface.OnClickListener listener3 = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(finalTextfound)
                {
                    Intent intent=new Intent(GalleryImageActivity.this,SendtoPcActivity.class);
                    intent.putExtra("shared",sharedImage);
                    startActivity(intent);

                    if(!sharedImage)
                        finish();
                }
                else
                Toast.makeText(GalleryImageActivity.this, "Cannot save empty text", Toast.LENGTH_SHORT).show();

            }
        };

        new SaveTextDialog(this,listener,listener2,listener3).show();
    }

    private boolean getCardNumber(SparseArray<TextBlock> items) {
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            for (Text t : item.getComponents()) {
                String cardNumber = t.getValue();
                cardNumber = cardNumber.replaceAll("\\s+", "");
                if (cardNumber.length() == numLength && cardNumber.matches("[0-9SDOo]+")) {
                    Log.e("msg", cardNumber);
                    cardNumber=cardNumber.toLowerCase();
                    cardNumber=cardNumber.replaceAll("s","5");
                    cardNumber=cardNumber.replaceAll("d","0");
                    cardNumber=cardNumber.replaceAll("o","0");
                    final Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+prefix+cardNumber+"%23"));
                    dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialIntent);
                    finish();
                    return true;
                }

            }

        }
        selectPicture();
        return  false;


    }

    private void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        //super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PICTURE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.e("in","kk");
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if(cursor==null)
                    {
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        };
                        AlertDialog.Builder alertExit=new AlertDialog.Builder(GalleryImageActivity.this)
                                .setMessage("Error occured while processing,the image might be too large, or permission to read images was denied")
                                .setPositiveButton("Exit",listener);
                        alertExit.show();
                        return;
                    }
                    if (cursor.moveToFirst()) {
                        try {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String filePath = cursor.getString(columnIndex);
                            bitmap = BitmapFactory.decodeFile(filePath);
                            ExifInterface exif = new ExifInterface(filePath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                            Log.d("EXIF", "Exif: " + orientation);
                            Matrix matrix = new Matrix();
                            if (orientation == 6) {
                                matrix.postRotate(90);
                            }
                            else if (orientation == 3) {
                                matrix.postRotate(180);
                            }
                            else if (orientation == 8) {
                                matrix.postRotate(270);
                            }
                            if(bitmap==null)selectPicture();
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight() , matrix, true); // rotating bitmap

                        }
                        catch (Exception e) {
                            DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            };
                            AlertDialog.Builder alertExit=new AlertDialog.Builder(GalleryImageActivity.this)
                                    .setMessage("Error occured while processing,the image might be too large")
                                    .setPositiveButton("Exit",listener);
                            alertExit.show();
                        }
                        imageView.setImageBitmap(bitmap);
                        Log.e("TAG","found");

                    }

                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(sharedImage)
            finish();
        else {
            startActivity(new Intent(GalleryImageActivity.this, SelectionActivity.class));
            finish();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_STORAGE_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!sharedImage)
                    selectPicture();

                } else {


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
