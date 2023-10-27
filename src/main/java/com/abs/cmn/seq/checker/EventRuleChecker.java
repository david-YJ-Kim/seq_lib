package com.abs.cmn.seq.checker;

import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.dto.SequenceRuleDto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EventRuleChecker {

	private static final Logger log = LoggerFactory.getLogger(EventRuleChecker.class);

    private String filePath;
    private String fileName;
    private JSONObject ruleObj;


    private ConcurrentHashMap<String, ConcurrentHashMap<String, SequenceRuleDto>> eventRuleData;

    private ArrayList<String> registeredEventName;

    public EventRuleChecker(){}

    public EventRuleChecker(String ruleFilesPath, String eventRuleFilename, JSONObject ruleObj){

        this.filePath = ruleFilesPath;
        this.fileName = eventRuleFilename;
        this.ruleObj = ruleObj;
        this.registeredEventName = new ArrayList<>();
        eventRuleData = this.setDataMap(ruleObj);

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
                ", originData='" + ruleObj.toString() + '\'' +
                ", eventRuleData=" + eventRuleData +
                ", registeredEventName=" + registeredEventName +
                '}';
    }
}
