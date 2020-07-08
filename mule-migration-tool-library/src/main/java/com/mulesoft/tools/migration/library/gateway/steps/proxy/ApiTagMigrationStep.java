/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_GW_MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate api tag
 *
 * @author Mulesoft Inc.
 */
public class ApiTagMigrationStep extends GatewayMigrationStep {

  private static final String API_TAG_NAME = "api";
  private static final String AUTODISCOVERY_TAG_NAME = "autodiscovery";

  private static final String API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE4 = "http://www.mulesoft.org/schema/mule/api-gateway";
  private static final String API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd";
  private static final String API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE3 = "http://www.mulesoft.org/schema/mule/api-platform-gw";
  private static final String API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE3_XSD =
      "http://www.mulesoft.org/schema/mule/api-platform-gw/current/mule-api-platform-gw.xsd";

  private static final String API_ID = "apiId";
  private static final String API_ID_VALUE = "${api.id}";

  private static final String ID = "id";
  private static final String API_NAME = "apiName";
  private static final String CREATE = "create";
  private static final String VERSION = "version";
  private static final String APIKIT_REF = "apikitRef";

  private static final List<String> ATTRIBUTES_TO_REMOVE = new ArrayList<String>() {

    {
      add(ID);
      add(API_NAME);
      add(CREATE);
      add(VERSION);
      add(APIKIT_REF);
    }
  };

  public ApiTagMigrationStep() {
    super(API_PLATFORM_GW_MULE_3_NAMESPACE, API_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrateRootElement(element, API_PLATFORM_GW_MULE_3_NAMESPACE, API_GW_MULE_4_NAMESPACE,
                       API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE3_XSD,
                       API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD, API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE3,
                       API_GATEWAY_XSI_SCHEMA_LOCATION_URI_MULE4);
    element.setName(AUTODISCOVERY_TAG_NAME);
    element.setNamespace(API_GW_MULE_4_NAMESPACE);
    element.setAttribute(new Attribute(API_ID, API_ID_VALUE));
    removeAttributes(ATTRIBUTES_TO_REMOVE, element);
    element.removeContent();
  }
}
