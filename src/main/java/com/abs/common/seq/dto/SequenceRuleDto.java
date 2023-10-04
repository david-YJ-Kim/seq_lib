package com.abs.common.seq.dto;

import com.abs.common.seq.code.SeqCommonCode;
import org.json.JSONObject;

public class SequenceRuleDto {

    private String eventName;
    private String parsingItem;
    private Integer position;
    private String type;
    private String target;

    public SequenceRuleDto() {
    }

    public SequenceRuleDto(String jsonString){
        this(new JSONObject(jsonString));
    }

    public SequenceRuleDto(JSONObject jsonObject){
        this((String) jsonObject.get(SeqCommonCode.eventName.name()),
                (String) jsonObject.get(SeqCommonCode.parsingItem.name()),
                jsonObject.isNull(SeqCommonCode.position.name()) ? null : jsonObject.getInt(SeqCommonCode.position.name()),
                (String) jsonObject.get(SeqCommonCode.type.name()),
                (String) jsonObject.get(SeqCommonCode.target.name())
        );

    }


    public SequenceRuleDto(String eventName, String parsingItem, Integer position, String type, String target) {
        this.eventName = eventName;
        this.parsingItem = parsingItem;
        this.position = position;
        this.type = type;
        this.target = target;

        System.out.println(
                this.toString()
        );
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getParsingItem() {
        return parsingItem;
    }

    public void setParsingItem(String parsingItem) {
        this.parsingItem = parsingItem;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "EventRuleDto{" +
                "eventName='" + eventName + '\'' +
                ", parsingItem='" + parsingItem + '\'' +
                ", position=" + position +
                ", type='" + type + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
