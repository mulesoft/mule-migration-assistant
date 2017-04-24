package com.mulesoft.tools.migration.mel;

import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by davidcisneros on 4/24/17.
 */
public class MELUtils {

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
            entryBuilder.append(SINGLE_QUOTE);
            entryBuilder.append(pair.getValue());
            entryBuilder.append(SINGLE_QUOTE);
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
