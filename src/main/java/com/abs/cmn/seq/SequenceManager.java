package com.abs.cmn.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abs.cmn.seq.checker.EventRuleChecker;
import com.abs.cmn.seq.checker.ParsingRuleChecker;
import com.abs.cmn.seq.code.PayloadCommonCode;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.code.SystemNameList;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import com.abs.cmn.seq.executor.SequenceRuleExecutor;
import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.util.file.RuleFileWatcher;

public final class SequenceManager {
	private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule.json";

        SequenceManager sequenceManager = new SequenceManager(
                sourceSystem,
                site,
                env,
                ruleFilesPath,
                sequenceRuleFileName
        );

    }
    
    private String sourceSystem;

    private String site;

    private String env;

    private int queueCount;

    private String topicHeader;
    private String ruleFilePath;
    private String ruleFileName;


    private final ParsingRuleChecker parsingRuleChecker;
    private final EventRuleChecker eventRuleChecker;
    private final SequenceRuleExecutor ruleExecutor;

    private final Thread ruleFileWatcherThread;


    /**
     * 시퀀스 라이브러리 초기화를 위한 기본 생성자
     * @param sourceSystem
     * @param site
     * @param env
     * @param ruleFilePath
     * @param ruleFileName
     * @throws IOException
     */
    public SequenceManager(String sourceSystem, String site, String env,
                            String ruleFilePath, String ruleFileName) throws IOException {

        logger.info("SequenceManager start to initialize. Parameter for constructor." +
                " sourceSystem: {}, site: {}, env: {}," +
                " ruleFilePath: {}. ruleFileName: {}",
                sourceSystem, site, env, ruleFilePath, ruleFileName);


        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.ruleFilePath = ruleFilePath;
        this.ruleFileName = ruleFileName;

        JSONObject ruleDataObj = new JSONObject(SequenceManageUtil.readFile(ruleFilePath.concat(ruleFileName)));

        this.queueCount = ruleDataObj.getInt(SeqCommonCode.QueueCount.name());
        this.eventRuleChecker = new EventRuleChecker(ruleFilePath, ruleFileName,
                                                     ruleDataObj.getJSONObject(SeqCommonCode.EventNameRule.name()));
        this.parsingRuleChecker = new ParsingRuleChecker(sourceSystem, ruleFilePath, ruleFileName,
                                                        ruleDataObj.getJSONObject(SeqCommonCode.ParsingItemRule.name()));
        this.ruleExecutor = new SequenceRuleExecutor(this.queueCount);

        this.ruleFileWatcherThread = this.initializeFileWatcher();
        this.ruleFileWatcherThread.start();


        logger.info("SequenceManager has been initialized. Print SequenceManager clas information." + System.lineSeparator()
                + "{}", this.toString());


    }

    /**
     *
     * @param targetSystem
     * @param eventName
     * @param payload
     * @return
     */
    public String getTargetName(String targetSystem, String eventName, String payload){
        // topic Header = SVM/DEV/
    	String topicName;
    	String topicVal = null;
    	
    	logger.info("@@ -getTargetName()- params : targetSystem : "+ targetSystem);
    	logger.info("@@ -getTargetName()- params : eventName : "+ eventName);
    	logger.info("@@ -getTargetName()- params : payload : "+ payload);
    	
    	/**
    	 * Validation if payload , targetSystem , eventName
    	 **/ 
    	if ( payload == null ) {  // SVM/DEV//CMN/00
            // TODO payload와 target system 두 개 다 null 이라면? >> 대응 필요
            return this.topicHeader + targetSystem + SequenceManageUtil.getCommonDefaultTopic(); //+ CMN 맞춰서 잘못 된 Topic 으로 Return
            
        }else if (targetSystem == null || targetSystem.length() == 0){

            targetSystem = getTargetNameFromHeader(new JSONObject(payload));

        }else if (eventName == null || eventName.length() == 0 ) {

            eventName = getMessageNameFromHeader(new JSONObject(payload));
        }
        
        logger.info("## @@@ -- targetSystem : "+targetSystem);
        
        // 1. EventRuleChecker
        String checkEventRuleResult = this.checkEventRule(targetSystem, eventName, payload);
        logger.info("** checkEventRuleResult: {}", checkEventRuleResult);
        
        /**
         * if 이벤트 룰 파싱 결과가 있는 경우 해당 토픽 리턴,
         * else 파싱룰 : EAP 만 별도 파싱 룰, 나머지는 모두 동일한 파싱룰 진행 
         **/
        if(checkEventRuleResult != null){
        	topicVal = checkEventRuleResult;
        	logger.info("1-1 ---------------------- checkEventRuleResult : "+ topicVal);
        }else{
        	logger.info("2 ---------------------- checkEventRuleResult : "+ checkEventRuleResult);

            switch (targetSystem){

                case SystemNameList.EAP:
                    topicVal = targetSystem + getTopicNameForEAP(this.sourceSystem, targetSystem, eventName, payload);
                    break;

                default:
                    topicVal = targetSystem + this.getTopicNameForParsingRule(targetSystem, eventName, payload);
                    break;

            }
        }

        topicName = topicHeader.concat(topicVal);
        logger.info("Final topic name: {}", topicName);

        try{
            Objects.requireNonNull(topicVal);
            return topicName;

        }catch (NullPointerException e){
            e.printStackTrace();
            logger.error(e.toString());
            return topicHeader + SequenceManageUtil.getCommonDefaultTopic();
        }


    }

    private String checkEventRule(String targetSystem, String eventName, String payload){
        String ruleResultFormat = "%s/%s";
        String resultTargetSystem;
        String resultTargetValue;

        // 1. EventRuleChecker
        SequenceRuleDto sequenceRuleDto = this.eventRuleChecker.getEventRule(targetSystem, eventName);

        if(sequenceRuleDto == null){
            return null;

        }else {

            logger.info("Sequence Rule has been registered. details: {}", sequenceRuleDto.toString());

            resultTargetSystem = (targetSystem == null) ? sequenceRuleDto.getTarget() : targetSystem;
            resultTargetValue = this.ruleExecutor.executeEventRule(resultTargetSystem, eventName, new JSONObject(payload), sequenceRuleDto);

            String returnVal = String.format(ruleResultFormat, resultTargetSystem, resultTargetValue);
            logger.info("Sequence Rule result: {}", returnVal);
            return returnVal;


        }
        
    }

    private String getTopicNameForEAP(String sourceSystem, String targetSystem, String eventName, String payload) {

        return "/" + ruleExecutor.executeEAPParsingRule(new JSONObject(payload));

    }

    private String getTopicNameForParsingRule(String targetSystem, String eventName, String payload){

        String ruleResult = null;

        logger.info("Event Rule is not registered.");
        // 3. ParsingRule Checker.
        ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
        if( ruleDtoArrayList != null ){
            // 메모리에 기준 정보 등록된 경우 >< MOS 계열  BRS, WFS
            ruleResult = this.ruleExecutor.executeParsingRule(targetSystem, eventName, new JSONObject(payload),
                    ruleDtoArrayList);
            logger.info("## 3. executeParsingRule with ruleDtoArrayList");


        } else {
            // 메모리에 기준 정보가 없는 경우  >> SPC, RMS, FDC, MCS, MSS, RTD 등
            ruleResult = targetSystem.concat(SequenceManageUtil.getCommonDefaultTopic());
            logger.info("## 4. executeParsingRule without ruleDtoArrayList");
        }

        return "/" + ruleResult;

    }


    private Thread initializeFileWatcher(){
        Thread watcherThread = new Thread(new RuleFileWatcher(this, this.ruleFilePath, this.ruleFileName));
        return watcherThread;
    }

    public void fileChangeDetecting(String fileName, String fileContent){
        logger.info("File change event detected. fileName: {}, fileContent: {}", fileName, fileContent);

        JSONObject reloadedRuleFileObj = new JSONObject(fileContent);
        // TODO Rule File 검증 프로세스 호출

        // ERC
        this.eventRuleChecker.sequenceDataBackUp();
        this.eventRuleChecker.sequenceDataReload(reloadedRuleFileObj.getJSONObject(SeqCommonCode.EventNameRule.name()));

        // PRC
        this.parsingRuleChecker.sequenceDataBackUp();
        this.parsingRuleChecker.sequenceDataReload(reloadedRuleFileObj.getJSONObject(SeqCommonCode.ParsingItemRule.name()));
        
        // TODO Rule File 갱신 파일 생성
    };

    private String getTargetNameFromHeader(JSONObject payload) {
    	logger.info(":: getTargetNameFromHeader - header : "+payload.getJSONObject( PayloadCommonCode.head.name()));
    	JSONObject header;
    	if ( payload.length() != 0 ) {
    		header = payload.getJSONObject( PayloadCommonCode.head.name());
    		logger.info("-- getTargetNameFromHeader :: get Target : "+header.getString(PayloadCommonCode.tgt.name()));
    		return header.getString(PayloadCommonCode.tgt.name());
    	} else { 
    		return "";
    	}
    	
    }
    
    private String getMessageNameFromHeader(JSONObject payload) {
    	logger.info(":: getMessageNameFromHeader - header : "+payload.getJSONObject( PayloadCommonCode.head.name()));
    	JSONObject header ;
    	if ( payload.length() != 0 ) {
	    	header = payload.getJSONObject( PayloadCommonCode.head.name());
	    	logger.info("getMessageNameFromHeader :: get MessageName : "+header.getString(PayloadCommonCode.cid.name()));
	    	return header.getString(PayloadCommonCode.cid.name());
    	} else 
    		return "";
    }


    @Override
    public String toString() {
        return "SequenceManager{" +
                "sourceSystem='" + sourceSystem + '\'' +
                ", site='" + site + '\'' +
                ", env='" + env + '\'' +
                ", queueCount=" + queueCount +
                ", topicHeader='" + topicHeader + '\'' +
                ", ruleFilePath='" + ruleFilePath + '\'' +
                ", ruleFileName='" + ruleFileName + '\'' +
                ", parsingRuleChecker=" + parsingRuleChecker +
                ", eventRuleChecker=" + eventRuleChecker +
                ", ruleExecutor=" + ruleExecutor +
                '}';
    }
}
