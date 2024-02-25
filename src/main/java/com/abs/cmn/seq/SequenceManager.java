package com.abs.cmn.seq;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

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
    private String ruleVersion;
    private boolean fileWatchFlag;

    private String topicHeader;
    private String ruleFilePath;
    private String ruleFileName;


    private final ParsingRuleChecker parsingRuleChecker;
    private final EventRuleChecker eventRuleChecker;
    private final SequenceRuleExecutor ruleExecutor;

    private Thread ruleFileWatcherThread;


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


        String filePath = "version.json"; // resources 폴더 내의 버전 파일 경로

        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        JSONObject versionObject = new JSONObject(SequenceManageUtil.convertToString(is));

        logger.info("SequenceManager start to initialize. Version: {}, Parameter for constructor."
                        + "sourceSystem: {}, site: {}, env: {}, ruleFilePath: {}, ruleFileName: {}."
                ,versionObject.getString("version"), sourceSystem, site, env, ruleFilePath, ruleFileName
        );


        this.sourceSystem = sourceSystem;
        this.site = site;
        this.env = env;
        this.topicHeader = site+"/"+env+"/";
        this.ruleFilePath = ruleFilePath;
        this.ruleFileName = ruleFileName;

        JSONObject ruleDataObj = null;
        try{

            ruleDataObj = new JSONObject(SequenceManageUtil.readFile(ruleFilePath.concat(ruleFileName)));
        }catch (Exception e){
            e.printStackTrace();
            logger.error("{}" +
                            "ruleFilePath: {}, ruleFileName: {}, Error: {}"
                    ,"Fail to read rule file."
                    ,ruleFilePath, ruleFileName, e.getMessage()
            );
        }

        this.ruleVersion = ruleDataObj.getString(SeqCommonCode.Version.name());
        this.fileWatchFlag = ruleDataObj.getBoolean(SeqCommonCode.FileWatchFlag.name());
        this.queueCount = ruleDataObj.getInt(SeqCommonCode.QueueCount.name());
        this.eventRuleChecker = new EventRuleChecker(ruleFilePath, ruleFileName,
                ruleDataObj.getJSONObject(SeqCommonCode.EventNameRule.name()));
        this.parsingRuleChecker = new ParsingRuleChecker(sourceSystem, ruleFilePath, ruleFileName,
                ruleDataObj.getJSONObject(SeqCommonCode.ParsingItemRule.name()));
        this.ruleExecutor = new SequenceRuleExecutor(this.queueCount);

        if(this.fileWatchFlag){
            this.ruleFileWatcherThread = this.initializeFileWatcher();
            this.ruleFileWatcherThread.start();
            logger.info("{}"
                    ,"File watch service is now on."
            );
        }


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

        String key = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        logger.debug("{}: {}" +
                        "targetSystem: {}, eventName: {}, payload: {}."
                ,key ,"Print all parameters."
                , targetSystem, eventName, payload
        );

        // topic Header = SVM/DEV/
        String topicName;
        String topicVal = null;

        /**
         * A. Validation about each parameter.
         * if something is null, find in given parameter.
         **/
        // if payload is null
        // > then return with common topic.
        if ( payload == null ) {


            // what if payload and target is null
            // > then Get target system from event name. and return Common topic
            if(targetSystem == null || targetSystem.length() == 0){
                targetSystem = SequenceManageUtil.getTargetSystem(eventName);
            }
            logger.warn("{}: {}" +
                            "targetSystem: {}, eventNvame: {}, payload: {}."
                    , key ,"Mandatory parameter is null. so return common topic. please fill out all the parameters."
                    , targetSystem, eventName, payload
            );
            return this.topicHeader + targetSystem + SequenceManageUtil.getCommonDefaultTopic(key); //+ CMN 맞춰서 잘못 된 Topic 으로 Return


            // if targetSystem is null
            // > then find in payload
        }else if (targetSystem == null || targetSystem.length() == 0){

            targetSystem = SequenceManageUtil.getTargetNameFromHeader(key, new JSONObject(payload));

            // if eventName is null
            // > then find in payload
        }else if (eventName == null || eventName.length() == 0 ) {

            eventName = SequenceManageUtil.getMessageNameFromHeader(key, new JSONObject(payload));
        }

        logger.info("{}: {}" +
                        "targetSystem: {}, eventName: {}, payload: {}."
                , key ,"Print parameters after validation."
                , targetSystem, eventName, payload
        );


        /**
         * B. Verify if the event rule has been registered..
         */
        // 1. EventRuleChecker
        String checkEventRuleResult = this.checkEventRule(key, targetSystem, eventName, payload);
        logger.info("{}: {}" +
                        "checkEventRuleResult: {}."
                , key ,"Result of verification of event rule."
                , checkEventRuleResult
        );

        if(checkEventRuleResult != null){
            topicVal = checkEventRuleResult;
            logger.info("{}: {}" +
                            "topicVal: {}, checkEventRuleResult: {}."
                    , key ,"Result of event rule."
                    , topicVal, checkEventRuleResult
            );

            /**
             * C. Basic topic generate rule based on its target system.
             */
        }else{
            switch (targetSystem){

                case SystemNameList.EAP:
                    topicVal = targetSystem + getTopicNameForEAP(key, this.sourceSystem, targetSystem, eventName, payload);
                    break;

                default:
                    topicVal = targetSystem + this.getTopicNameForParsingRule(key, targetSystem, eventName, payload);
                    break;

            }
        }

        topicName = topicHeader.concat(topicVal);
        logger.info("{}: {}" +
                        "topicName: {}, topicHeader: {}, topicVal: {}, targetSystem: {}."
                ,key ,"Result of basic topic generating rule."
                , topicName, topicHeader, topicVal, targetSystem
        );


        try{
            Objects.requireNonNull(topicVal);
            return topicName;

        }catch (NullPointerException e){
            e.printStackTrace();
            logger.error(e.toString());
            return topicHeader + SequenceManageUtil.getCommonDefaultTopic(key);
        }


    }

    /**
     *
     * @param targetSystem
     * @param eventName
     * @param payload
     * @return
     */
    private String checkEventRule(String key, String targetSystem, String eventName, String payload){
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

    private String getTopicNameForEAP(String key, String sourceSystem, String targetSystem, String eventName, String payload) {

        return "/" + ruleExecutor.executeEAPParsingRule(key, new JSONObject(payload));

    }

    /**
     * C. Basic topic generation rul > B.Others
     * @param targetSystem
     * @param eventName
     * @param payload
     * @return
     */
    private String getTopicNameForParsingRule(String key, String targetSystem, String eventName, String payload){

        String ruleResult = null;

        ArrayList<SequenceRuleDto> ruleDtoArrayList = this.parsingRuleChecker.getParsingRule(targetSystem);
//        logger.info("{}: {}" +
//                        "ruleDtoArrayList_size: {}."
//                ,key ,"Get basic topic generate rule a.k.a parsing rule."
//                , ruleDtoArrayList.size()
//        );

        /**
         * A. Undefined System
         * ex) SPC, FDC, MCS, OIA, FIS etc...
         */
        if(ruleDtoArrayList == null || ruleDtoArrayList.size() == 0){

//            String allElementsAsString = ruleDtoArrayList.stream()
//                    .map(Object::toString) // Assuming SequenceRuleDto has overridden toString() method
//                    .reduce((result, element) -> result + ", " + element) // Concatenate all elements
//                    .orElse(""); // Handle the case when the list is empty
//
//            logger.warn("{}: {}" +
//                            "targetSystem: {}, ruleDtoArrayList: {}."
//                    ,key ,"System is not registered in parsing rule."
//                    , targetSystem, allElementsAsString
//            );

            ruleResult = SequenceManageUtil.getCommonDefaultTopic(key);

            logger.warn("{}: {}" +
                            "targetSystem: {}, ruleResult: {}."
                    ,key ,"Undefined system will be returned in common topic."
                    , targetSystem, ruleResult
            );


            /**
             * B. Defined System
             * ex) BRS, WFS, RMS, etc...
             */
        }else{

            ruleResult = this.ruleExecutor.executeParsingRule(key, targetSystem, eventName, new JSONObject(payload),
                    ruleDtoArrayList);
            logger.info("{}: {}" +
                            "targetSystem: {}, ruleResult: {}."
                    ,key ,"Topic will be returned according to parsing rule."
                    , targetSystem, ruleResult
            );
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
