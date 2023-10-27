package com.abs.cmn.seq.checker;

import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ParsingRuleChecker {

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
    private JSONObject ruleObj;
    private ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> parsingRuleData;


    public ParsingRuleChecker(String sourceSystem, String ruleFilePath, String ruleFileName, JSONObject ruleObj){

        this.sourceSystem = sourceSystem;
        this.filePath = ruleFilePath;
        this.fileName = ruleFileName;
        this.ruleObj = ruleObj;

        System.out.println(
                this.toString()
        );

        parsingRuleData = this.setDataMap(sourceSystem, ruleObj);


    }

    private ConcurrentHashMap<String, ArrayList<SequenceRuleDto>> setDataMap(String sourceSystem, JSONObject dataObj){

        ConcurrentHashMap map = new ConcurrentHashMap();
        JSONObject object = dataObj.getJSONObject(sourceSystem);
        for(String key: object.keySet()){
            map.put(key, this.setParsingSequenceArray(object.getJSONArray(key)));
        }
        System.out.println(
                map.toString()
        );

        return map;

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

        return parsingRuleData.get(targetSystem);
    }


    @Override
    public String toString() {
        return "ParsingRuleChecker{" +
                "sourceSystem='" + sourceSystem + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originData='" + ruleObj.toString() + '\'' +
                ", parsingRuleData=" + parsingRuleData +
                '}';
    }
}
