package com.abs.common.seq.executor;

import com.abs.common.seq.code.PayloadCommonCode;
import com.abs.common.seq.code.SeqCommonCode;
import com.abs.common.seq.dto.SequenceRuleDto;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

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

    public String executeEventRule(String eventName, JSONObject payload, SequenceRuleDto ruleDto){

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
        	
        	int seq = ruleDto.getPosition();
        	
        	topicVal = type + "/";
        	
        	if ( seq < -1 ) {	        	
    			topicVal.concat( parseSeqFromParsingItem( key, ruleDto.getPosition()) );
    		} else {
    			topicVal.concat( parseSeqFromParsingItem( key, key.length()-2) );
    		}
        }

        System.out.println(
                topicVal
        );

        return topicVal;
    }

    public String executeParsingRule(String targetSystem, String eventName, JSONObject payload, ArrayList<SequenceRuleDto> ruleDtoArray){

        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());

        for(SequenceRuleDto ruleDto : ruleDtoArray){

			/*
			 * if(ruleDto.getParsingItem().equals("/")){
			 * 
			 * // TODO "/" Depth 존재하는 항목 대응 }
			 * 		 >> "/" 가 있는 경우 carrList/lodId 와 같은 경우가 없다면, 파싱해서 사용해야 하는지 의문입니다. 
			 * 		 >> carrList/carrId 라면 괜찮지 않나요.
			 */


            if(!ruleDto.getTarget().equals(targetSystem))
                // TODO 1-depths 인 경우
            	
	            if(!bodyObj.isNull(ruleDto.getParsingItem())){
	            	// TODO 파싱 아이템 존재, 작업 진행
	            	
	            	String key = bodyObj.getString(ruleDto.getParsingItem());
	                String type = bodyObj.getString(ruleDto.getType());
	                String seq = bodyObj.getString(String.valueOf( (int)ruleDto.getPosition() )); 
	                
                	// 2.3.2 ② Sequence 순서대로 ④ Parsing Item 값이 존재하는지 체크
	                if (type != null) {
	                	
	                	// 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리
		                
		                if (seq.length()<2) 
		                	return type+"/"+String.format("%02d", String.valueOf(seq));
		                
	                } else {
	                	// TODO EQP외 대응 (CARR, LOT, CMN)	                	
	                	// 2.3.2.  ④ Parsing Item으로 파싱된 값을 ⑥ Item Name 에 따라 처리
	                	
	                	return parseFromParsingItemName(key)+"/"+convertIdintoAsciiValue(key, seq);
	                	
	                }

	                
	                // TODO EQP 용 대응
	                
	                
	            } else {
	            	continue;
	            }
        }
        
        // Parsing Item 값이 존재하지 않으면, + CMN/00 로 종료
        return basicSequenceRule();
    }

    public String basicSequenceRule(){
        return  "CMN/00";
    }
    
    private String convertEqpIdintoAsciiValue(String eqpId){

        int val = ((int) eqpId.charAt(1)+ (int) eqpId.charAt(3) + (int) eqpId.charAt(4) + (int) eqpId.charAt(7)) % 20;

        return String.format("%02d", String.valueOf(val));

    }
    
    // ④ Parsing Item으로 파싱된 값을 ⑥ Item Name 에 따라 처리  >> executeParsingRule 에서도 사용 
    private String convertIdintoAsciiValue(String parsItem, String position){
    	
    	int[] positions = Arrays.stream(position.split(",")).mapToInt(Integer::parseInt).toArray();
    	
    	int val = 0;
    	
    	for( int i = 0 ; i < positions.length ; i++ )
    		val += parsItem.charAt(positions[i]);
    	
    	val %= 20;

        return String.format("%02d", String.valueOf(val));

    }
    
    private String parseSeqFromParsingItem (String itemValue, int parsSeq) {
    	    	
    	return new String( itemValue.substring(parsSeq, parsSeq+1 ) );
    	
    }
    
    private String parseFromParsingItemName(String parsItemName) {
    	// 2.3.2 ⑥ Item Name(값이 없으면 ④ Parsing Item 앞4자리를 잘라 대문자 변환
    	// CARR외 3자리가 LOT, EQP 가 아니면 CMN으로 처리)
    	
    	String id = null;
    	
    	if ( parsItemName.substring(0).equals("c") ) {
    		
    		id = parsItemName.substring(0, 3).toUpperCase();
    	
    	} else {
    		
    		id = parsItemName.substring(0, 2).toUpperCase();
    		
    		if ( !id.equals("LOT") && !id.equals("EQP") ) id = "CMM";
    		
    	}

    	return id;
    }

}
