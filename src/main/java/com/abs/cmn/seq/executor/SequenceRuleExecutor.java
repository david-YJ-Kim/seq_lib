package com.abs.cmn.seq.executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.abs.cmn.seq.util.SequenceManageUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abs.cmn.seq.code.PayloadCommonCode;
import com.abs.cmn.seq.code.SeqCommonCode;
import com.abs.cmn.seq.dto.SequenceRuleDto;

public class SequenceRuleExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SequenceRuleExecutor.class);


    private int maxQueueSize;

    public SequenceRuleExecutor(int maxQueueSize){
        this.maxQueueSize = maxQueueSize;
    }


    /**
     * 이벤트 룰 실행 메소드
     * @param targetName
     * @param eventName
     * @param payload
     * @param ruleDto
     * @return
     */
    public String executeEventRule(String targetName, String eventName, JSONObject payload, SequenceRuleDto ruleDto){

        logger.info("$$>> executeEventRule param targetName : {},%n eventName: {}, "+targetName, eventName);
        String type = null;
        String parsingTargetItem = null;

        // type 이 null 이면 parsing item 에서 값을 가져옴
        if(!SequenceManageUtil.isStringNull(ruleDto.getType())){
            type = ruleDto.getType();

        }else{

            type = this.parseFromParsingItemName(ruleDto.getParsingItem());
        }


        // KEY 구하는 부분
        // Parsing Item 에 Depth 있을 경우와 없을 경우, key 값을 가져옴.
        if(this.isDepthCase(ruleDto.getParsingItem())){
            logger.info(">> Depth parsing item : {} ", ruleDto.getParsingItem());
            parsingTargetItem = parseParsingItemDepth(ruleDto.getParsingItem(),
                    payload.getJSONObject(PayloadCommonCode.body.name()));
        } else {
            parsingTargetItem = payload.getJSONObject(PayloadCommonCode.body.name()).getString(ruleDto.getParsingItem());
        }


        // TODO parsingItem이 payload-body에 없는 경우
        logger.info("@@ 1 executeEventRule Parsing item : {} ", ruleDto.getParsingItem());
        logger.info("@@ 2 executeEventRule type : {}", type);
        logger.info("@@ 3 executeEventRule key : {}", parsingTargetItem);

        // 타입 설정
        // 큐 타입 설정 - 타입 분류는 위에서 이미 함.

        String topicVal = this.getTypeIdParsingRule(ruleDto, parsingTargetItem);
        logger.info("topic Value: {}", topicVal);
        return this.getLastValue(parsingTargetItem, topicVal, ruleDto);

    }


    /**
     * C. Basic topic generation rule > B. Others > B. Defined System
     * Iterate registered parsing rule and find the exact rule.
     *
     * Sequence
     * 1. Get Parsing Item
     * 2. Find position and generate key.
     *
     * 파싱 룰 실행 메소드
     * @param targetSystem
     * @param eventName
     * @param payload
     * @param ruleDtoArray
     * @return
     */
    public String executeParsingRule(String key, String targetSystem, String eventName, JSONObject payload, ArrayList<SequenceRuleDto> ruleDtoArray){

        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());

        int i = 0;
        for(SequenceRuleDto ruleDto : ruleDtoArray){
            i++;

            logger.info("{}: {}" +
                            "index: {}, ruleDto: {}."
                    ,key ,"Now check with below rule."
                    , i, ruleDto.toString()
            );

            /**
             * A. If modified target is exist and is not same with target system
             *    then return common topic name with modified target system.
             */
            // TODO Modified Target 관련한 내용으로 추정, 현재 해당 내용 이해 못함... 문서 확인 필요 (2024-02-12, David)
            // TODO Modified Target 관련하여 고민 필요
            // 룰에 등록된 타겟은 Modified Target으로 인지 Modified 일 때의 룰 수행으로 변경
            if(!SequenceManageUtil.isStringNull(ruleDto.getTarget())){
                // 룰에 등록된 모디파이 타켓과 파라미터로 들어온 타켓 시스템을 비교한다
                if(!ruleDto.getTarget().equals(targetSystem)){

                    logger.info(">> 36. executeParsingRule - targetSystem : CMN : " + i);
                    // 룰 타겟 AP 와  입력된 송신 대상 타겟 시스템이 같다
                    return this.basicSequenceRule();
                }
            }

            /**
             * B. If modified target is not registered.
             */
            else {

                String parsingTargetItem = ""; // parsingTargetItem
                String position = "";
                /**
                 * 1. 파싱 아이템에 Depth 가 있는지 확인  하여 parsingTargetItem 변수에 item 값 설정
                 *  Depth 가 있다면, Depth 파싱 하여 item 값을 읽어옴.
                 *  Depth가 없다면, 바로 아이템 값을 읽어옴
                 **/

                // Depth 있는 케이스
                if(this.isDepthCase(ruleDto.getParsingItem())){

                    logger.info(">> 34-1. Depth parsing item : {} : {}", i, ruleDto.getParsingItem());
                    parsingTargetItem = parseParsingItemDepth(ruleDto.getParsingItem(), bodyObj);

                }
                // Depth 없는 파싱 아이템이 있는 경우 (1)
                else if( !bodyObj.isNull(ruleDto.getParsingItem()) ){

                    parsingTargetItem = bodyObj.getString(ruleDto.getParsingItem());
                    logger.info(">> 34-2. No Depth parsing item : {} : {}", i, ruleDto.getParsingItem());

                }
                // 룰에 파싱 아이템이 아예 없는 경우 continue ; 다 돌고 common 으로 끝
                else {

                    // 탐색 결과 존재 하지 않으면, continue를 통해 다음 idx로 넘기자
                    logger.info(" >> 34-3. No Item defined : {} ", i);
                    continue;
                }

                String topicVal = this.getTypeIdParsingRule(ruleDto, parsingTargetItem);

                /**
                 * 2. 아이템 값이 null 이거나 없는 경우는 다음 룰로,
                 *  아이템 값이 있는 경우, Position 설정에 따라 파싱 값 가져옴
                 **/
                if(!SequenceManageUtil.isStringNull(parsingTargetItem)){
                    return this.getLastValue(parsingTargetItem, topicVal, ruleDto);
                }
            }
        }


        // Parsing Item 값이 존재하지 않으면, targetSystem + CMN/00 로 종료
        logger.warn("Rule has been registered but not fit for this case.");
        return this.basicSequenceRule();

    }


    /**
     * 획득한 Key를 통해서 분배에 필요한 토픽의 값을 생성
     * @param parsingTargetItem: 메시지 전문에서 파싱할 대상
     * @param ruleDto
     * @return
     */
    private String getLastValue(String parsingTargetItem, String topicVal, SequenceRuleDto ruleDto){

        logger.info(" >> 35-2. parsing key values : " );
        topicVal = getTypeIdParsingRule(ruleDto, parsingTargetItem);
        logger.info("################################# rule : "+ruleDto.toString());

        if(ruleDto.getPosition() == null || ruleDto.getPosition().equals("")){
            return topicVal.concat(parsePosFromParsingItem(parsingTargetItem,parsingTargetItem.length()-1));

        }else {
            String position = ruleDto.getPosition();

            // Case1. 포지션 설정이 한 자리 수 일 때.
            if(position.length() < 2){

                // Case1-1. 포지션이 Key 길이 보다 작을 때 (정상)
                if(this.validatePositionSize(parsingTargetItem, position)){
                    return topicVal.concat(parsePosFromParsingItem(parsingTargetItem, Integer.valueOf(position)));

                }

                // Case1-2. 포지션이 Key 길이 보다 클 때 (비 정상)
                // ex) key: S1012 / key.length: 5, position: 6 인 경우
                else{
                    return topicVal.concat(parsePosFromParsingItem(parsingTargetItem, parsingTargetItem.length()-1));

                }
                // Case2. 포지션 설정이 여러 자리 수 일 때.
            }else{

                return topicVal.concat(convertEqpIdValue(parsingTargetItem, position));
            }

        }

    }

    /**
     *
     * @param key
     * @param position
     * @return
     */
    private boolean validatePositionSize(String key, String position){
        return (Integer.valueOf(position) ) < key.length();
    }

    private String getTypeIdParsingRule(SequenceRuleDto ruleDto, String parsingItem ) {
        String type = "";
        String topicVal = null;
        /**
         * 2.3.2 해당 Record의 ⑤ Position, ⑥ Item Name 으로 처리
         * > ItemName( := type ) check 4 depth
         **/

        // 타입을 설정한다.
        if ( ruleDto.getType() != null ) {
            type = ruleDto.getType();
            logger.info("2. topicVal "+topicVal);
            topicVal = type.concat("/");

        } else {
            // TODO EQP외 대응 (CARR, LOT, CMN)
            topicVal = parseFromParsingItemName(parsingItem).concat("/");
            logger.info("3. topicVal "+topicVal);
        }
        return topicVal;
    }


    /**
     *
     * @param payload
     * @return
     */
    public String executeEAPParsingRule(String key, JSONObject payload){

        String topicVal= SeqCommonCode.CMN.name() + "/";
        JSONObject bodyObj = payload.getJSONObject(PayloadCommonCode.body.name());
        return topicVal.concat(bodyObj.getString(SeqCommonCode.eqpId.name()));
    }

    @Deprecated
    public String basicSequenceRule(){

        // TODO Util에 getCommonTopic 혹은 getCommonDefaultTopic으로 대응
        return  SeqCommonCode.CMN.name() + "/00";
    }

    @Deprecated
    private String convertEqpIdintoAsciiValue(String eqpId){
        logger.info(">>>  executeEventRule param eqpId : "+eqpId);
        logger.info(">>>  executeEventRule maxQueueSize : "+maxQueueSize);

        int val = (eqpId.charAt(1)+ (int) eqpId.charAt(3) + (int) eqpId.charAt(4) + (int) eqpId.charAt(7)) % maxQueueSize;

        logger.info("## executeEventRule val : "+val);
        return String.format("%02d", val);

    }

    //
    // ④ Parsing Item으로 파싱된 값을 ⑥ Item Name 에 따라 처리

    /**
     * Make EQP ID into topic value
     * ex) AT-AT-05 > make value with position > 'T'/'A'/'T'/'5'
     //	 * @param key
     * @param parsItem
     * @param position
     * @return
     */
    private String convertEqpIdValue(String parsItem, String position){
        String[] positions = position.split(",");
        int rst = 0;
        for (String pos : positions) {
            if ( Integer.valueOf(pos)-1 < parsItem.length() )
                rst += parsItem.charAt(Integer.valueOf(pos)-1);
            else
                break;
        }
        logger.info("## check params : parsItem "+parsItem);
        logger.info("## check params : postion "+position);
        logger.info("## check value : rst "+rst);
        logger.info("## check value :  rst % maxQueueSize "+rst%maxQueueSize);
        String id = String.valueOf(rst%maxQueueSize);
        if (id.length() < 2) {
            logger.info("## check value : one length id - "+"0"+id);
            logger.info("String.format(\"%2d\", rst%maxQueueSize): "+String.format("%2d", rst%maxQueueSize));
            return "0"+id;
        } else {
            logger.info("## check value : 2char id "+id);
            return id;
        }
    }
    /**
     * position 1자리로 무조건 2자리 파싱해서 return 하기!
     */
    private String parsePosFromParsingItem (String key, int position) {

        logger.info("## itemValue "+key);
        logger.info("## itemValue substring (return) "+key.substring(position-1, position+1));

        String originPosValue = "";
        int res = 0;

        logger.info("@@ parsePosFromParsingItem(), position:{} , key.length():{} ", position, key.substring(position-1, position+1));
        originPosValue = key.substring(position-1, position+1);
        logger.info("## originPosValue :: "+originPosValue);
        try {
            res = Integer.valueOf(originPosValue) % maxQueueSize;
        } catch (Exception e) {
            res = 0;
        }

        return String.format("%02d", res);

    }


    private String parseFromParsingItemName(String parsingItem) {
        // 2.3.2 ⑥ Item Name(값이 없으면 ④ Parsing Item 앞4자리를 잘라 대문자 변환
        // CARR외 3자리가 LOT, EQP 가 아니면 CMN으로 처리)
        String id = null;
        if ( parsingItem.substring(0).equals("c") ) {
            id = parsingItem.substring(0, 4).toUpperCase();
            logger.info("## parseFromParsingItemName() 1. id : "+ id);
        } else {
            id = parsingItem.substring(0, 3).toUpperCase();
            logger.info("## parseFromParsingItemName() 2. id : "+ id);
            if ( !id.equals(SeqCommonCode.EQP.name()) && !id.equals(SeqCommonCode.LOT.name()) ) id = "CMM";
        }

        return id.toUpperCase();
    }
    private String parseParsingItemDepth(String parsingItem, JSONObject bodyObj) {

        // 파싱 item depth 를 나눔   ex) [0] carridList // [1] carrid
        String[] item = parsingItem.split("/");
        logger.info("## executeParsingRule - parsingItem depth process");
        String key = null;
        if ( bodyObj.has(item[0]) ) {
            JSONObject obj = null;
            logger.info("## parseParsingItemDepth() bodyObj.getJSONArray(item[0]).length() : "+bodyObj.getJSONArray(item[0]).length());
            for ( int i = 0 ; i < bodyObj.getJSONArray(item[0]).length() ; i++ ) {
                if ( bodyObj.getJSONArray(item[0]).get(i) != null ) {
                    obj = (JSONObject) bodyObj.getJSONArray(item[0]).get(i);
                    logger.info("@@ SequecneRuleExcutor, parseParsingItemDepth() : "+obj.toString());
                    if ( obj.toString().indexOf(item[1]) > -1 )
                        key = obj.getString(item[1]);
                    else
                        continue;
                    if ( key != null && !key.equals("") ) {
                        logger.info("## executeParsingRule, parseParsingItemDepth() - key : " + key);
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


    private boolean isDepthCase(String element){
        return element.indexOf("/") != -1;
    }



}
