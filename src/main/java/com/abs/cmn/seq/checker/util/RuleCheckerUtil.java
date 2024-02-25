package com.abs.cmn.seq.checker.util;

import com.abs.cmn.seq.checker.code.CheckerCommonCode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class RuleCheckerUtil {

    public static final Logger logger = LoggerFactory.getLogger(RuleCheckerUtil.class);

    public void saveHistory(String ruleKey, JSONObject ruleObject, ConcurrentHashMap<String, JSONObject> ruleDataHistoryMap, ArrayList<String> ruleHistoryKeySequenceList){
        ruleDataHistoryMap.put(ruleKey, ruleObject);
        ruleHistoryKeySequenceList.add(ruleKey);
    }

    public void deleteHistory(String ruleKey, ConcurrentHashMap<String, JSONObject> ruleDataHistoryMap, ArrayList<String> ruleHistoryKeySequenceList){
        if(ruleKey.equals(CheckerCommonCode.INIT.name())){
            logger.error("No permit to delete Initialize data.");
            return;
        }

        if(ruleDataHistoryMap.contains(ruleKey)){
            ruleDataHistoryMap.remove(ruleKey);
        }

        if(ruleHistoryKeySequenceList.contains(ruleKey)){
            ruleHistoryKeySequenceList.remove(ruleKey);
        }
    }
}
