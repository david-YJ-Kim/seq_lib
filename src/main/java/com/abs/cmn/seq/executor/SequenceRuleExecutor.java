package com.abs.cmn.seq.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
 
    public static void main(String[] args) {
 
    }
 
	public SequenceRuleExecutor(int maxQueueSize){
		this.maxQueueSize = maxQueueSize;
	}
 
    public String executeEventRule(String targetName, String eventName, JSONObject payload, SequenceRuleDto ruleDto){
 
    	log.info("$$>> executeEventRule param targetName : {},%n eventName: {}, "+targetName, eventName);
        String topicVal = null;
        String type = null;
        String key = null;
 
        // type 이 null 이면 parsing item 에서 값을 가져옴 
        if ( ruleDto.getType() != null && ruleDto.getType().length() > 0 )
        	type = ruleDto.getType();
        else type = parseFromParsingItemName(ruleDto.getParsingItem());
        // Parsing Item 에 Depth가 있을 경우와 없을 경우, key 값을 가져옴. 
        if(ruleDto.getParsingItem().indexOf("/") != -1){
    		log.info(">> Depth parsing item : "+ruleDto.getParsingItem());
			key = parseParsingItemDepth(ruleDto.getParsingItem(), payload.getJSONObject(PayloadCommonCode.body.name()));
        } else {
            key = payload.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());
        }
        // TODO parsingItem이 payload-body에 없는 경우
        log.info("@@ 1 executeEventRule Parsing item : "+ruleDto.getParsingItem());
        log.info("@@ 2 executeEventRule type : "+type);
    	log.info("@@ 3 executeEventRule key : "+key);
    	
        // 큐 타입 설정 - 타입 분류는 위에서 이미 함.
    	if ( type != null || type.length() > 0 )
    		topicVal = type.concat("/");
    	else
    		topicVal = SeqCommonCode.CMN.name().concat("/");
    	
		// 큐 키 설정 - 파싱
		if (key != null) {
        	log.info("## (1) . executeEventRule with key. ");
        	if ( ruleDto.getPosition() != null ) {
        		
        		String position = ruleDto.getPosition();
        		log.info("## (1) 1-1 . executeEventRule EQP parsing. / postion : "+position);
        		
        		// 포지션이 1자리 이상일 때,
        		if ( ruleDto.getPosition().length() > 1 ) {
        			topicVal += this.convertIdintoAsciiValue(key, ruleDto.getPosition());
        		// 포지션이 1자리 일 때
        		} else {
        			topicVal += this.parsePosFromParsingItem(key, Integer.valueOf(ruleDto.getPosition()));
        		}
        	// 포지션이 없을 때 :: 마지막 2자리 파싱
        	} else {
        		log.info("## (1) 2 . executeEventRule EQP parsing. / no position , cut final 2 characters ");
        		topicVal += this.parsePosFromParsingItem(key, key.length()-1);
        	}
        	
        } else {
        	log.info("## (2) executeEventRule without key. ");
        	topicVal += "/00"; // CMN/00
        }
 
		log.info("topic Value: {}", topicVal);
 
		// $TYPE/$KEY
        return "/" + topicVal;
    }

    public String executeParsingRule(String targetSystem, String eventName, JSONObject payload, ArrayList<SequenceRuleDto> ruleDtoArray){
 
        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());
        String topicVal = null;//targetSystem.concat("/");
//        log.info("1. topicVal "+topicVal);
        int i = 0;
        for(SequenceRuleDto ruleDto : ruleDtoArray){
        	i++;
        	topicVal = null;
//        	Set<String> duplicateKeyCheck = fiindDuplicateKeys(bodyObj);
        	
        	// 룰에 등록된 타겟은 Modified Target으로 인지 Modified 일 때의 룰 수행으로 변경
            if( ruleDto.getTarget() != null && !ruleDto.getTarget().equals(targetSystem)) {
            	log.info(">> 36. executeParsingRule - targetSystem : CMN : " + i);
                return this.basicSequenceRule();
			// 룰 타겟 AP 와  입력된 송신 대상 타겟 시스템이 같다 
            } else {
 
            	log.info(">> 33. check getParsingItem : "+i+" : "+ruleDto.getParsingItem());
            	String key = "";
                String position = "";       
                /**
                 * 1. 파싱 아이템에 Depth 가 있는지 확인  하여 key 변수에 item 값 설정
                 *  Depth 가 있다면, Depth 파싱 하여 item 값을 읽어옴.
                 *  Depth가 없다면, 바로 아이템 값을 읽어옴 
            	 **/
            	if(ruleDto.getParsingItem().indexOf("/") != -1){
            		log.info(">> 34-1. Depth parsing item : "+i+" : "+ruleDto.getParsingItem());
    				key = parseParsingItemDepth(ruleDto.getParsingItem(), bodyObj);
    				// 뎁스 아이템 return이 null 이면, 다음 루프 진행 
    				if ( key == null ) continue;
				// Depth 없는 파싱 아이템이 있는 경우 (1)
            	} else if( !bodyObj.isNull(ruleDto.getParsingItem()) ){
    				log.info(">> 34-2. No Depth parsing item : "+i+" : "+ruleDto.getParsingItem());
					key = bodyObj.getString(ruleDto.getParsingItem());
					log.info(">> 34-2-1. keym : "+key);
	            // 룰에 파싱 아이템이 아예 없는 경우 continue ; 다 돌고 common 으로 끝
	            } else {
					log.info(" >> 34-3. No Item defined : " + i);	            	
					continue;
	            }

            	/**
                 * 2. 아이템 값이 null 이더나 없는 경우는 다음 룰로, 
                 *  아이템 값이 있는 경우, Position 설정에 따라 파싱 값 가져옴
            	 **/
				if (  key == null || key.contentEquals("") ) {
					log.info(" >> 35-1. No values in item name key : " + i);	    
					continue;
				} else {
					log.info(" >> 35-2. parsing key values : " + i);	    
					// 4 단계 파싱
					topicVal = getTypeIdParsingRule(ruleDto, key);
					/**
                	 * 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리 
                	 * > ItemName( := type ) check 4 depth
                	 **/	     
					if ( ruleDto.getPosition() != null ) {
	                	position = ruleDto.getPosition();
	                	log.info("################################# rule : "+ruleDto.toString());
	                	if ( position.length() < 2 && (Integer.valueOf(position)-1) < key.length()-1 )
	                		return topicVal.concat(parsePosFromParsingItem(key, Integer.valueOf(position)));
	                	else if ( position.length() < 2 && (Integer.valueOf(position)-1) >= key.length()-1 )
	                		return topicVal.concat(parsePosFromParsingItem(key, key.length()-1));
	                	else
	                		return topicVal.concat(convertIdintoAsciiValue(key, position));
	                } else {
	                	log.info("################################# rule : "+ruleDto.toString());
	                	return topicVal.concat(parsePosFromParsingItem(key,key.length()-1));
	                }
				}
            }	
        }
        
        // Parsing Item 값이 존재하지 않으면, targetSystem + CMN/00 로 종료
        return topicVal=this.basicSequenceRule();
    }
    private String getTypeIdParsingRule(SequenceRuleDto ruleDto, String key ) {
    	String type = "";
        String topicVal = null;
        /**
    	 * 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리 
    	 * > ItemName( := type ) check 4 depth
    	 **/
 
		// 타입을 설정한다.
        if ( ruleDto.getType() != null ) {
        	type = ruleDto.getType();
        	log.info("2. topicVal "+topicVal);
        	topicVal = type.concat("/");
 
        } else {
        	// TODO EQP외 대응 (CARR, LOT, CMN)
        	topicVal = parseFromParsingItemName(key).concat("/");
        	log.info("3. topicVal "+topicVal);
        }
    	return topicVal;
    }

 
	/**
	 *
	 * @param payload
	 * @return
	 */
    public String executeEAPParsingRule(JSONObject payload){
 
    	String topicVal= SeqCommonCode.CMN.name() + "/";
    	JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());
    	return topicVal.concat(bodyObj.getString(SeqCommonCode.eqpId.name()));
    }
 
    public String basicSequenceRule(){
        return  SeqCommonCode.CMN.name() + "/00";
    }
    private String convertEqpIdintoAsciiValue(String eqpId){
    	log.info(">>>  executeEventRule param eqpId : "+eqpId);
    	log.info(">>>  executeEventRule maxQueueSize : "+maxQueueSize);
 
        int val = (eqpId.charAt(1)+ (int) eqpId.charAt(3) + (int) eqpId.charAt(4) + (int) eqpId.charAt(7)) % maxQueueSize;
 
        log.info("## executeEventRule val : "+val);
        return String.format("%02d", val);
 
    }
    
    // ④ Parsing Item으로 파싱된 값을 ⑥ Item Name 에 따라 처리   
    private String convertIdintoAsciiValue(String parsItem, String position){
    	String[] postions = position.split(",");
    	int rst = 0;
    	for (String pos : postions) {
    		if ( Integer.valueOf(pos)-1 < parsItem.length() )
    			rst += parsItem.charAt(Integer.valueOf(pos)-1);
    		else
    			break;
    	}
    	log.info("## check params : parsItem "+parsItem);
    	log.info("## check params : postion "+position);
    	log.info("## check value : rst "+rst);
    	log.info("## check value :  rst % maxQueueSize "+rst%maxQueueSize);
    	String id = String.valueOf(rst%maxQueueSize);
    	if (id.length() < 2) {
    		log.info("## check value : one length id - "+"0"+id);
    		log.info("String.format(\"%2d\", rst%maxQueueSize): "+String.format("%2d", rst%maxQueueSize));
    		return "0"+id;
    	} else {
    		log.info("## check value : 2char id "+id);
    		return id;
    	}
    }
    /**
     * position 1자리로 무조건 2자리 파싱해서 return 하기! 
    */
    private String parsePosFromParsingItem (String key, int position) {
    	
    		log.info("## itemValue "+key);
    		log.info("## itemValue substring (return) "+key.substring(position-1, position+1));
 
    		String originPosValue = "";
			int res = 0;
			
			log.info("@@ parsePosFromParsingItem(), position:{} , key.length():{} ", position, key.substring(position-1, position+1));
			originPosValue = key.substring(position-1, position+1);
			log.info("## originPosValue :: "+originPosValue);
			try {
				res = Integer.valueOf(originPosValue) % maxQueueSize;
			} catch (Exception e) {
				res = 0;
			}
 
			return String.format("%02d", res);
 
    }
    
 
    private String parseFromParsingItemName(String parsItemName) {
    	// 2.3.2 ⑥ Item Name(값이 없으면 ④ Parsing Item 앞4자리를 잘라 대문자 변환
    	// CARR외 3자리가 LOT, EQP 가 아니면 CMN으로 처리)
    	String id = null;
    	if ( parsItemName.substring(0).equals("c") ) {
    		id = parsItemName.substring(0, 4).toUpperCase();
    		log.info("## parseFromParsingItemName() 1. id : "+ id); 
    	} else {
    		id = parsItemName.substring(0, 3).toUpperCase();
    		log.info("## parseFromParsingItemName() 2. id : "+ id);
    		if ( !id.equals(SeqCommonCode.EQP.name()) && !id.equals(SeqCommonCode.LOT.name()) ) id = "CMM";
    	}
 
    	return id.toUpperCase();
    }
    private String parseParsingItemDepth(String parsingItem, JSONObject bodyObj) {
    	// 파싱 item depth 를 나눔   ex) [0] carridList // [1] carrid
    	String[] item = parsingItem.split("/"); 
		log.info("## executeParsingRule - parsingItem depth process");
		String key = null;
		if ( bodyObj.has(item[0]) ) {
			JSONObject obj = null;
			log.info("## parseParsingItemDepth() bodyObj.getJSONArray(item[0]).length() : "+bodyObj.getJSONArray(item[0]).length());
			for ( int i = 0 ; i < bodyObj.getJSONArray(item[0]).length() ; i++ ) {
				if ( bodyObj.getJSONArray(item[0]).get(i) != null ) {
					obj = (JSONObject) bodyObj.getJSONArray(item[0]).get(i);
					log.info("@@ SequecneRuleExcutor, parseParsingItemDepth() : "+obj.toString());
					if ( obj.toString().indexOf(item[1]) > -1 )
						key = obj.getString(item[1]);
					else
						continue;
					if ( key != null && !key.equals("") ) {
						log.info("## executeParsingRule, parseParsingItemDepth() - key : " + key);
						break;
					} else 
						continue;
				} else {
					continue;
				}
			}
		} else {
			return key;
		}
		return key;
    }
//    private Set<String> fiindDuplicateKeys(JSONObject obj) {
//    	Set<String> allKeys = new HashSet<>();
//    	Set<String> duplicateKeys = new HashSet<String>();
//    	for (String key : obj.keySet()) {
//    		if ( !allKeys.add(key) )
//    			duplicateKeys.add(key);
//    	}
//    	return duplicateKeys;
//    }
}
