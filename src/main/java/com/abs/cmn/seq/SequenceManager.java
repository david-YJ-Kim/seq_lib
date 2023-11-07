package com.abs.cmn.seq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.util.file.RuleFileWatcher;
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
        String topicVal;

        logger.info("@@ -- params : payload : "+ payload);
        // 1. EventRuleChecker
        String checkEventRuleResult = this.checkEventRule(targetSystem, eventName, payload);
        if(checkEventRuleResult != null){
             // EAP +
            topicVal = targetSystem + checkEventRuleResult;

        }else{

            switch (targetSystem){

                case SystemNameList.EAP:
                    topicVal = targetSystem + getTopicNameForEAP(this.sourceSystem, targetSystem, eventName, payload);
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
        }

        topicName = topicHeader.concat(topicVal);

        try{
            Objects.requireNonNull(topicVal);
            return topicName;

        }catch (NullPointerException e){
            e.printStackTrace();
            logger.error(e.toString());
            return topicHeader + "/" + this.ruleExecutor.basicSequenceRule();
        }


    }

    private String checkEventRule(String targetSystem, String eventName, String payload){
        String ruleResult = null;

        // 1. EventRuleChecker
        SequenceRuleDto sequenceRuleDto = this.eventRuleChecker.getEventRule(targetSystem, eventName);
        if(sequenceRuleDto != null){

            // 룰에 등록된 Target과 요청 받은 Target이 동일한지 확인
            // 서로 다를 경우, 룰에 등록된 타켓 정보를 우선으로 분배
            if ( sequenceRuleDto.getTarget().compareTo(targetSystem) < 1) {
                ruleResult = this.ruleExecutor.executeEventRule(targetSystem, eventName, new JSONObject(payload), sequenceRuleDto);
                logger.info("## 1. executeEventRule with targetSystem");

            } else {
                ruleResult = this.ruleExecutor.executeEventRule(sequenceRuleDto.getTarget(), eventName, new JSONObject(payload), sequenceRuleDto);
                logger.info("## 2. executeEventRule with sequenceRuleDto.target");

            }

        }

        // $큐타입/$큐 키
        return ruleResult;
    }

    private String getTopicNameForEAP(String sourceSystem, String targetSystem, String eventName, String payload) {

        return "/" + ruleExecutor.executeEAPParsingRule(new JSONObject(payload));

    }

    private String getTopicNameForMOS(String targetSystem, String eventName, String payload){

        String ruleResult = null;

        logger.info("Event Rule is not registered.");
        // 3. ParsingRule Checker.
        ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
        if(!ruleDtoArrayList.isEmpty()){
            ruleResult = this.ruleExecutor.executeParsingRule(targetSystem, eventName, new JSONObject(payload),
                    ruleDtoArrayList);
            logger.info("## 3. executeParsingRule with ruleDtoArrayList");
        } else {
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
        
        this.eventRuleChecker.sequenceDataBackUp();
        this.eventRuleChecker.sequenceDataReload(reloadedRuleFileObj.getJSONObject(SeqCommonCode.EventNameRule.name()));
        
        // TODO Rule File 갱신 파일 생성
    };

    


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
