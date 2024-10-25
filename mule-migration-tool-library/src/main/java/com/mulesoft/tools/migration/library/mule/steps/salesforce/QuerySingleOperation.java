/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;

/**
 * Migrate Query Single operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QuerySingleOperation extends AbstractQueryOperationMigrationStep {

  private static String m3Name = "query-single";
  private static String m4Name = "query";
  private static String limit = " LIMIT 1";

  public QuerySingleOperation() {
    super(m3Name, m4Name);
  }

  @Override
  protected void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    String query = mule3Operation.getAttributeValue("query");
    if (query != null) {
      Element mule4Query = new Element("salesforce-query", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      String expression = expressionMigrator.migrateExpression(query, true, mule3Operation);
      mule4Query.setContent(new CDATA(expression + limit));

      mule4Operation.addContent(mule4Query);
    }
  }
}
