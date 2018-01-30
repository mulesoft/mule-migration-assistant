package com.mulesoft.tools.migration.tools.dw;


import static org.mule.weave.v2.V2LangMigrant.migrateToV2;

/**
 * Created by davidcisneros on 6/9/17.
 */
public class DataweaveUtils {

    private DataweaveUtils() {

    }

    public static String getMigratedScript(String dwOriginalScript) throws Exception {
        return migrateToV2(dwOriginalScript);
    }
}
