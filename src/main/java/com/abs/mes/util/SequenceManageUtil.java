package com.abs.mes.util;

import java.util.UUID;

public class SequenceManageUtil {

    public static String generateMessageID(){

        String randomeUUIDString = UUID.randomUUID().toString();
        return System.currentTimeMillis() + "-" + randomeUUIDString;

    }

}
