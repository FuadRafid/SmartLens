package com.muhtasim.fuadrafid.smartlens.others;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager;

import java.util.ArrayList;

import static com.muhtasim.fuadrafid.smartlens.activities.SelectionActivity.CODE_DRAW_OVER_OTHER_APP_PERMISSION;
import static com.muhtasim.fuadrafid.smartlens.storagemanager.SQLmanager.entryNo;

/**
 * Created by Fuad Rafid on 9/17/2017.
 */

public class IdCardListAdapter extends ArrayAdapter <IdData>{
    ArrayList<IdData> idDatas;
    Context context;
    Activity activity;
    public IdCardListAdapter(Context context,ArrayList<IdData> idDatas) {
        super(context, R.layout.id_list_layout ,idDatas);
        this.idDatas=idDatas;
        this.context=context;
        this.activity=(Activity)context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=activity.getLayoutInflater();
        final View list=layoutInflater.inflate(R.layout.id_list_layout,null);

        TextView fields[]= new TextView[10];

        fields[0]=(TextView)list.findViewById(R.id.field1);
        fields[1]=(TextView)list.findViewById(R.id.field2);
        fields[2]=(TextView)list.findViewById(R.id.field3);
        fields[3]=(TextView)list.findViewById(R.id.field4);
        fields[4]=(TextView)list.findViewById(R.id.field5);
        fields[5]=(TextView)list.findViewById(R.id.field6);
        fields[6]=(TextView)list.findViewById(R.id.field7);
        fields[7]=(TextView)list.findViewById(R.id.field8);
        fields[8]=(TextView)list.findViewById(R.id.field9);
        fields[9]=(TextView)list.findViewById(R.id.field10);

        IdData tempDataHolder=idDatas.get(position);
        for(int i=0;i<entryNo;i++)
        {
            if(!tempDataHolder.getField(i).isEmpty())
            {
                fields[i].setVisibility(View.VISIBLE);
                fields[i].setText(tempDataHolder.getFieldName(i)+": "+tempDataHolder.getField(i));
            }

        }
        list.setBackgroundColor(activity.getResources().getColor(R.color.white));
        return list;

    }


}
