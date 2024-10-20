package com.abs.cmn.seq.util;

import com.abs.cmn.seq.checker.code.CheckerCommonCode;
import com.abs.cmn.seq.code.PayloadCommonCode;
import com.abs.cmn.seq.code.SeqCommonCode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

public class SequenceManageUtil {

    public static Logger logger = LoggerFactory.getLogger(SequenceManageUtil.class);



    public static InputStream getFileInResource(String filePathWithName){

        return SequenceManageUtil.class.getClassLoader().getResourceAsStream(filePathWithName);

    }
    public static String generateMessageID(){

        String randomeUUIDString = UUID.randomUUID().toString();
        return System.currentTimeMillis() + "-" + randomeUUIDString;

    }

    public static String readFile(String filePath) throws IOException, FileNotFoundException {
        // 파일의 입력 스트림을 가져옵니다.
        FileInputStream inputStream = new FileInputStream(filePath);

        // 입력 스트림을 문자 스트림으로 변환합니다.
        Reader reader = new InputStreamReader(inputStream, "UTF-8");

        // 파일의 내용을 읽습니다.
        StringBuffer buffer = new StringBuffer();
        int c;
        while ((c = reader.read()) != -1) {
            buffer.append((char) c);
        }

        // 입력 스트림을 닫습니다.
        inputStream.close();

        return buffer.toString();
    }

    public static String getTargetSystem(String eventName){
        int indexOfUnderscore = eventName.indexOf("_");
        if(indexOfUnderscore != -1){
            String result = eventName.substring(0, indexOfUnderscore);
            return result;
        }else{
            System.err.println(
                    "Underscores not found in the string."
            );
            throw new NullPointerException("Underscores not found in the string.");
        }
    }

    public static boolean validString(String element){
        return element != null && !element.isEmpty();
    }

    public static String getCommonDefaultTopic(String key, String targetSystem){
        logger.info("{}: {}, targetSystem: {}"
                ,key
                ,"This event has benn return in common topic value."
                , targetSystem
        );
        return targetSystem + "/" + SequenceManageUtil.getCommonTopic("00");
    }
    public static String getCommonTopic(String seq){
        return SeqCommonCode.CMN.name() + "/" + seq;
    }

    public static String generateErcKey(){
        return SequenceManageUtil.generateCheckerKey(CheckerCommonCode.ERC.name());
    }

    public static String generatePrcKey(){
        return SequenceManageUtil.generateCheckerKey(CheckerCommonCode.PRC.name());
    }

    public static String generateCheckerKey(String prefix){
        return prefix + System.currentTimeMillis();
    }

    public static String getTargetNameFromHeader(String key, JSONObject payload) {

        logger.info("{}: {}" +
                        "payload: {}."
                , key ,"Get target system name from payload header."
                , payload
        );

        JSONObject header;
        if ( payload.length() != 0 ) {
            header = payload.getJSONObject( PayloadCommonCode.head.name());
            return header.getString(PayloadCommonCode.tgt.name());

        } else {
            logger.error("{}: {}" +
                            "payload: {}."
                    , key ,"Payload is null."
                    , payload.toString()
            );
            throw new NullPointerException(String.format("Payload is null. Payload: %s", payload));
        }

    }

    public static String getMessageNameFromHeader(String key, JSONObject payload) {

        logger.info("{}: {}" +
                        "payload: {}."
                , key ,"Get event name from payload header."
                , payload
        );
        JSONObject header ;
        if ( payload.length() != 0 ) {
            header = payload.getJSONObject( PayloadCommonCode.head.name());
            return header.getString(PayloadCommonCode.cid.name());

        } else{
            logger.error("{}: {}" +
                            "payload: {}."
                    , key ,"Payload is null."
                    , payload.toString()
            );
            throw new NullPointerException(String.format("Payload is null. Payload: %s", payload));
        }

    }

    public static String convertToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        reader.close();
        return stringBuilder.toString();
    }
}
