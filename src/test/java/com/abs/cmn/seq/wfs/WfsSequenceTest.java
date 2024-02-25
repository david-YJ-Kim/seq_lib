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

}
