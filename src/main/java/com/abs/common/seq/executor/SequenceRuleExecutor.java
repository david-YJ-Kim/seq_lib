package com.abs.common.seq.executor;

import com.abs.common.seq.code.PayloadCommonCode;
import com.abs.common.seq.code.SeqCommonCode;
import com.abs.common.seq.dto.SequenceRuleDto;
import org.json.JSONObject;

import java.util.ArrayList;

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

    public String executeEventRule(String targetSystem, String eventName, JSONObject payload, SequenceRuleDto ruleDto){

        String topicVal = null;
        String type = ruleDto.getType();
        String key = payload.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());
        // TODO parsingItem이 payload-body에 없는 경우

        if(SeqCommonCode.EQP.equals(type)){

            topicVal = type + "/" + this.convertEqpIdintoAsciiValue(key);
        }else{
            // TODO CARR, LOT, CMN인 경우 대응
            // 2.4.1. ⑥ Item Name가 EQP가 아닌 경우
            //  Position으로 입력된 자리수에서 2자리를 파싱(없으면 끝에서 2자리) 하여 숫자 변환 후 20으로 나눈 나머지 값
        }

        System.out.println(
                topicVal
        );

        return topicVal;
    }

    public String executeParsingRule(String targetSystem, String eventName, JSONObject payload, ArrayList<SequenceRuleDto> ruleDtoArray){

        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());
        for(SequenceRuleDto ruleDto : ruleDtoArray){

            if(ruleDto.getParsingItem().equals("/")){

                // TODO "/" Depth 존재하는 항목 대응
            }


            if(!bodyObj.isNull(ruleDto.getParsingItem())){

                // TODO 1-depths 인 경우
                String key = bodyObj.getString(ruleDto.getParsingItem());

                // TODO 파싱 아이템 존재, 작업 진행

                // TODO EQP 용 대응
                // TODO EQP외 대응 (CARR, LOT, CMN)

            }
        }


        return "";
    }

    public String basicSequenceRule(){
        return  "CMN/00";
    }

    private String convertEqpIdintoAsciiValue(String eqpId){

        int val = ((int) eqpId.charAt(1)+ (int) eqpId.charAt(3) + (int) eqpId.charAt(4) + (int) eqpId.charAt(7)) / 20;

        return String.format("%02d", String.valueOf(val));

    }


}
