package com.muhtasim.fuadrafid.smartlens.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.others.FloatingViewService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectionFragment extends Fragment {
    private LinearLayout DocScan, IdScan, WebForm, Recharge,Text2Speech;
    Context context;
    private boolean touchedAlready;
    View thisView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectionFragment newInstance(String param1, String param2) {
        SelectionFragment fragment = new SelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        thisView= inflater.inflate(R.layout.fragment_selection, container, false);
        Initialize();
        context=getActivity();
        return thisView;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    private void Initialize() {
        DocScan=(LinearLayout)thisView.findViewById(R.id.docScan);
        IdScan=(LinearLayout)thisView.findViewById(R.id.IdScan);
        WebForm=(LinearLayout)thisView.findViewById(R.id.FormFill);
        Recharge=(LinearLayout)thisView.findViewById(R.id.ScratchCard);
        Text2Speech=(LinearLayout)thisView.findViewById(R.id.TextToSpeech);
        touchedAlready=false;
        setTouchListeners();

    }


    private void setTouchListeners() {
        Recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touchedAlready)
                    return;
                touchedAlready=true;
                Recharge.setBackgroundColor(getResources().getColor(R.color.blue));
                //clearTouch();

                gotoAnimation("Recharge");

            }
        });

        WebForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touchedAlready)
                    return;
                touchedAlready=true;
                WebForm.setBackgroundColor(getResources().getColor(R.color.blue));
                try{
                    getActivity().stopService(new Intent(getActivity(), FloatingViewService.class));
                }
                catch (Exception e){}
                startActivity(new Intent(getActivity(),WebFormActivity.class));
                getActivity().finish();
               // clearTouch();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getActivity())) {
//                    ((SelectionActivity) getActivity()).initializePopUpView();
//                    getActivity().finish();
//                }
//                else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity()))
//                {
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + getActivity().getPackageName()));
//                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//                    touchedAlready=false;
//                    WebForm.setBackgroundColor(getResources().getColor(R.color.white));
//                }
//                else
//                {
//                    ((SelectionActivity) getActivity()).initializePopUpView();
//                    getActivity().finish();
//                }




            }
        });
        IdScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touchedAlready)
                    return;
                touchedAlready=true;
                IdScan.setBackgroundColor(getResources().getColor(R.color.blue));
                try{
                getActivity().stopService(new Intent(getActivity(), FloatingViewService.class));
                }
                catch (Exception e){}
                getActivity().startActivity(new Intent(getActivity(),IdScanActivity.class));
                getActivity().finish();

            }
        });
        DocScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touchedAlready)
                    return;
                touchedAlready=true;
                DocScan.setBackgroundColor(getResources().getColor(R.color.blue));
                gotoAnimation("Doc");
            }
        });
        Text2Speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    new AlertDialog.Builder(getActivity()).setMessage("Requires Android Lolipop and above").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();

                    return;
                }
                if(touchedAlready)
                    return;
                touchedAlready=true;
                Text2Speech.setBackgroundColor(getResources().getColor(R.color.blue));
                getActivity().startActivity(new Intent(getActivity(),TextToSpeechActivity.class));
                getActivity().finish();
            }
        });
    }

    private void gotoAnimation(final String TargetActivity) {
        try{
            getActivity().stopService(new Intent(getActivity(), FloatingViewService.class));
        }
        catch (Exception e){}
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimationFragment TargetFragment = new AnimationFragment();
                Bundle args = new Bundle();
                args.putString("Activity", TargetActivity);
                TargetFragment.setArguments(args);
                android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(this.getClass().getName());
                fragmentTransaction.replace(R.id.container,TargetFragment);
                fragmentTransaction.commit();
            }
        },400);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SelectionActivity)getActivity()).mGoogleApiClient.stopAutoManage(getActivity());
        ((SelectionActivity)getActivity()).mGoogleApiClient.disconnect();
    }
}
