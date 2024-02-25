package com.abs.cmn.seq.wfs;

import com.abs.cmn.seq.SequenceManager;
import com.abs.cmn.seq.SequenceManagerTest;
import com.abs.cmn.seq.util.SeqTestUtil;
import com.abs.cmn.seq.util.SequenceManageUtil;
import com.abs.cmn.seq.util.code.SeqTestConstant;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

import java.io.IOException;

public class WfsSequenceTest {

    public static void main(String[] args) throws Exception {
        String A = "CAR";

        if(A.startsWith("c") || A.startsWith("C")){
            if(A.length() > 4){
                String type = A.substring(0,4);
            }else{
                logger.error("{} Pasing item is invalid.");
                throw new Exception("PARSING ITEM IS INVALID");

            }
        }

    }

    private final static Logger logger = LoggerFactory.getLogger(SequenceManagerTest.class);

    private static final String SYSTEM = "WFS";

    private String SITE;
    private String ENV;

    private SequenceManager sequenceManager;

    private JSONObject registeredTestItems;

    @Before
    public void setUpSequenceManager() throws IOException {



        JSONObject testConfObject = new JSONObject(
                SequenceManageUtil.convertToString(SequenceManageUtil.getFileInResource(SeqTestConstant.TEST_CONF_NAME))
        );
        this.SITE = testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_SITE);
        this.ENV = testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_EVN);

        logger.info(testConfObject.toString());


        this.sequenceManager = new SequenceManager(SYSTEM,
                testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_SITE),
                testConfObject.getString(SeqTestConstant.TEST_CONF_KEY_EVN),
                testConfObject.getJSONObject(SeqTestConstant.TEST_CONF_KEY_FILE).getString(SeqTestConstant.TEST_CONF_KEY_FILE_PATH),
                testConfObject.getJSONObject(SeqTestConstant.TEST_CONF_KEY_FILE).getString(SeqTestConstant.TEST_CONF_KEY_FILE_RULE_NAME)
        );

        this.registeredTestItems = new JSONObject(
                SequenceManageUtil.convertToString(
                        SequenceManageUtil.getFileInResource(
                                testConfObject.getJSONObject(SeqTestConstant.TEST_CONF_KEY_FILE)
                                        .getString(SeqTestConstant.TEST_CONF_KEY_FILE_TEST_NAME)
                        )
                )
        ).getJSONObject(SYSTEM);



    }


    @Test
    public void testAddition(){


        for (String targetSystem : this.registeredTestItems.keySet()){
            JSONObject itemEachTarget = this.registeredTestItems.getJSONObject(targetSystem);
            int registeredItemCount = itemEachTarget.keySet().size();
            if(!(registeredItemCount > 0)){
                logger.info("Nothing is register to test for from {} to this system: {}",SYSTEM,  targetSystem);

            }else{

                for (String eventName : itemEachTarget.keySet()){

                    JSONObject testItem = itemEachTarget.getJSONObject(eventName);
                    String returnedTopic = this.sequenceManager.getTargetName(targetSystem, eventName,
                            testItem.getJSONObject(SeqTestConstant.TEST_PAYLOAD).toString()
                    );
                    String answerTopic = SeqTestUtil.generateTopicAnswer(
                            SITE, ENV, targetSystem,testItem.getJSONObject(SeqTestConstant.TEST_ANSWER)
                    );
                    assertEquals(answerTopic, returnedTopic);
                }

            }

        }
    }

    @Test
    public void EqpIdIsNotRegisteredCase(){

        String testMsg = "{\"head\": {\n" +
                "    \"tgt\": \"EAP\",\n" +
                "    \"tgtEqp\": [\n" +
                "      \"AP-PD-09-02\"\n" +
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
                "    \"key\": \"\\/rms\\/EAP_RECIPE_LIST_REQ\\/20240223090635767~mckim~AP-PD-09-02_PARA~null~RECIPE_LIST\"\n" +
                "  }\n" +
                "}";

        assertEquals("SVM/DEV/EAP/CMN/AP-PD-09-02", this.sequenceManager.getTargetName("EAP", "AAA", testMsg));

    }

}
