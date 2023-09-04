package com.abs.mes;

public final class SequenceManager {


    public static void main(String[] args) {
        System.out.println(
                SequenceManager.getTargetName("WFS")
        );
    }

    public static String getTargetName(String targetSystem, String eqpId, String carrId, String lotId, String cid, String payload){
        return getTargetName(targetSystem);
    }

    public static String getTargetName(String targetSystem){
        return "SVM/DEV/" + targetSystem + "/CMN/00";
    }
}
