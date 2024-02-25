package com.abs.cmn.seq.util;

import com.abs.cmn.seq.util.code.SeqTestConstant;
import org.json.JSONObject;

public class SeqTestUtil {

    public static String generateTopicAnswer(String site, String env, String targetSystem, JSONObject answer){

        String answerFormat = "%s/%s/%s/%s/%s";
        return String.format(answerFormat, site, env, targetSystem,
                answer.getString(SeqTestConstant.TEST_ANSWER_TYPE),
                answer.getString(SeqTestConstant.TEST_ANSWER_VALUE)
        );

    }
}
