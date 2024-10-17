package com.abs.cmn.seq.checker;

import com.abs.cmn.seq.checker.code.CheckerCommonCode;
import com.abs.cmn.seq.checker.util.RuleCheckerUtil;
import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ParsingRuleChecker implements RuleCheckerInterface<ArrayList<SequenceRuleDto>>{

    private static final Logger logger = LoggerFactory.getLogger(ParsingRuleChecker.class);

    public static void main(String[] args) throws IOException {
        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule.json";

        JSONObject jsonObject = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileName))).getJSONObject(SeqCommonCode.parsingRule.name());

        new ParsingRuleChecker("MCS", ruleFilesPath, sequenceRuleFileName, jsonObject);
    }

    private final String sourceSystem;
    private String filePath;
    private String fileName;
    private JSONObject currentRuleObject;
    private ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> ruleDataMap;

    private ConcurrentHashMap<String, JSONObject> ruleDataHistoryMap;
    private ArrayList<String> ruleHistoryKeySequenceList;

    private final RuleCheckerUtil util = new RuleCheckerUtil();


    public ParsingRuleChecker(String sourceSystem, String ruleFilePath, String ruleFileName, JSONObject ruleObject){

        this.sourceSystem = sourceSystem;
        this.filePath = ruleFilePath;
        this.fileName = ruleFileName;
        this.currentRuleObject = ruleObject;

        this.ruleHistoryKeySequenceList = new ArrayList<>();
        this.ruleDataHistoryMap = new ConcurrentHashMap<>();
        util.saveHistory(CheckerCommonCode.INIT.name(), ruleObject, this.ruleDataHistoryMap, this.ruleHistoryKeySequenceList);


        ruleDataMap = this.setSequenceData(ruleObject);


    }


    @Override
    public ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> setSequenceData(JSONObject ruleObject) {

        ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> map = new ConcurrentHashMap();
        this.generateRuleDataMap(map, ruleObject);
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

    private void generateRuleDataMap(ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> dataMap, JSONObject ruleObject){

        JSONObject object = ruleObject.getJSONObject(this.sourceSystem);
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray jsonArray = object.getJSONArray(key);
            dataMap.put(key, this.setParsingSequenceArray(jsonArray));
        }
    }

    private ArrayList<SequenceRuleDto> setParsingSequenceArray(JSONArray jsonArray){
        ArrayList arrayList = new ArrayList();
        for (int i=0; i < jsonArray.length(); i++){
            SequenceRuleDto sequenceRuleDto = new SequenceRuleDto(jsonArray.getJSONObject(i));
            arrayList.add(sequenceRuleDto);
        }
        return arrayList;
    }

    public ArrayList<SequenceRuleDto> getParsingRule(String targetSystem){

        return ruleDataMap.get(targetSystem);
    }


    /**
     *
     * @param ruleObj
     * @return
     */
    private boolean updateRuleDataMap(JSONObject ruleObj){

        try{

            synchronized (this.ruleDataMap){

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
        return "ParsingRuleChecker{" +
                "sourceSystem='" + sourceSystem + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", currentRuleObject=" + currentRuleObject +
                ", ruleDataMap=" + ruleDataMap +
                ", ruleDataHistoryMap=" + ruleDataHistoryMap +
                ", ruleHistoryKeySequenceList=" + ruleHistoryKeySequenceList +
                ", util=" + util +
                '}';
    }
}
