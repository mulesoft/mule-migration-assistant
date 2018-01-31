/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
