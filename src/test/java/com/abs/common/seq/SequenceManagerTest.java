package com.abs.common.seq;


import com.abs.common.seq.util.SequenceManageUtil;
import org.json.JSONObject;

import java.io.IOException;

enum TEST_CODE{
    INOUT_EAP_0001,
    INOUT_EAP_0002, 
    INOUT_EAP_0003;
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

        String sourceSystem = "TST"; // Property
        String site = "SMV"; // Property
        String env = "DEV"; // Property

//        String ruleFilesPath = "C:\\Workspace\\abs\\cmn\\seq-library\\src\\main\\resources\\";
        String ruleFilesPath = "D:\\work-spaces\\work-space-3.9.11\\SEQLib_dv\\src\\main\\resources\\";
        String sequenceRuleFileName = "SequenceRule.json";
        String sequenceRuleFileTestName = "SequenceRuleTest.json";

        JSONObject testObj = new JSONObject(SequenceManageUtil.readFile(ruleFilesPath.concat(sequenceRuleFileTestName)));

        JSONObject test = testObj.getJSONObject(TEST_CODE.INOUT_EAP_0003.name());


        SequenceManager sequenceManager = new SequenceManager(sourceSystem, site, env, ruleFilesPath, sequenceRuleFileName);
        String result = sequenceManager.getTargetName(
                                test.getString(COMMON_CODE.tgt.name()),
                                test.getString(COMMON_CODE.cid.name()),
                                test.getJSONObject(COMMON_CODE.payload.name()).toString()
                        );

        System.out.println(
                result
        );
    }
}
