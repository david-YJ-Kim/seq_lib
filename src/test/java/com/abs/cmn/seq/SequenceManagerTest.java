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
        String testMsg = "{\"head\":{\"cid\":\"BRS_ALARM_EXECUTE\",\"tid\":\"SPC_20241015090731_ed89123c-a3cf-4998-9de9-24b3a5a91951\",\"src\":\"SPC\",\"tgt\":\"BRS\",\"osrc\":\"\",\"otgt\":\"\",\"srcEqp\":\"\",\"tgtEqp\":[\"\"]},\"body\":{\"alarmCm\":\" Upper Spec Limit 400.0 OUT\",\"alarmDefId\":\"AS-T0001\",\"carrId\":\"\",\"eqpId\":\"AP-TG-08-01\",\"lotId\":\"1\",\"mtrlId\":\"1\",\"siteId\":\"SVM\",\"rsnCd\":\"\",\"trnsCm\":\"\",\"mdfyUserId\":\"SPC\"}}";
//
////        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));
//        JSONObject testObj = new JSONObject(testMsg);


        SequenceManager sequenceManager = new SequenceManager("SPC", "SVM", "PROD", "C:\\Workspace\\Common\\seq\\src\\main\\resources\\", "SequenceRule.json");
        JSONObject test;
        String topicResult;

        System.out.println(sequenceManager.getTargetName("BRS","BRS_ALARM_EXECUTE", testMsg));

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
