/*
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
package com.muhtasim.fuadrafid.smartlens.ocr;

import android.content.Context;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.muhtasim.fuadrafid.smartlens.camera.GraphicOverlay;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public abstract class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    public static boolean readClicked;
    public int tryCount;
    public GraphicOverlay<OcrGraphic> mGraphicOverlay;
    public Context contextHere;
    public static int numLength,numLength2;
    public String prevCardNumber;
    public static String prefix,finalText;
    public boolean CardNumfoundOnce;
    public WordChecker wordChecker;
    public int chagneCount;
    public int modes[]=new int[4];
    public static boolean scanDone;
    public boolean doneOnce;
    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context CallingContext,boolean wordCheck) {
        mGraphicOverlay = ocrGraphicOverlay;
        contextHere=CallingContext;
        tryCount=0;
        numLength=17;
        readClicked=false;
        finalText="";
        prevCardNumber="";
        numLength2=10;
        CardNumfoundOnce=false;
        if(wordCheck)
        wordChecker=new WordChecker(contextHere);
        scanDone=false;
        doneOnce=false;

    }
    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public abstract void receiveDetections(Detector.Detections<TextBlock> detections);

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
