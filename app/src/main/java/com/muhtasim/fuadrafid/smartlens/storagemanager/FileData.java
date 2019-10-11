package com.muhtasim.fuadrafid.smartlens.storagemanager;

/**
 * Created by Fuad Rafid on 8/20/2017.
 */

public class FileData {
    String name;
    String fileData;
    public  FileData(){}
    public  FileData(String name, String fileData)
    {
        this.fileData=fileData;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }
}
