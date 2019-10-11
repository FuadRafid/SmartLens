package com.muhtasim.fuadrafid.smartlens.others;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.SelectionActivity;

import static com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager.entryNo;

public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    public static IdData DataHolder;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null)
        {
            onDestroy();
            return super.onStartCommand(intent, flags, startId);
        }
        if(DataHolder!=null)
        {
            Log.e("DataFLow","ashenai");
        }
        CreatePopUp();
        return super.onStartCommand(intent, flags, startId);

    }
    public void CreatePopUp()
    {
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        //Add the view to the window.
        final WindowManager.LayoutParams params;
//        = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //The root element of the collapsed view layout
        final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = mFloatingView.findViewById(R.id.expanded_container);


        //Set the close button
        ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the from from the window
                stopSelf();
            }
        });

        //Set the view while floating view is expanded.
        //Set the play button.
        final TextView fields[]=new TextView[10];

        fields[0]=(TextView)mFloatingView.findViewById(R.id.Tv1);
        fields[1]=(TextView)mFloatingView.findViewById(R.id.Tv2);
        fields[2]=(TextView)mFloatingView.findViewById(R.id.Tv3);
        fields[3]=(TextView)mFloatingView.findViewById(R.id.Tv4);
        fields[4]=(TextView)mFloatingView.findViewById(R.id.Tv5);
        fields[5]=(TextView)mFloatingView.findViewById(R.id.Tv6);
        fields[6]=(TextView)mFloatingView.findViewById(R.id.Tv7);
        fields[7]=(TextView)mFloatingView.findViewById(R.id.Tv8);
        fields[8]=(TextView)mFloatingView.findViewById(R.id.Tv9);
        fields[9]=(TextView)mFloatingView.findViewById(R.id.Tv10);

        for(int i=0;i<entryNo;i++)
        {


            if(DataHolder.getField(i)==null)
            {
                fields[i].setVisibility(View.GONE);
            }
            else if(DataHolder.getField(i).isEmpty())
            {
                fields[i].setVisibility(View.GONE);
            }
            fields[i].setText(DataHolder.getFieldName(i)+": "+DataHolder.getField(i));
            final int finalI = i;
            fields[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fields[finalI].setBackgroundColor(getResources().getColor(R.color.blue));
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Id Field", DataHolder.getField(finalI));
                    clipboard.setPrimaryClip(clip);

                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fields[finalI].setBackgroundColor(getResources().getColor(R.color.white));
                        }
                    },700);

                    Toast.makeText(FloatingViewService.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

                }
            });
        }




        ImageView closeButton = (ImageView) mFloatingView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });

        //Open the application on thi button click
        ImageView openButton = (ImageView) mFloatingView.findViewById(R.id.open_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the application  click.
                Intent intent = new Intent(FloatingViewService.this, SelectionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove view from the view hierarchy
                stopSelf();
            }
        });

        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }
}
