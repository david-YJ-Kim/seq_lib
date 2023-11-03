package com.abs.cmn.seq;


import com.abs.cmn.seq.util.SequenceManageUtil;
import org.json.JSONObject;

import java.io.IOException;

enum TEST_CODE{
    INOUT_EAP_0001,
    INOUT_EAP_0002, 
    INOUT_EAP_0003,
    WFS_CARR_ID_READ;
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

        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
//        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLibrary\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule.json";
        String sequenceRuleFileTestName = "SequenceRuleTest.json";

        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));

        JSONObject test = testObj.getJSONObject("INOUT_MSG_008");


        SequenceManager sequenceManager = new SequenceManager(sourceSystem, site, env, ruleFilesPath, sequenceRuleFileName);
        String topicResult = sequenceManager.getTargetName(
                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
                                test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
                        );

        System.out.println(
                topicResult
        );
    }
}
