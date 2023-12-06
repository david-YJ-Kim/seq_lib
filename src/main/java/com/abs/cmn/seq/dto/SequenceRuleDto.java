package com.abs.cmn.seq.dto;

import com.abs.cmn.seq.code.SeqCommonCode;
import org.json.JSONObject;

/**
 * 메시지 분배 룰 정보를 담은 객체
 */
public class SequenceRuleDto {

    private String eventName;
    private String parsingItem;
    private String position;
    private String type;
    private String target;
    private String modifiedTarget;

    
    public SequenceRuleDto() {
    }

    public SequenceRuleDto(String jsonString){
        this(new JSONObject(jsonString));
    }

    public SequenceRuleDto(JSONObject jsonObject){
        this(
            jsonObject.isNull(SeqCommonCode.eventName.name()) ? null : (String) jsonObject.get(SeqCommonCode.eventName.name()),
            (String) jsonObject.get(SeqCommonCode.parsingItem.name()),                
            jsonObject.isNull(SeqCommonCode.position.name()) ? null : String.valueOf(jsonObject.get(SeqCommonCode.position.name())),                
            jsonObject.isNull(SeqCommonCode.type.name())? null: String.valueOf( jsonObject.get(SeqCommonCode.type.name())),
            jsonObject.isNull(SeqCommonCode.target.name()) ? null : (String) jsonObject.get(SeqCommonCode.target.name()),
    		jsonObject.isNull(SeqCommonCode.target.name()) ? null : (String) jsonObject.get(SeqCommonCode.target.name())
                		
        );
 
    }
     

    

    public SequenceRuleDto(String eventName, String parsingItem, String position, String type, String target, String modifiedTarget) {
        this.eventName = eventName;
        this.parsingItem = parsingItem;
        this.position = position;
        this.type = type;
        this.target = target;
        this.modifiedTarget = modifiedTarget;

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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
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


	public String getModifiedTarget() {
		return modifiedTarget;
	}

	public void setModifiedTarget(String modifiedTarget) {
		this.modifiedTarget = modifiedTarget;
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
