package com.abs.common.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.abs.common.seq.checker.EventRuleChecker;
import com.abs.common.seq.checker.ParsingRuleChecker;
import com.abs.common.seq.code.SeqCommonCode;
import com.abs.common.seq.code.SystemNameList;
import com.abs.common.seq.dto.SequenceRuleDto;
import com.abs.common.seq.executor.SequenceRuleExecutor;
import com.abs.common.seq.util.SequenceManageUtil;

//import com.abs.mes.util.JsonUtil;

@ConfigurationProperties(prefix="application.topic-header")
public final class SequenceManager {
	private static final Logger log = LoggerFactory.getLogger(SequenceManager.class);

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

//        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLib_dv\\src\\main\\resources";
        String sequenceRuleFileName = "SequenceRule.json";

        SequenceManager sequenceManager = new SequenceManager(
                sourceSystem,
                site,
                env,
                ruleFilesPath,
                sequenceRuleFileName
        );

    }
    
    @Value("${source-system")
    private String sourceSystem;
    
    @Value("${site}")
    private String site;
    
    @Value("${env}")
    private String env;
    
    private String topicHeader;
    private String ruleFilePath;
    private String ruleFileName;


    private final ParsingRuleChecker parsingRuleChecker;
    private final EventRuleChecker eventRuleChecker;

    private final SequenceRuleExecutor ruleExecutor;

    /**
     * iniitialized Input Data in Memory - From File Data
     * @throws IOException 
     **/
    public SequenceManager(String sourceSystem, String site, String env, String ruleFilePath) throws IOException {

        this(sourceSystem, site, env, ruleFilePath, null);
    }

    public SequenceManager(String sourceSystem, String site, String env, String ruleFilePath, String ruleFileName) throws IOException {


        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.ruleFilePath = ruleFilePath;
        this.ruleFileName = ruleFileName;

        String ruleData = SequenceManageUtil.readFile(ruleFilePath.concat(ruleFileName));
        JSONObject ruleDataObj = new JSONObject(ruleData);

        this.eventRuleChecker = new EventRuleChecker(ruleFilePath, ruleFileName,
                ruleDataObj.getJSONObject(SeqCommonCode.EventNameRule.name()));
        this.ruleExecutor = new SequenceRuleExecutor();
        this.parsingRuleChecker = new ParsingRuleChecker(sourceSystem, ruleFilePath, ruleFileName,
                ruleDataObj.getJSONObject(SeqCommonCode.ParsingItemRule.name()));


    }


    // Data initialized
    public void getRuleData(String filePath, String fileName) throws IOException {


    }

    public String getTargetName(String targetSystem, String eventName, String payload){
    	String topicName;
        String topicVal;
        
        log.info("@@ -- params : target : "+ targetSystem );
        log.info("@@ -- params : eventName : "+ eventName);
        log.info("@@ -- params : payload : "+ payload);

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
                topicVal = "CMN/00";
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

        	if ( sequenceRuleDto.getTarget().compareTo(targetSystem) < 1) {
        		ruleResult = this.ruleExecutor.executeEventRule(targetSystem, eventName, new JSONObject(payload), sequenceRuleDto);
        		log.info("## 1. excuteEventRule wieh targetSystem");
        	} else {
        		ruleResult = this.ruleExecutor.executeEventRule(sequenceRuleDto.getTarget(), eventName, new JSONObject(payload), sequenceRuleDto);
        		log.info("## 2. excuteEventRule wieh sequenceRuleDto.target");
        	}

        }else{
            // 2. if Event Rule Checker return null
            // 3. ParsingRule Checker.
            ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
            if(!ruleDtoArrayList.isEmpty()){
                ruleResult = this.ruleExecutor.executeParsingRule(targetSystem, eventName, new JSONObject(payload),
                        ruleDtoArrayList);
                log.info("## 3. executeParsingRule with ruleDtoArrayList");
            } else {
            	ruleResult = targetSystem.concat(getCommonTopic("00"));
            	log.info("## 4. executeParsingRule without ruleDtoArrayList");
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
