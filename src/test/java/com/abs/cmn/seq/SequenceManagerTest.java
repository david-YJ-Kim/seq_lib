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

        //JSONObject test = testObj.getJSONObject(TEST_CODE.WFS_CARR_ID_READ.name());


        SequenceManager sequenceManager = new SequenceManager(sourceSystem, site, env, ruleFilesPath, sequenceRuleFileName);
        String topicResult = sequenceManager.getTargetName(
//                                test.getString(COMMON_CODE.tgt.name()), // target 시스템 코드 (e,x) WFS, BRS, RTD, ...)
        						"EAP",
        						"WFS_CARR_ID_READ",
                                //test.getString(COMMON_CODE.cid.name()), // 이벤트 명 (ex) WFS_CARR_ID_READ)
//                                test.getJSONObject(COMMON_CODE.payload.name()).toString() // 메시지 전문
                                "{\r\n" + 
                                "      \"head\": {\r\n" + 
                                "        \"cid\": \"WFS_CARR_ID_READ\",\r\n" + 
                                "        \"tid\": \"202303030000000\",\r\n" + 
                                "        \"osrc\": \"\",\r\n" + 
                                "        \"otgt\": \"\",\r\n" + 
                                "        \"src\": \"EAP\",\r\n" + 
                                "        \"srcEqp\": \"\",\r\n" + 
                                "        \"tgt\": \"WFS\",\r\n" + 
                                "        \"tgtEqp\": \"\"\r\n" + 
                                "      },\r\n" + 
                                "      \"body\": {\r\n" + 
                                "        \"siteId\": \"SVM\",\r\n" + 
                                "        \"eqpId\": \"AP-TG-01\",\r\n" + 
                                "        \"portId\": \"AP-TG-01_IP01\",\r\n" + 
                                "        \"portType\": \"IP\",\r\n" + 
                                "        \"carrId\": \"CST_23222023\",\r\n" + 
                                "        \"slotMap\": \"1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1\",\r\n" + 
                                "        \"userId\": \"WFS\"\r\n" + 
                                "      }\r\n" + 
                                "    }"
                        );

        System.out.println(
                topicResult
        );
    }
}
