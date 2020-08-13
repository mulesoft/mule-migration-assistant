/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;

/**
 * Migrate Invoke Apex Rest Method operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InvokeApexRestMethodOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "invoke-apex-rest-method";

  public InvokeApexRestMethodOperation() {
    super(name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation);

//in request se construieste cu
//    body: payload, -> adica input-ref - DONE?
//            headers: vars.headers, -> adica requestHeaders-ref
//            cookies: vars.cookies, -> de unde le ia???
//            queryParameters: vars.queryParameters -> adica <sfdc:query-parameters>

    //  	<sfdc:invoke-apex-rest-method  requestHeaders-ref="#[flowvars.headers]">
//			<sfdc:query-parameters ref="#[vars.queryParameters-pot sa fei si adaugati manual]"/>
//        </sfdc:invoke-apex-rest-method>
//  devine
//      <salesforce:invoke-apex-rest-method
//			<salesforce:request ><![CDATA[#[payload-request]]]></salesforce:request>
//		</salesforce:invoke-apex-rest-method>
//
//  				<salesforce:request ><![CDATA[#[%dw 2.0
//  output application/json
//---
//  {
//    queryParams: {
//      queryParam: "query"
//    },
//    body: {
//      bodyParam: "body"
//    }
//  }]]]></salesforce:request>

    StringBuilder requestContents = new StringBuilder();

    String body = mule3Operation.getAttributeValue("input-ref");
    if (body != null && !body.isEmpty()) {
      String expression = expressionMigrator.migrateExpression(body, true, mule3Operation); // ?
      requestContents.append("body: { " + body + " } ");
    }








    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    String restMethodName = mule3Operation.getAttributeValue("restMethodName");
    if (restMethodName != null && !restMethodName.isEmpty()) {
      Integer index = restMethodName.indexOf("||");
      mule4Operation.setAttribute("className", restMethodName.substring(0, index));
      mule4Operation.setAttribute("methodName", restMethodName.substring(index + 2, restMethodName.length()));
    }
  }
}
