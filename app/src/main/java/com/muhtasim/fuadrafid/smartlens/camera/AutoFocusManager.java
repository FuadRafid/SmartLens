/**
 * Created by Fuad Rafid on 7/17/2017.
 */
package com.muhtasim.fuadrafid.smartlens.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class AutoFocusManager {
    public static int FOCUS_AREA_SIZE =200 ;
    Camera camera;
    public AutoFocusManager(Camera camera)
    {
        this.camera=camera;
    }


    public void doTouchFocus(final Rect tfocusRect) {
        Log.e("Err", "TouchFocus");
        try {

            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters para =camera.getParameters();
            para.setFocusAreas(focusList);
            Log.e("camerasize",para.getPreviewSize().width+" "+para.getPreviewSize().height);
            para.setMeteringAreas(focusList);
            camera.setParameters(para);

            camera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Err", e.toString());
        }

    }
    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0){
                camera.cancelAutoFocus();
                Log.e("Err", "Able to autofocus");
            }
        }
    };

}
