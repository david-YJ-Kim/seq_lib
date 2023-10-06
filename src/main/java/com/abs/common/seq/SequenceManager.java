package com.abs.common.seq;

import com.abs.common.seq.checker.EventRuleChecker;
import com.abs.common.seq.checker.ParsingRuleChecker;
import com.abs.common.seq.code.SystemNameList;
import com.abs.common.seq.dto.SequenceRuleDto;
import com.abs.common.seq.executor.SequenceRuleExecutor;
import com.abs.common.seq.util.SequenceManageUtil;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

//import com.abs.mes.util.JsonUtil;


public final class SequenceManager {

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String eventRuleFileName = "EventNameRule.json";
        String parsingRuleFileName = "ParsingItemRule.json";

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
    private String topicHeader; // SVM/DEV/
    private String ruleFilesPath;

    private String eventRuleFileName;
    private String parsingRuleFileName;


    private final ParsingRuleChecker parsingRuleChecker;
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
        this.parsingRuleChecker = new ParsingRuleChecker(sourceSystem, ruleFilesPath, parsingRuleFileName,
                SequenceManageUtil.readFile(ruleFilesPath.concat(parsingRuleFileName)));


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
        String topicVal;

        switch (targetSystem){
            case SystemNameList.MCS:
            case SystemNameList.FDC:
            case SystemNameList.SPC:
            case SystemNameList.EAP:
            case SystemNameList.RMS:
            case SystemNameList.RTD:
            case SystemNameList.MSS:
            case SystemNameList.CRS:
                topicVal = targetSystem + this.getCommonTopic("00");
                break;

            case SystemNameList.WFS:
            case SystemNameList.BRS:
                topicVal = targetSystem + "/" + this.getTopicNameForMOS(targetSystem, eventName, payload);
                break;
            default:
                topicVal =  "";
                break;

        }

        topicName = topicHeader.concat(topicVal);
        return topicName;

    }


    private String getTopicNameForMOS(String targetSystem, String eventName, String payload){
        String ruleResult = null;

        // 1. EventRuleChecker
        SequenceRuleDto sequenceRuleDto = this.eventRuleChecker.getEventRule(targetSystem, eventName);
        if(sequenceRuleDto != null){

            ruleResult = this.ruleExecutor.executeEventRule(targetSystem, eventName,
                    new JSONObject(payload), sequenceRuleDto);

        }else{
            // 2. if Event Rule Checker return null
            // 3. ParsingRule Checker.
            ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
            if(!ruleDtoArrayList.isEmpty()){
                ruleResult = this.ruleExecutor.executeParsingRule(targetSystem, eventName, new JSONObject(payload),
                        ruleDtoArrayList);
            }
        }

        try{
            Objects.requireNonNull(ruleResult);
            return ruleResult;

        }catch (NullPointerException e){
            e.printStackTrace();
            System.err.println(
                    e
            );
        }

        return this.ruleExecutor.basicSequenceRule();

    }

    private String getCommonTopic(String seq){
        return "/" + "CMN" + "/" +  seq;
    }
    

    




}
