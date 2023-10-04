package com.abs.common.seq;

import com.abs.common.seq.checker.EventRuleChecker;
import com.abs.common.seq.dto.SequenceRuleDto;
import com.abs.common.seq.executor.SequenceRuleExecutor;
import com.abs.common.seq.util.SequenceManageUtil;

import java.io.*;
import java.util.concurrent.ConcurrentSkipListMap;

//import com.abs.mes.util.JsonUtil;


public final class SequenceManager {

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String eventRuleFileName = "EventNameRule.json";
        String parsingRuleFileName = "EventNameRule.json";

        SequenceManager sequenceManager = new SequenceManager(
                sourceSystem,
                site,
                env,
                ruleFilesPath,
                eventRuleFileName,
                parsingRuleFileName
        );


    }

    private String sourceSystem;
    private String site;
    private String env;
    private String topicHeader;
    private String ruleFilesPath;

    private String eventRuleFileName;
    private String parsingRuleFileName;


//    private final AnotherChecker;
    private final EventRuleChecker eventRuleChecker;

    private final SequenceRuleExecutor ruleExecutor;


    private ConcurrentSkipListMap<String, Object> inputData;



    /**
     * iniitialized Input Data in Memory - From File Data
     * @throws IOException 
     **/
    public SequenceManager(String sourceSystem, String site, String env, String ruleFilePath) throws IOException {

        this(sourceSystem, site, env, ruleFilePath, null, null);
    }

    public SequenceManager(String sourceSystem, String site, String env, String ruleFilesPath, String eventRuleFileName, String parsingRuleFileName) throws IOException {


        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.ruleFilesPath = ruleFilesPath;
        this.eventRuleFileName = eventRuleFileName;
        this.parsingRuleFileName = parsingRuleFileName;

        this.eventRuleChecker = new EventRuleChecker(ruleFilesPath, eventRuleFileName,
                SequenceManageUtil.readFile(ruleFilesPath.concat(eventRuleFileName)));
        this.ruleExecutor = new SequenceRuleExecutor();


    }


    // Data initialized
    public void getRuleData(String filePath, String fileName) throws IOException {


    }

    private String getThreeDepth(String targetSystem, String eventName, String payload) {
    	return "";
    }
    
    private String getDetailDestination() {
    	return "";
    }

    public String getTargetName(String targetSystem, String eventName, String payload){
    	String topicName;
        SequenceRuleDto sequenceRuleDto;

        // 1. EventRuleChecker
        sequenceRuleDto = this.eventRuleChecker.getEventRule(targetSystem, eventName);
        if(sequenceRuleDto != null){

            String ruleResult = this.ruleExecutor.executeEventRule(targetSystem, eventName, payload, sequenceRuleDto);
            return topicHeader.concat(ruleResult);
        }
        // 2. if Event Rule Checker return null
        // 3. ParsingRule Checker.
    	
//    	topic = getThreeDepth(targetSystem, eventName, payload);
    	
        return site + "/" + env + "/" + targetSystem + "/CMN/00";
    }
    
    /**
     * only using BRA
     **/
    public String getTargetName(String payload){    	
    	return getTargetName(payload, payload, payload);
    }

    




}
