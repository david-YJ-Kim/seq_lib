package com.abs.common.seq.checker;

import com.abs.common.seq.dto.SequenceRuleDto;
import com.abs.common.seq.util.SequenceManageUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EventRuleChecker {


    private String filePath;
    private String fileName;
    private String originData;


    private ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> eventRuleData;

    private ArrayList<String> registeredEventName;

    public EventRuleChecker(){}

    public EventRuleChecker(String ruleFilesPath, String eventRuleFilename, String jsonData){

        this.filePath = ruleFilesPath;
        this.fileName = eventRuleFilename;
        this.originData = jsonData;
        this.registeredEventName = new ArrayList<>();
        eventRuleData = this.setDataMap(new JSONObject(jsonData));

        System.out.println(
                this.toString()
        );

    }

    private ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> setDataMap(JSONObject dataObj){

        ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> map = new ConcurrentHashMap<>();

        for (String key: dataObj.keySet()){
            map.put(key, this.setEventRuleArray(dataObj.getJSONArray(key)));
        }

        return map;
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

    public SequenceRuleDto getEventRule(String eventName){

        return this.getEventRule(SequenceManageUtil.getTargetSystem(eventName),eventName);
    }

    public SequenceRuleDto getEventRule(String targetSystem, String eventName){

        if(!registeredEventName.contains(eventName)){
            System.out.println(
                    "Event Name is not registered"
            );
            return null;
        }else{
            return  this.eventRuleData.get(targetSystem).get(eventName);
        }
    }


    @Override
    public String toString() {
        return "EventRuleChecker{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originData='" + originData + '\'' +
                ", eventRuleData=" + eventRuleData +
                ", registeredEventName=" + registeredEventName +
                '}';
    }
}
