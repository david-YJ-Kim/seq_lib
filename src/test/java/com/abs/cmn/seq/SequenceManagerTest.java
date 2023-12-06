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


public class SequenceManagerTest {

    public static void main(String[] args) throws IOException {

        String sourceSystem = SOURCE_SYSTEM.OIA.name(); // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

        String ruleFilesPath = "C:\\codespace\\abs\\cmn\\cmn.seq\\src\\main\\resources\\";
//        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLibrary\\src\\main\\resources\\";
//        String ruleFilesPath = "D:\\work-spaces\\new-git-storage\\seq\\src\\main\\resources\\";
        
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
        
	        test = testObj.getJSONObject(value.name());
	        
	        topicResult = sequenceManager.getTargetName(
	                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
	                                test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
	                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
					);
	        
	        System.out.println(
	                " @@@  > "+value.name()+" : "+topicResult
	        );
	        testCase = new HashMap<String,String>();
	        testCase.put(test.getString(COMMON_CODE.cid.name()), topicResult);
	        // TODO test.getString("cmsTopic"); 등등의 메세지 제일 아래 값을 같이 넣어줌, 
	        testCase.put(sourceSystem.toLowerCase(), test.getString(sourceSystem).toLowerCase());
	        testCase.put("result" , test.getString("answer") == topicResult ? "true" : "false");
	        testResult.add(testCase);
	        
        }
        
        System.out.println("**"+sourceSystem);
        
        // 현재 파싱 결과 값 & 해당 SOURCE 일 때 예상 결과값 같이 출력됨
        // 사전에 Message 폼에는 정상적인 룰로 돌린 Topic 값들을 넣어주어야 함. 그럼 추후 결과값으로 true false 확인 가능 .
        int cnt = 0;
        for ( Map<String, String> map :  testResult ) {
        	System.out.printf( "[ "+(cnt+1)+" ]\t");
        	for(String key : map.keySet() ) {        	
	        	System.out.printf(
	        			key+" : "+map.get(key)+" \t"
				);
        	}
        	System.out.println();
        	cnt++;
        }
        
        System.exit(0);
    }
    
}
