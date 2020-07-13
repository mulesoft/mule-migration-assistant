/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools;

import org.jdom2.Namespace;

/**
 * Migrate BatchJob component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SalesforceConstants {

  public static final String MULE3_SALESFORCE_NAMESPACE_PREFIX = "sfdc";
  public static final String MULE4_SALESFORCE_NAMESPACE_PREFIX = "salesforce";

  public static final String MULE3_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/sfdc";
  public static final String MULE4_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/salesforce";

  public static final Namespace MULE3_SALESFORCE_NAMESPACE =
      Namespace.getNamespace(MULE3_SALESFORCE_NAMESPACE_PREFIX, MULE3_SALESFORCE_NAMESPACE_URI);
  public static final Namespace MULE4_SALESFORCE_NAMESPACE =
      Namespace.getNamespace(MULE4_SALESFORCE_NAMESPACE_PREFIX, MULE4_SALESFORCE_NAMESPACE_URI);

  public static final String DOC_NAMESPACE_PREFIX = "doc";
  public static final String DOC_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/documentation";
  public static final Namespace DOC_NAMESPACE = Namespace.getNamespace(DOC_NAMESPACE_PREFIX, DOC_NAMESPACE_URI);
}
