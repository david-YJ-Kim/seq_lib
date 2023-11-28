package com.abs.cmn.seq.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public static void main(String[] args) {

    }

	public SequenceRuleExecutor(int maxQueueSize){
		this.maxQueueSize = maxQueueSize;
	}

    public String executeEventRule(String targetName, String eventName, JSONObject payload, SequenceRuleDto ruleDto){

    	log.info("## executeEventRule param targetName : {}, eventName: {}, "+targetName, eventName);
    	log.info("## executeEventRule param payload : {} , ruleDto : {}"+payload.toString(), ruleDto.toString());
    	
        String topicVal = null;//targetName.concat("/");
        String type = null;
        String key = null;

        // type 이 null 이면 parsing item 에서 값을 가져옴 
        if ( ruleDto.getType() == null ) type = parseFromParsingItemName(ruleDto.getParsingItem());
        else type = ruleDto.getParsingItem();
        
        // Parsing Item 에 Depth가 있을 경우, 
        if(ruleDto.getParsingItem().indexOf("/") != -1){
    		log.info(">> Depth parsing item : "+ruleDto.getParsingItem());
			key = parseParsingItemDepth(ruleDto.getParsingItem(), payload.getJSONObject(PayloadCommonCode.body.name()));
        } else {
            key = payload.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());
        }
        
        // TODO parsingItem이 payload-body에 없는 경우
        log.info("@@ executeEventRule Parsing item : "+ruleDto.getParsingItem());
        log.info("@@ executeEventRule type : "+type);
    	log.info("@@ executeEventRule key : "+key);
    	
        // 큐 타입 설정
    	if ( type != null && type.length() > 0) {
    		log.info("## executeEventRule() ---- 1 log type not null  : " +ruleDto.getType());
			topicVal = type.concat("/");
			log.info("## executeEventRule() ---- 1-1 log set type : " +topicVal);
		}else {
			if ( ruleDto.getParsingItem()!= null ) {
				log.info("## executeEventRule() ---- 2 log type null  parsingItem: " +ruleDto.getParsingItem());
				topicVal = parseFromParsingItemName(ruleDto.getParsingItem()).concat("/");
				log.info("## executeEventRule() ---- 2-2 log set type : " +topicVal);
			}else {
				// 큐 타입 미등록 시, CMN 으로 리턴
				topicVal = SeqCommonCode.CMN.name().concat("/");
				log.info("## executeEventRule() ---- 3 log set type : " +topicVal);
			};
			
		} 
    	
		// 큐 키 설정
		if (key != null) {
        	
        	log.info("## 1 . executeEventRule with key. ");
        	
	        if(SeqCommonCode.EQP.name().equals(type)) {
	        	
	        	if ( ruleDto.getPosition() == null ) {
	        		// TODO - EQP ID 일 때, postion 이 null 이면, 가장 뒤에 2자리를 파싱
		            topicVal += this.parsePosFromParsingItem(key, Integer.valueOf( ruleDto.getPosition())-1);
		            log.info("## 2-1 . executeEventRule EQP parsing. / postion null");
	        	} else {
	        		topicVal += this.convertIdintoAsciiValue(key, ruleDto.getPosition());
		            log.info("## 2-1 . executeEventRule EQP parsing. / postion : "+ruleDto.getPosition());
	        	}
	            
	        } else {
	            // TODO CARR, LOT, CMN인 경우 대응
	            // 2.4.1. ⑥ Item Name가 EQP가 아닌 경우
	            // Position으로 입력된 자리수에서 2자리를 파싱(없으면 끝에서 2자리) 하여 숫자 변환 후 20으로 나눈 나머지 값
	        	
	        	String position = ruleDto.getPosition();
	        	
	        	log.info("## 2-2 executeEventRule Not EQP . ");
      	
				// 포지션 등록 OK
	        	if ( position != null && position.length() > 1 ) {
	        		
	        		log.info("## executeEventRule type position parsing. ");
	    			topicVal += convertIdintoAsciiValue(key, ruleDto.getPosition() );
	    			log.info("@@ executeEventRule topicVal : "+topicVal);
    			
	    		// 포지션이 4글자 이상 일 때, 
	    		} else if ( position != null && position.length() < 2){
	    			
	    			log.info("## executeEventRule type position parsing. ");
	    			topicVal += parsePosFromParsingItem(key, Integer.valueOf( ruleDto.getPosition())-1 );
	    			log.info("@@ executeEventRule topicVal : "+topicVal);
	    			
				// 포지션 등록 안됨
	    		} else {
	    			
	    			log.info("## executeEventRule key value's last 2 characters. ");
	    			topicVal += parsePosFromParsingItem(key, key.length()-2);
	    			log.info("@@ executeEventRule topicVal : "+topicVal);
	    		}
	        	
	        }
        } else {
        	
        	log.info("## executeEventRule without key. ");
        	
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
        	
        	Set<String> duplicateKeyCheck = fiindDuplicateKeys(bodyObj);
        	
        	// 룰에 등록된 타켓정보와 요청받은 타겟 시스템이 다른 경우
            if( ruleDto.getTarget() != null && !ruleDto.getTarget().equals(targetSystem)) {
            	log.info(">> 36. executeParsingRule - targetSystem : CMN : " + i);
                return this.basicSequenceRule();
                
			// 룰 타겟 AP 와  입력된 송신 대상 타겟 시스템이 같다 
            } else {

            	log.info(">> 33. check getParsingItem : "+i+" : "+ruleDto.getParsingItem());
            	
            	String key = "";
                String position = "";       
                
                /**
            	 * 2.3.2 ② Sequence 순서대로 ④ Parsing Item 값이 존재하는지 체크
            	 * key value setting, from parsing item
            	 * parsing Item depth ? get array[0].item value : parsingItem value 
            	 **/
            	
                // 파싱 아이템이 Depth 경우
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
            		
	            		            	
            	// item 값이 "" 이거나 null 인 경우!! 체크
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
	                		return topicVal.concat(parsePosFromParsingItem(key, Integer.valueOf(position)-1));
	                	else if ( position.length() < 2 && (Integer.valueOf(position)-1) >= key.length()-1 )
	                		return topicVal.concat(parsePosFromParsingItem(key, key.length()-2));
	                	else
	                		return topicVal.concat(convertIdintoAsciiValue(key, position));
	                } else {
	                	log.info("################################# rule : "+ruleDto.toString());
	                	return topicVal.concat(parsePosFromParsingItem(key,key.length()-2));
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
     * position 
    */
    private String parsePosFromParsingItem (String key, int position) {
    	
    		log.info("## parsePosFromParsingItem posiotn 1 character or last 2 characters");
    		log.info("## itemValue "+key);
//    		int rtn = Integer.valueOf( itemValue.substring(position, position+2) );    		
    		log.info("## itemValue substring (return) "+key.substring(position, position+2));

    		String originPosValue = "";
			int res = 0;
			
//			if ( position > key.length()-1) {
//    			log.info("## itemValue substring 1 (return) "+key.substring(position-1, position+1));
//    		// 2자리를 자른다. 
//    			originPosValue = key.substring(position-1, position+1);
//    			log.info("## itemValue substring 2 (return) "+Integer.valueOf(originPosValue));
//    			if ( Character.isDigit( originPosValue.charAt( originPosValue.length()-1 ) )) {
//    				res = Integer.valueOf(originPosValue) % maxQueueSize;
//    				log.info("## res :: "+res);
//    			} else if ( Character.isDigit( originPosValue.charAt( originPosValue.length()-2 ))
//    					&& Character.isDigit( originPosValue.charAt( originPosValue.length()-1 ))
//    					) {
//    				return parsePosFromParsingItem(key,key.length()-2);
//    			}
//    		} else {
    			log.info("@@ parsePosFromParsingItem(), position:{} , key.length():{} ", position, key.length());
    			log.info("@@ parsePosFromParsingItem(), position:{} , key.length():{} ", position, key.substring(position, position+2));
//    			position = key.length()-2;
    			originPosValue = key.substring(position, position+2);
    			log.info("## originPosValue :: "+originPosValue);
    			res = Integer.valueOf(originPosValue) % maxQueueSize;
//    		}

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
//    		log.info("## parseFromParsingItemName() 2. id : "+ id);
    		if ( !id.equals(SeqCommonCode.EQP.name()) && !id.equals(SeqCommonCode.LOT.name()) ) id = "CMM";
    	}

    	return id;
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
    
    private Set<String> fiindDuplicateKeys(JSONObject obj) {
    	
    	Set<String> allKeys = new HashSet<>();
    	Set<String> duplicateKeys = new HashSet<String>();
    	
    	for (String key : obj.keySet()) {
    		if ( !allKeys.add(key) )
    			duplicateKeys.add(key);
    	}
    	return duplicateKeys;
    }

}
