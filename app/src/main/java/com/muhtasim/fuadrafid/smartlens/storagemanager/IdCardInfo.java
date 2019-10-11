package com.muhtasim.fuadrafid.smartlens.storagemanager;

/**
 * Created by Fuad Rafid on 9/4/2017.
 */

public class IdCardInfo {
    String IdNo,fields[];

    public IdCardInfo()
    {
        fields=new String[10];
        for(int i=0;i<10;i++)
        {
            fields[i]="";
        }
    }


    public String getIdNo() {
        return IdNo;
    }

    public String getField(int pos) {
        return fields[pos];
    }
}
