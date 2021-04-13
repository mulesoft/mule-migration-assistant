/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;

import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.Optional;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Salesforce constants class
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SalesforceUtils {

  public static final String MULE3_SALESFORCE_NAMESPACE_PREFIX = "sfdc";
  public static final String MULE3_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/sfdc";
  public static final Namespace MULE3_SALESFORCE_NAMESPACE =
      Namespace.getNamespace(MULE3_SALESFORCE_NAMESPACE_PREFIX, MULE3_SALESFORCE_NAMESPACE_URI);

  public static final String MULE4_SALESFORCE_NAMESPACE_PREFIX = "salesforce";
  public static final String MULE4_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/salesforce";
  public static final Namespace MULE4_SALESFORCE_NAMESPACE =
      Namespace.getNamespace(MULE4_SALESFORCE_NAMESPACE_PREFIX, MULE4_SALESFORCE_NAMESPACE_URI);
  public static final String MULE4_SALESFORCE_SCHEMA_LOCATION =
      "http://www.mulesoft.org/schema/mule/salesforce/current/mule-salesforce.xsd";

  public static final String DOC_NAMESPACE_PREFIX = "doc";
  public static final String DOC_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/documentation";
  public static final Namespace DOC_NAMESPACE = Namespace.getNamespace(DOC_NAMESPACE_PREFIX, DOC_NAMESPACE_URI);

  public static final String START_TRANSFORM_BODY_TYPE_JSON = "%dw 2.0 output application/json\n---\n[{\n";
  public static final String START_TRANSFORM_BODY_TYPE_JAVA = "%dw 2.0 output application/java\n---\n{\n";
  public static final String CLOSE_TRANSFORM_BODY_TYPE_JSON = "\n}]";

  public static void migrateRecordsFromExpression(Element records, Element mule4Operation,
                                                  ExpressionMigrator expressionMigrator, String mule4ElementName) {
    Optional.ofNullable(records.getAttributeValue("ref")).ifPresent(value -> {
      Element recordsChild = new Element(mule4ElementName, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      String expression = expressionMigrator.migrateExpression(value, true, records);
      recordsChild.setContent(new CDATA(expression));

      mule4Operation.addContent(recordsChild);
    });
  }

  public static void createTransformBeforeElement(Element mule3Operation, String transformBody) {
    Element element = new Element("transform");
    element.setName("transform");
    element.setNamespace(CORE_EE_NAMESPACE);
    element.removeContent();
    element.addContent(new Element("message", CORE_EE_NAMESPACE)
        .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
            .setText(transformBody)));

    XmlDslUtils.addElementBefore(element, mule3Operation);
  }

  public static void createTransformAfterElementToMatchOutputs(Element mule4Operation, String transformBody) {
    Element element = new Element("transform");
    element.setName("transform");
    element.setNamespace(CORE_EE_NAMESPACE);
    element.removeContent();
    element.addContent(new Element("message", CORE_EE_NAMESPACE)
        .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
            .setText(transformBody)));

    XmlDslUtils.addElementAfter(element, mule4Operation);
  }

  public static String getTransformBodyToMatchCreateAndUpdateOutputs() {
    return "%dw 2.0\n" +
        "output application/json\n" +
        "---\n" +
        "payload.items map ( item , indexOfItem ) -> {\n" +
        "\tsuccess: item.successful default true,\n" +
        "\twrapped: {\n" +
        "\t\tsuccess: item.payload.success default true,\n" +
        "\t\terrors: item.payload.errors map ( error , indexOfError ) -> error,\n" +
        "\t\tid: item.payload.id default \"\"\n" +
        "\t},\n" +
        "\terrors: item.payload.errors map ( error , indexOfError ) -> error,\n" +
        "\tid: item.id as String default \"\"\n" +
        "}";
  }

  public static String getTransformBodyToMatchUpsertOutputs() {
    return "%dw 2.0\n" +
        "output application/json\n" +
        "---\n" +
        "payload.items map ( item , indexOfItem ) -> {\n" +
        "\tcreated: item.payload.created default true,\n" +
        "\tsuccess: item.successful default true,\n" +
        "\tpayload: {\n" +
        "\t\tid: item.id as String default \"\"\n" +
        "\t},\n" +
        "\terrors: item.payload.errors map ( error , indexOfError ) -> error,\n" +
        "\tid: item.id as String default \"\"\n" +
        "}";
  }

  public static void resolveTypeAttribute(Element mule3Operation, Element mule4Operation) {
    String type = mule3Operation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4Operation.setAttribute("type", type);
    }
  }

}
