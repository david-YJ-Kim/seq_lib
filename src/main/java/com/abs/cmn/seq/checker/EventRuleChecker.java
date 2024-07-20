package com.abs.cmn.seq.checker;

import com.abs.cmn.seq.checker.code.CheckerCommonCode;
import com.abs.cmn.seq.checker.util.RuleCheckerUtil;
import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EventRuleChecker implements RuleCheckerInterface<ConcurrentHashMap<String, SequenceRuleDto>> {

    private static final Logger logger = LoggerFactory.getLogger(EventRuleChecker.class);

    private String filePath;
    private String fileName;
    private JSONObject currentRuleObject;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> ruleDataMap;

    private ConcurrentHashMap<String, JSONObject> ruleDataHistoryMap;
    private ArrayList<String> ruleHistoryKeySequenceList;

    private ArrayList<String> registeredEventName;

    private final RuleCheckerUtil util = new RuleCheckerUtil();

    public EventRuleChecker(String ruleFilesPath, String eventRuleFilename, JSONObject ruleObject){

        this.filePath = ruleFilesPath;
        this.fileName = eventRuleFilename;
        this.currentRuleObject = ruleObject;
        this.registeredEventName = new ArrayList<>();

        this.ruleHistoryKeySequenceList = new ArrayList<>();
        this.ruleDataHistoryMap = new ConcurrentHashMap<>();
        util.saveHistory(CheckerCommonCode.INIT.name(), ruleObject, this.ruleDataHistoryMap, this.ruleHistoryKeySequenceList);

        ruleDataMap = this.setSequenceData(ruleObject);

        logger.info("Class has been initialize. Print out class parameters:{}", this.toString());

    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> setSequenceData(JSONObject currentRuleObject) {


        ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> map = new ConcurrentHashMap<>();

        this.generateRuleDataMap(map, currentRuleObject);
        return map;

    }


    @Override
    public boolean sequenceDataBackUp() {

        String ruleKey = SequenceManageUtil.generateErcKey();

        try{
            util.saveHistory(ruleKey, this.currentRuleObject, this.ruleDataHistoryMap, this.ruleHistoryKeySequenceList);

            if(this.ruleDataHistoryMap.contains(ruleKey) && this.ruleHistoryKeySequenceList.contains(ruleKey)){

                logger.info("Rule data has been back-up ed. ruleKey:{}, currentRuleObjectData :{}, ruleSequenceList:{}, historyMap:{}",
                        ruleKey, this.currentRuleObject, this.ruleHistoryKeySequenceList.toString(), ruleDataHistoryMap.toString());
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error while back-up current data." +
                            " currentRuleObjectData :{}, ruleSequenceList:{}, historyMap:{}",
                    this.currentRuleObject, this.ruleHistoryKeySequenceList.toString(), ruleDataHistoryMap.toString());
            return false;

        }


        return false;
    }

    @Override
    public boolean sequenceDataReload(JSONObject ruleObject) {

        // Back up temporary rule obj for in case.
        JSONObject tmpCurrentRuleObj = this.currentRuleObject;
        String ruleKey = SequenceManageUtil.generateErcKey();

        try{

            // 실제 Map Update 하는 로직
            boolean reloadResultSuccess = this.updateRuleDataMap(ruleObject);
            if(!reloadResultSuccess){
                logger.error("Fail to reload with rule object: {}. its key: {}", ruleObject.toString(), ruleKey);
                return false;
            }
            this.currentRuleObject = ruleObject;

            return true;

        }catch (Exception e){

            this.currentRuleObject = tmpCurrentRuleObj;
            util.deleteHistory(ruleKey, this.ruleDataHistoryMap, this.ruleHistoryKeySequenceList);

            return false;
        }
    }

    public SequenceRuleDto getEventRule(String eventName){

        return this.getEventRule(SequenceManageUtil.getTargetSystem(eventName),eventName);
    }

    public SequenceRuleDto getEventRule(String targetSystem, String eventName){

        if(!registeredEventName.contains(eventName)){
            return null;
        }else{

            if(this.ruleDataMap.get(targetSystem) == null){
                logger.warn("Event Name is registered but not for that target.  CID: {}, EventName: {}", eventName, targetSystem);
                return null;
            }else {
                if(this.ruleDataMap.get(targetSystem) == null){
                    logger.warn("Event Name is registered but not for that target.  CID: {}, EventName: {}", eventName, targetSystem);
                    return null;
                }else {
                    return this.ruleDataMap.get(targetSystem).get(eventName);
                }
            }

        }
    }

    private void generateRuleDataMap(ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> dataMap, JSONObject ruleObj){

        Iterator<String> keys = ruleObj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray jsonArray = ruleObj.getJSONArray(key);
            ConcurrentHashMap<String, SequenceRuleDto> value = this.setEventRuleArray(jsonArray);
            dataMap.put(key, value);
            logger.info("Sequence rule data generate map with key: {}, value: {}", key, value.toString());
        }
        logger.info("Rule data map has been generated. ruleObject: {}. data map: {}",
                ruleObj.toString(), dataMap.toString());

    }

    private ConcurrentHashMap<String, SequenceRuleDto> setEventRuleArray(JSONArray jsonArray){
        ConcurrentHashMap<String, SequenceRuleDto> map = new ConcurrentHashMap<>();

        for (int i=0; i < jsonArray.length(); i++){
            SequenceRuleDto sequenceRuleDto = new SequenceRuleDto(jsonArray.getJSONObject(i));
            String eventName = sequenceRuleDto.getEventName();
            map.put(eventName, sequenceRuleDto);
            this.registeredEventName.add(sequenceRuleDto.getEventName());
        }

        System.out.println(
                map.toString()
        );

        return map;

    }

    /**
     *
     * @param ruleObj
     * @return
     */
    private boolean updateRuleDataMap(JSONObject ruleObj){

        try{

            synchronized (this.ruleDataMap){

//                for (String key: currentRuleObject.keySet()){
//                    this.ruleDataMap.remove(key);
//                    logger.info("Sequence data going to update. Remove key: {}", key);
//                }
                this.ruleDataMap.clear();
                logger.info("Current Rule data map has been cleared. Data Map: {}", this.ruleDataMap.toString());

                // Add new data
                this.generateRuleDataMap(this.ruleDataMap, ruleObj);
                logger.info("Current Rule data map has been reloaded.");

            }

            return true;
        }catch (Exception e){

            // Error Occur, Need to update with current Data.
            synchronized (this.ruleDataMap){

                this.generateRuleDataMap(this.ruleDataMap, this.currentRuleObject);
            }

            e.printStackTrace();
            logger.error("Error occurred while reload rule data And Complete to rollback. e:{}, errorMessage:{}", e, e.getMessage());
            return  false;
        }
    }



    @Override
    public String toString() {
        return "EventRuleChecker{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originData='" + currentRuleObject.toString() + '\'' +
                ", ruleDataMap=" + ruleDataMap +
                ", registeredEventName=" + registeredEventName +
                '}';
    }


}
