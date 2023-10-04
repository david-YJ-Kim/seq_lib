package com.abs.common.seq.executor;

import com.abs.common.seq.code.PayloadCommonCode;
import com.abs.common.seq.dto.SequenceRuleDto;
import org.json.JSONObject;

public class SequenceRuleExecutor {

    public static void main(String[] args) {


//        String id = "AP-SR-02-01";
        String id = "AP-LA-02-01";
//        String id = "AP-TG-02-01";
        System.out.println(
                id.charAt(1) + " / " + id.charAt(3) + " / " +id.charAt(4) + " / " +id.charAt(7) + " / "
        );
        System.out.println(
                ((int) id.charAt(1)+ (int) id.charAt(3) + (int) id.charAt(4) + (int) id.charAt(7)) / 20
        );
    }

    public String executeEventRule(String targetSystem, String eventName, String payload, SequenceRuleDto ruleDto){

        JSONObject object = new JSONObject(payload);
        String key = object.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());


        return targetSystem + "/" + "";
    }

    public String executeParsingRule(String targetSystem, String eventName, String payload, SequenceRuleDto ruleDto){

        return "";
    }

    private String basicSequenceRule(){
        return  "CMN/00";
    }

    private String convertEqpIdintoAscilValue(String eqpId){

        return "";

    }


}
