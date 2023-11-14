package com.abs.cmn.seq;


import com.abs.cmn.seq.util.SequenceManageUtil;
import org.json.JSONObject;

import java.io.IOException;

enum TEST_CODE{
    INOUT_BRS_0001,
//    INOUT_EAP_0002, 
//    INOUT_EAP_0003,
//    INOUT_EAP_0004,
//    INOUT_MSG_005,
//    INOUT_MSG_006,
//    INOUT_MSG_007,
//    INOUT_MSG_008,
//    INOUT_MSG_009,
//    WFS_CARR_SLOTMAP_REPORT_REQ,
//    WFS_CARR_ID_READ,
//	BRS_CARR_HOLD,
//	BRS_CARR_HOLD_RELEASE,
//	BRS_CARR_STATE_CHANGE_CLEAN,
//	BRS_CARR_STATE_CHANGE_DIRTY,
//	BRS_LOT_TRACK_IN_CNFM_REP,
//	WFS_LOAD_REQ,
//	WFS_DSP_WORK_REP,
//	WFS_TOOL_COND_REP,
//	WFS_LOT_TRACK_IN_CNFM_REP,
//	WFS_RECIPE_VALIDATE_REP,
//	WFS_CARR_MOVE_REP,
//	WFS_CARR_MOVE_COMP,
//	WFS_LOAD_COMP,
//	BRS_ALARM_EXECUTE
	;
}

enum COMMON_CODE{
    cid,
    tgt,
    payload,
    type,
    topic;
}

public class SequenceManagerTest {

    public static void main(String[] args) throws IOException {

        String sourceSystem = "RTD"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

//        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLibrary\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule.json";
        String sequenceRuleFileTestName = "SequenceRuleTest.json";

        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));

        
        SequenceManager sequenceManager = new SequenceManager(sourceSystem, site, env, ruleFilesPath, sequenceRuleFileName);
        JSONObject test;
        String topicResult;
        
        for (TEST_CODE value : TEST_CODE.values()) {
//        for ( int i =0 ; i < TEST_CODE.values().length ; i ++)
        
	        test = testObj.getJSONObject(value.name());
	        
	        topicResult = sequenceManager.getTargetName(
	                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
	                                test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
	                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
	                        );
	
	        System.out.println(
	                " @@@  > "+value.name()+" : "+topicResult
	        );
        }
        
    }
}
