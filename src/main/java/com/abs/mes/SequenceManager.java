package com.abs.mes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ConcurrentSkipListMap;

import com.abs.mes.util.JsonUtil;


public final class SequenceManager {

    private String sourceSystem;
    private String site;
    private String env;
    private String filPath;
        
    private String topicHeader = "SVM/DEV/";	// or "SVM/PROD/"
    

    private ConcurrentSkipListMap<String, Object> inputData;
    
    /**
     * iniitialized Input Data in Memory - From Database resultSet
     **/
    public SequenceManager(String sourceSystem, String site, String env, ConcurrentSkipListMap<String, Object> inputData){
        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.inputData = inputData;                
    }

    /**
     * iniitialized Input Data in Memory - From File Data
     * @throws IOException 
     **/
    public SequenceManager(String sourceSystem, String site, String env, String filePath) throws IOException{

        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.filPath = filePath;
        
        // setting inputData
        
        // read file .json file
        inputDataInit();
        
    }
    
    // Data initialized
    public void inputDataInit() throws IOException {
    	InputStream inputStream = SequenceManager.class.getResourceAsStream(filPath);
		Reader reader = new InputStreamReader(inputStream);
		this.inputData = new ConcurrentSkipListMap<>();
		inputData.putAll(JsonUtil.readJson(reader.toString()));
    }

    private String getThreeDepth(String targetSystem, String eventName, String payload) {
    	return "";
    }
    
    private String getDetailDestination() {
    	return "";
    }

    public String getTargetName(String targetSystem, String eventName, String payload){
    	String topic = "";
    	
    	topic = getThreeDepth(targetSystem, eventName, payload);
    	
        return site + "/" + env + "/" + targetSystem + "/CMN/00";
    }
    
    /**
     * only using BRA
     **/
    public String getTargetName(String payload){    	
    	return getTargetName(payload, payload, payload);
    }

    
    public static void main(String[] args) {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property
        ConcurrentSkipListMap<String, Object> queryResult = null; // Select Query Result.

        SequenceManager seqLib = new SequenceManager(sourceSystem, site, env, queryResult);
        System.out.println(
                seqLib.getTargetName("WFS", "eventName", "payload")
        );
    }

}
