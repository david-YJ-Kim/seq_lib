package com.abs.cmn.seq.executor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abs.cmn.seq.code.PayloadCommonCode;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.dto.SequenceRuleDto;

public class SequenceRuleExecutor {
	
	private static final Logger log = LoggerFactory.getLogger(SequenceRuleExecutor.class);

	// TODO Manager에서 Exceutor 생성 시 넘어와야함.

	private int maxQueueSize;
	private String defaultTarget = "CMN/00";
	
    public static void main(String[] args) {

    }

	public SequenceRuleExecutor(int maxQueueSize){
		this.maxQueueSize = maxQueueSize;
	}

    public String executeEventRule(String targetName, String eventName, JSONObject payload, SequenceRuleDto ruleDto){

    	log.info("## executeEventRule param targetName : "+targetName);
    	log.info("## executeEventRule param eventName : "+eventName);
    	log.info("## executeEventRule param payload : "+payload);
    	log.info("## executeEventRule param ruleDto : "+ruleDto.toString());
    	
    	
        String topicVal = "";//targetName.concat("/");
        String type = ruleDto.getType();
        String key = payload.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());
        // TODO parsingItem이 payload-body에 없는 경우

        log.info("@@ executeEventRule type : "+type);
    	log.info("@@ executeEventRule key : "+key);
    	
        
    	if (type != null) topicVal.concat(type).concat("/");
    	
        if (key != null) {
        	
        	log.info("## 1 . executeEventRule with key. ");
        	
	        if(SeqCommonCode.EQP.equals(type)) {
	        	
	            topicVal += this.convertEqpIdintoAsciiValue(key);
	            log.info("## 2-1 . executeEventRule EQP parsing. ");
	            
	        } else {
	            // TODO CARR, LOT, CMN인 경우 대응
	            // 2.4.1. ⑥ Item Name가 EQP가 아닌 경우
	            // Position으로 입력된 자리수에서 2자리를 파싱(없으면 끝에서 2자리) 하여 숫자 변환 후 20으로 나눈 나머지 값
	        	
	        	String position = "";
	        	topicVal += ruleDto.getType().concat("/");
	        	
	        	log.info("## 2-2 executeEventRule Not EQP . ");
      	
	        	if ( ruleDto.getPosition() != null ) {
	        		
	        		log.info("## executeEventRule type position parsing. ");	        		
	    			topicVal += parsePosFromParsingItem( key, Integer.valueOf( ruleDto.getPosition())-1 );
	    			log.info("@@ executeEventRule topicVal : "+topicVal);
	    			
	    		} else {
	    			
	    			log.info("## executeEventRule key value's last 2 characters. ");
	    			topicVal += parsePosFromParsingItem( key, key.length()-2);
	    			log.info("@@ executeEventRule topicVal : "+topicVal);
	    		}
	        	
	        }
        } else {
        	
        	log.info("## executeEventRule without key. ");
        	
        	topicVal += defaultTarget;
        }

        System.out.println(
                topicVal
        );

        return "/" + topicVal;
    }

    public String executeParsingRule(String targetSystem, String eventName, JSONObject payload, ArrayList<SequenceRuleDto> ruleDtoArray){

        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());
        String topicVal = "";//targetSystem.concat("/");
        
        log.info("1. topicVal "+topicVal);

        for(SequenceRuleDto ruleDto : ruleDtoArray){
        	
            if( ruleDto.getTarget() != null && !ruleDto.getTarget().equals(targetSystem)) {
            	
            	log.info("## executeParsingRule - targetSystem ");
                return targetSystem.concat("/").concat(basicSequenceRule());
                
            } else {
            	// TODO 1-depths 인 경우
            	
	            if(!bodyObj.isNull(ruleDto.getParsingItem())){
	            	// TODO 파싱 아이템 존재, 작업 진행
	            	
	            	String[] item = null;
	            	String key = "";
	            	String type = "";
	                String position = "";                
	            	
	            	/**
	            	 * 2.3.2 ② Sequence 순서대로 ④ Parsing Item 값이 존재하는지 체크
	            	 * key value setting, from parsing item
	            	 * parsing Item depth ? get array[0].item value : parsingItem value 
	            	 **/
	            	if(ruleDto.getParsingItem().indexOf("/") != -1){
	    				// TODO "/" Depth 존재하는 항목 대응
	    				key = parseParsingItemDepth(ruleDto.getParsingItem(), bodyObj);
	    			} else {
//	    				key = bodyObj.getString(ruleDto.getParsingItem());
	    				key = checkIdValueArry(bodyObj.getString(ruleDto.getParsingItem()));
	    			}
	                

                	/**
                	 * 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리 
                	 * > ItemName( := type ) check 4 depth
                	 **/
	                if ( ruleDto.getType() != null ) {
	                	type = ruleDto.getType();
	                	topicVal += type.concat("/");
	                	log.info("2. topicVal "+topicVal);
	                } else {
	                	// TODO EQP외 대응 (CARR, LOT, CMN)
	                	topicVal += parseFromParsingItemName(key).concat("/");
	                	log.info("3. topicVal "+topicVal);
	                }
	                
	                /**
                	 * 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리 
                	 * > ItemName( := type ) check 4 depth
                	 **/
	                log.info("@@. topicVal "+topicVal);
	                
	                if ( ruleDto.getPosition() != null ) {
	                	position = bodyObj.getString(ruleDto.getPosition() );
	                	
//	                	if ( position.length() < 2 )
	                	
	                		return topicVal.concat(parsePosFromParsingItem( key, Integer.valueOf(position)) );
//	                	else
//	                		return topicVal.concat(parsePosFromParsingItem( key, position) );
	                	
	                } else {
	                	return topicVal.concat(parsePosFromParsingItem( key, key.length()-2));
	                }
	                
	                // TODO EQP 용 대응
	            } else {
	            	continue;
	            }
            }	
        }
        
        // Parsing Item 값이 존재하지 않으면, targetSystem + CMN/00 로 종료
        return topicVal.concat(basicSequenceRule());
    }

    public String basicSequenceRule(){
        return  defaultTarget;
    }
    
    private String convertEqpIdintoAsciiValue(String eqpId){
    	
    	log.info(">>>  executeEventRule param eqpId : "+eqpId);
    	log.info(">>>  executeEventRule maxQueueSize : "+maxQueueSize);

        int val = (eqpId.charAt(1)+ (int) eqpId.charAt(3) + (int) eqpId.charAt(4) + (int) eqpId.charAt(7)) % maxQueueSize;

        log.info("## executeEventRule val : "+val);
        
        return String.format("%02d", String.valueOf(val));

    }
    
    // ④ Parsing Item으로 파싱된 값을 ⑥ Item Name 에 따라 처리   
    private String convertIdintoAsciiValue(String parsItem, String position){
    	
    	String[] postions = position.split(",");
    	int rst = 0;
    	
    	for (String pos : postions) {
    		rst += parsItem.charAt(Integer.valueOf(pos));
    	}
    	
    	log.info("## check params : parsItem "+parsItem);
    	log.info("## check params : postion "+position);
    	log.info("## check value : rst "+rst);
    	
    	return String.format("%2d", rst%maxQueueSize );
    }
    
    /**
     * position 
    */
    private String parsePosFromParsingItem (String itemValue, int position) {
    	
    		log.info("## parsePosFromParsingItem posiotn 1 character or last 2 characters");
    		log.info("## itemValue "+itemValue);
//    		int rtn = Integer.valueOf( itemValue.substring(position, position+2) );    		
    		log.info("## itemValue substring (return) "+itemValue.substring(position, position+2));
    		
    		if (itemValue.substring(position, position+2).length() < 2)
    			return "0"+itemValue.substring(position, position+2);
    		
    		return itemValue.substring(position, position+2);
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
    
    private String parseParsingItemDepth(String parsingItem, JSONObject bodyObj) {
    	String[] item = parsingItem.split("/");	    				
		log.info("## executeParsingRule - parsingItem depth process");
		
		JSONObject obj = new JSONObject(bodyObj.getString(item[0]));
		for ( int i = 1 ; i < item.length-3 ; i++ ) {
			obj = new JSONObject( obj.getString(item[i]) );
		}
		JSONArray jsnArr = obj.getJSONArray(item[ item.length -2 ]);
		
		return ((JSONObject) jsnArr.get(0)).getString(item[item.length-1]);
    }
    
    
    private String checkIdValueArry (String idValue) {
    	if ( idValue.contains(",") ) {
    		String[] ids = idValue.split(",");
    		return ids[0];
    	}
    	return idValue;
    }

}
