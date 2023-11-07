package com.abs.cmn.seq.checker;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public interface RuleCheckerInterface<T> {

    ConcurrentHashMap<String, T> setSequenceData(JSONObject ruleObj);

    boolean sequenceDataBackUp();
    boolean sequenceDataReload(JSONObject ruleObject);
}
