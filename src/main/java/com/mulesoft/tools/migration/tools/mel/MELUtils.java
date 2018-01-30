package com.mulesoft.tools.migration.tools.mel;

import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

public class MELUtils {

    private MELUtils() {
    }

    private static final String SINGLE_QUOTE = "'";

    private static final String SEPARATOR = ":";

    public static String getMELExpressionFromMap(Map<String, String> attributesMap) {
        StringJoiner mapJoiner = new StringJoiner(",");
        StringBuilder melExpressionBuilder = new StringBuilder();
        Iterator<Map.Entry<String,String>> it = attributesMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String,String> pair = it.next();
            StringBuilder entryBuilder = new StringBuilder();
            entryBuilder.append(SINGLE_QUOTE);
            entryBuilder.append(pair.getKey());
            entryBuilder.append(SINGLE_QUOTE);
            entryBuilder.append(SEPARATOR);

            if(pair.getValue().contains("#[")) {
                entryBuilder.append(pair.getValue().replace("#[","").replace("]",""));
            } else {
                entryBuilder.append(SINGLE_QUOTE);
                entryBuilder.append(pair.getValue());
                entryBuilder.append(SINGLE_QUOTE);
            }

            mapJoiner.add(entryBuilder.toString());
        }

        melExpressionBuilder.append("#[mel:[");
        melExpressionBuilder.append(mapJoiner.toString());
        melExpressionBuilder.append("]]");
        return melExpressionBuilder.toString();
    }

    public static String getMELExpressionFromValue(String attributeValue) {
        return attributeValue.replace("#[","#[mel:[").concat("]");
    }
}
