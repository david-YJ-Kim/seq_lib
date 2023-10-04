package com.abs.mes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SequenceManager {


    public static void main(String[] args) {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property
        List<Map> queryResult = null; // Select Query Result.

        SequenceManager seqLib = new SequenceManager(sourceSystem, site, env, queryResult);
        System.out.println(
                seqLib.getTargetName("WFS", "eventName", "payload")
        );
    }

    private String sourceSystem;
    private String site;
    private String env;
    private List<Map> queryResult;
    private String filePath;
    private String fileName;

    public SequenceManager(String sourceSystem, String site, String env, String filePath, String fileName){
        this(sourceSystem, site, env, filePath, fileName, null);
    }


    public SequenceManager(String sourceSystem, String site, String env, List<Map> queryResult){

        this(sourceSystem, site, env, null, null, queryResult);

        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.queryResult = queryResult;
    }

    public SequenceManager(String sourceSystem, String site, String env, String filePath, String fileName, List<Map> queryResult){

        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.filePath = filePath;
        this.fileName = fileName;
        this.queryResult = queryResult;

    }


    public SequenceManager(String sourceSystem, String site, String env, String filePath){

        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.filPath = filePath;
    }



    public String getTargetName(String targetSystem, String eventName, String payload){
        return site + "/" + env + "/" + targetSystem + "/CMN/00";
    }


}
