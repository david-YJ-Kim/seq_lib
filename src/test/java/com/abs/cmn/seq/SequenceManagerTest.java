package com.abs.cmn.seq;

import com.abs.cmn.seq.util.SequenceManageUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum COMMON_CODE{
    cid,
    tgt,
    payload,
    type,
    topic;
}

enum SOURCE_SYSTEM{
	TST,
	MCS,
	RTD,
	EAP,
	RMS,
	MSS,
	CRS,
	SPC,
	WFS,
	BRS,
	OIA	
}

// 테스트 자동화를 위한 값
enum RESULT_TOPIC {
	topic,
	tstTopic,
	mcsTopic,
	rtdTopic,
	eapTopic,
    rmsTopic,
    mssTopic,
    crsTopic,
    spcTopic,
    wfsTopic,
    brsTopic,
    oiaTopic
	;
}

public class SequenceManagerTest {

    public static void main(String[] args) throws IOException {

        String sourceSystem = SOURCE_SYSTEM.OIA.name(); // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

//        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLibrary\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule-TEST.json";
        String sequenceRuleFileTestName = "SequenceMessageRuleTest.json";
//        String sequenceRuleFileName = "SequenceRule.json";
//        String sequenceRuleFileTestName = "SequenceRuleTest.json";

        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));

        
        SequenceManager sequenceManager = new SequenceManager(sourceSystem, site, env, ruleFilesPath, sequenceRuleFileName);
        JSONObject test;
        String topicResult;
        
        List<Map<String,String>> testResult = new ArrayList<Map<String, String>>();
        Map<String,String> testCase = null;
        
        for (TEST_CODE value : TEST_CODE.values()) {
//        for ( int i =0 ; i < TEST_CODE.values().length ; i ++)
        
	        test = testObj.getJSONObject(value.name());
	        
	        topicResult = sequenceManager.getTargetName(
	                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
	                                test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
	                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
//	        		"WFS",		
//	        		"WFS_DSP_WORK_REP",
//					"{\r\n" + 
//					"      \"head\": {\r\n" + 
//					"        \"cid\": \"WFS_DSP_WORK_REP\",\r\n" + 
//					"        \"tid\": \"202303030000000\",\r\n" + 
//					"        \"osrc\": \"\",\r\n" + 
//					"        \"otgt\": \"\",\r\n" + 
//					"        \"src\": \"RTD\",\r\n" + 
//					"        \"srcEqp\": \"\",\r\n" + 
//					"        \"tgt\": \"WFS\",\r\n" + 
//					"        \"tgtEqp\": \"\"\r\n" + 
//					"      },\r\n" + 
//					"      \"body\": {\r\n" + 
//					"        \"siteId\": \"SVM\",\r\n" + 
//					"        \"dspType\": \"LOT\",\r\n" + 
//					"        \"lotId\": \"LOT_230327005\",\r\n" + 
//					"	\"lot\":[\r\n" + 
//					"	  {\"carrId\": \"CAA00128\"},\r\n" + 
//					"          {\"LotId\": \"LOT_230327005\"},\r\n" + 
//					"	  { \"eqpId\": \"AP-TG-01T\" }\r\n" + 
//					"	],\r\n" + 
//					"        \"eqpId\": \"AP-TG-01T\",\r\n" + 
//					"        \"portId\": \"AP-TG-01_IP01,AP-TG-01_OP01\",\r\n" + 
//					"        \"carrId\": \"\",\r\n" + 
//					"        \"prodDefId\": \"\",\r\n" + 
//					"        \"procDefId\": \"\",\r\n" + 
//					"        \"rsltCm\": \"\",\r\n" + 
//					"        \"rsnCd\": \"PASS\",\r\n" + 
//					"        \"evntCm\": \"\",\r\n" + 
//					"        \"evntUserId\": \"WFS\"\r\n" + 
//					"      }\r\n" + 
//					"}"
					);
	        
	        System.out.println(
	                " @@@  > "+value.name()+" : "+topicResult
	        );
	        testCase = new HashMap<String,String>();
	        testCase.put(test.getString(COMMON_CODE.cid.name()), topicResult);
	        testResult.add(testCase);
	        
        }
        System.out.println("**"+sourceSystem);
        int cnt = 0;
        for ( Map<String, String> map :  testResult ) {
        	for(String key : map.keySet() ) {        	
	        	System.out.println(
	        			"[ "+(cnt+1)+" ]"+
	        			key+" : "+map.get(key)
				);
        	}
        	cnt++;
        }
        
        System.exit(0);
    }
    
}
