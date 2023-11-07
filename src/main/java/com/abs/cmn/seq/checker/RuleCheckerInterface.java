package com.abs.cmn.seq.checker;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public interface RuleCheckerInterface<T> {

    ConcurrentHashMap<String, T> setSequenceData(JSONObject ruleObj);

    boolean sequenceDataBackUp();
    boolean sequenceDataReload(JSONObject ruleObject);

    // JDK8 Private 으로 interface 선언 못함...
     // private void generateRuleDataMap(ConcurrentHashMap<String, T> dataMap, JSONObject ruleObj );
}
