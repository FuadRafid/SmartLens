package com.muhtasim.fuadrafid.smartlens.others;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Fuad Rafid on 9/17/2017.
 */

public class IdData{
    private ArrayList<String> fieldNames;
    private ArrayList<String> fields;
    private int Id;
    public IdData()
    {
        fieldNames=new ArrayList<String>();
        fields=new ArrayList<String>();
    }

    public void addFieldName(String fieldName) {
       fieldNames.add(fieldName);
    }

    public void addField(String field) {
        fields.add(field);
    }
    public String getFieldName(int pos)
    {
        return fieldNames.get(pos);
    }
    public String getField(int pos)
    {
        return fields.get(pos);
    }



    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }
}
