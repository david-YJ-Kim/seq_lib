package com.abs.cmn.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.abs.cmn.seq.util.SequenceManageUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abs.cmn.seq.checker.EventRuleChecker;
import com.abs.cmn.seq.checker.ParsingRuleChecker;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.code.SystemNameList;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import com.abs.cmn.seq.executor.SequenceRuleExecutor;

//import com.abs.mes.util.JsonUtil;

public final class SequenceManager {
	private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

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
    
    private String sourceSystem;

    private String site;

    private String env;

    // TODO 큐 개수 설정은 각 시스템에서 설정할 게 아니라, 감춰야 함.
    private int queueCount;

    private String topicHeader;
    private String ruleFilePath;
    private String ruleFileName;


    private final ParsingRuleChecker parsingRuleChecker;
    private final EventRuleChecker eventRuleChecker;

    private final SequenceRuleExecutor ruleExecutor;


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
        this.ruleExecutor = new SequenceRuleExecutor(this.queueCount);
        this.parsingRuleChecker = new ParsingRuleChecker(sourceSystem, ruleFilePath, ruleFileName,
                                                        ruleDataObj.getJSONObject(SeqCommonCode.ParsingItemRule.name()));

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
    	String topicName;
        String topicVal;


        logger.info("@@ -- params : payload : "+ payload);

        switch (targetSystem){
            
            case SystemNameList.EAP:
            	topicVal = getTopicNameForEAP(this.sourceSystem, targetSystem, eventName, payload);
            	break;
            case SystemNameList.MCS:
            case SystemNameList.FDC:
            case SystemNameList.SPC:
            case SystemNameList.RMS:
            case SystemNameList.RTD:
            case SystemNameList.MSS:
            case SystemNameList.CRS:
                topicVal = targetSystem + SequenceManageUtil.getCommonDefaultTopic();
                break;

            case SystemNameList.WFS:
            case SystemNameList.BRS:
                topicVal = targetSystem + this.getTopicNameForMOS(targetSystem, eventName, payload);
                break;
            default:
                // TODO targetSystem이 없는 경우는..?
                topicVal = "ERR" + SequenceManageUtil.getCommonDefaultTopic();
                break;

        }

        topicName = topicHeader.concat(topicVal);
        return topicName;

    }


    private String getTopicNameForMOS(String targetSystem, String eventName, String payload){
        String ruleResult = "/";

        // 1. EventRuleChecker
        SequenceRuleDto sequenceRuleDto = this.eventRuleChecker.getEventRule(targetSystem, eventName);
        if(sequenceRuleDto != null){      	

        	if ( sequenceRuleDto.getTarget().compareTo(targetSystem) < 1) {
        		ruleResult += this.ruleExecutor.executeEventRule(targetSystem, eventName, new JSONObject(payload), sequenceRuleDto);
        		logger.info("## 1. excuteEventRule wieh targetSystem");
        	} else {
        		ruleResult += this.ruleExecutor.executeEventRule(sequenceRuleDto.getTarget(), eventName, new JSONObject(payload), sequenceRuleDto);
        		logger.info("## 2. excuteEventRule wieh sequenceRuleDto.target");
        	}

        }else{
            // 2. if Event Rule Checker return null
            // 3. ParsingRule Checker.
            ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
            if(!ruleDtoArrayList.isEmpty()){
                ruleResult += this.ruleExecutor.executeParsingRule(targetSystem, eventName, new JSONObject(payload),
                        ruleDtoArrayList);
                logger.info("## 3. executeParsingRule with ruleDtoArrayList");
            } else {
            	ruleResult += targetSystem.concat(SequenceManageUtil.getCommonDefaultTopic());
            	logger.info("## 4. executeParsingRule without ruleDtoArrayList");
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

        return "/" + this.ruleExecutor.basicSequenceRule();

    }

    
    private String getTopicNameForEAP(String sourceSystem, String targetSystem, String eventName, String payload) {
    	
		return targetSystem+"/"+ruleExecutor.executeEAPParsingRule(targetSystem, new JSONObject(payload));

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
