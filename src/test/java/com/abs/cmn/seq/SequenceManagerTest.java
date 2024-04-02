package com.abs.cmn.seq;

import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.util.code.SeqTestConstant;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class SequenceManagerTest {

    private final static Logger logger = LoggerFactory.getLogger(SequenceManagerTest.class);

    private static final String sourceSystem = "EAP";

    private SequenceManager sequenceManager;

    @Before
    public void setUpSequenceManager() throws IOException {



        JSONObject testConfObject = new JSONObject(
                SequenceManageUtil.convertToString(SequenceManageUtil.getFileInResource(SeqTestConstant.TEST_CONF_NAME))
        );

        logger.info(testConfObject.toString());


        this.sequenceManager = new SequenceManager(sourceSystem,
                testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_SITE),
                testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_EVN),
                testConfObject.getJSONObject(SeqTestConstant.TEST_CONF_KEY_FILE).getString(SeqTestConstant.TEST_CONF_KEY_FILE_PATH),
                testConfObject.getJSONObject(SeqTestConstant.TEST_CONF_KEY_FILE).getString(SeqTestConstant.TEST_CONF_KEY_FILE_RULE_NAME)
        );


    }


    @Test
    public void testAddition(){

        logger.info(this.sequenceManager.toString());

    }

    public static void main(String[] args) throws IOException {

//        JSONObject testConfObject = new JSONObject(
//                SequenceManageUtil.convertToString(SequenceManageUtil.getFileInResource(SeqTestConstant.TEST_CONF_NAME))
//        );
//        logger.info(testConfObject.toString());
//
//
//        String sourceSystem = SOURCE_SYSTEM.EAP.name(); // Property
//        String site = "SVM"; // Property
//        String env = "DEV"; // Property
//
//
//        String sequenceRuleFileName = "SequenceRule.json";
//        String sequenceRuleFileTestName = "SequenceMessageRuleTest_BakUp.json";
//
        String testMsg = "{\n" +
                "  \"head\": {\n" +
                "    \"tgt\": \"RMS\",\n" +
                "    \"tgtEqp\": [\n" +
                "      \n" +
                "    ],\n" +
                "    \"osrc\": \"\",\n" +
                "    \"srcEqp\": \"AP-PD-09-02\",\n" +
                "    \"src\": \"EAP\",\n" +
                "    \"otgt\": \"\",\n" +
                "    \"tid\": \"AP-PD-09-02_00_20240223090636937\",\n" +
                "    \"cid\": \"RMS_RECIPE_LIST_REP\"\n" +
                "  },\n" +
                "  \"body\": {\n" +
                "    \"msg\": \"000007140000000100140130411323315F3430756D5F546163745F323330393035411323325F3430756D5F546163745F323330393035411323335F3430756D5F546163745F323330393035411323345F3430756D5F546163745F323330393035410D32303233303733315F5465737441153233313230355F444354565F416C69676E6D656E7441163233313230355F444354565F50726F63657373696E674111343830783438304D6174726978544553544111353130783531344D61747269785445535441064452335243504110526976656E64656C6C5F6431302D31314110526976656E64656C6C5F6431312D3132410E526976656E64656C6C5F64352D36410E526976656E64656C6C5F64362D374108546573745F303031410D546573745F3030315F4C696E654108546573745F303032410D546573745F3030325F4C696E654108546573745F303033410C546573745F35313078353135410A546573745F416C69676E410C546573745F416C69676E2D31410C546573745F416C69676E2D32410C546573745F416C69676E2D334110546573745F416C69676E2D335F73656C4112546573745F416C69676E2D335F73656C202D4110546573745F416C69676E2D345F73656C4113546573745F416C69676E2D345F73656C5F54544114546573745F416C69676E2D6E6F5F566973696F6E4116546573745F416C69676E2D6E6F5F566973696F6E2D314116546573745F416C69676E2D6E6F5F566973696F6E2D324113546573745F4C43485F3030315F3232323730344113546573745F4C43485F3030325F3536353337364113546573745F4C43485F3030335F3831333932304114546573745F4C43485F3030345F313031303831364111546573745F4C43485F3030355F5556434F4111546573745F4C43485F3030365F444354564112546573745F4C43485F3030375F4443545632411C546573745F4C43485F3530397835313420544553545F3233303930344114546573745F4C43485F444354565F3233303930314120546573745F4C43485F444354565F325468696E5F736B6976655F323330393137411F546573745F4C43485F444354565F325468696E5F566961735F323330393137411A546573745F4C43485F444354565F736B6976655F3233303930344119546573745F4C43485F444354565F544553545F3233303931394119546573745F4C43485F444354565F566961735F3233303930344117546573745F4C43485F747261636B696E67206572726F724116546573745F5556434F5F736B6976655F3233303930354115546573745F5556434F5F766961735F323330393035\",\n" +
                "    \"siteId\": \"SVM\",\n" +
                "    \"eqpTime\": \"20240223090636\",\n" +
                "    \"eqpId\": \"AP-PD-09-02\",\n" +
                "    \"key\": \"\\/rms\\/EAP_RECIPE_LIST_REQ\\/20240223090635767~mckim~AP-PD-09-02_PARA~null~RECIPE_LIST\"\n" +
                "  }\n" +
                "}";
//
////        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));
//        JSONObject testObj = new JSONObject(testMsg);


        SequenceManager sequenceManager = new SequenceManager("EAP", "SVM", "PROD", "C:\\codespace\\abs\\cmn\\cmn.seq\\src\\main\\resources\\", "SequenceRule.json");
        JSONObject test;
        String topicResult;

        System.out.println(sequenceManager.getTargetName("RMS","RMS_RECIPE_LIST_REP", testMsg));

//        List<Map<String,String>> testResult = new ArrayList<Map<String, String>>();
//        Map<String,String> testCase = new HashMap();

//        for (TEST_CODE value : TEST_CODE.values()) {
//
//	        test = testObj.getJSONObject(value.name());
//
//	        topicResult = sequenceManager.getTargetName(
//	                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
//	                                test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
//	                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
//					);
//
//	        System.out.println(
//	                " @@@  > "+value.name()+" : "+topicResult
//	        );
//	        testCase.put(test.getString(COMMON_CODE.cid.name()), topicResult);
//	        // TODO test.getString("cmsTopic"); 등등의 메세지 제일 아래 값을 같이 넣어줌,
////	        testCase.put(sourceSystem.toLowerCase(), test.getString(sourceSystem).toLowerCase());
//
//
////	        testCase.put("result" , test.getString("answer") == topicResult ? "true" : "false");
////	        testResult.add(testCase);
//
//        }
//		System.out.println(testCase.toString());
//
//        System.out.println("**"+sourceSystem);
//
//        // 현재 파싱 결과 값 & 해당 SOURCE 일 때 예상 결과값 같이 출력됨
//        // 사전에 Message 폼에는 정상적인 룰로 돌린 Topic 값들을 넣어주어야 함. 그럼 추후 결과값으로 true false 확인 가능 .
//        int cnt = 0;
//        for ( Map<String, String> map :  testResult ) {
//        	System.out.printf( "[ "+(cnt+1)+" ]\t");
//        	for(String key : map.keySet() ) {
//	        	System.out.printf(
//	        			key+" : "+map.get(key)+" \t"
//				);
//        	}
//        	System.out.println();
//        	cnt++;
//        }

        System.exit(0);
    }

}
