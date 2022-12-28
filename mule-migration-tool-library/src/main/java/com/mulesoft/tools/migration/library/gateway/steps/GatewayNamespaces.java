/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps;

import org.jdom2.Namespace;

/**
 * Namespaces to use during gateway migration
 *
 * @author Mulesoft Inc.
 */
public class GatewayNamespaces {

  public static final Namespace MULE_3_GATEWAY_NAMESPACE =
      Namespace.getNamespace("api-platform-gw", "http://www.mulesoft.org/schema/mule/api-platform-gw");
  public static final Namespace MULE_4_GATEWAY_NAMESPACE =
      Namespace.getNamespace("api-gateway", "http://www.mulesoft.org/schema/mule/api-gateway");
  public static final Namespace MULE_DOC_NAMESPACE =
      Namespace.getNamespace("doc", "http://www.mulesoft.org/schema/mule/documentation");
  public static final Namespace MULE_3_POLICY_NAMESPACE =
      Namespace.getNamespace("policy", "http://www.mulesoft.org/schema/mule/policy");
  public static final Namespace MULE_4_POLICY_NAMESPACE =
      Namespace.getNamespace("mule", "http://www.mulesoft.org/schema/mule/core");
  public static final Namespace MULE_4_CORE_NAMESPACE_NO_PREFIX =
      Namespace.getNamespace("http://www.mulesoft.org/schema/mule/core");
  public static final Namespace XSI_NAMESPACE =
      Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
  public static final Namespace HTTP_POLICY_NAMESPACE =
      Namespace.getNamespace("http-policy", "http://www.mulesoft.org/schema/mule/http-policy");
  public static final Namespace IP_FILTER_GW_NAMESPACE =
      Namespace.getNamespace("ip-filter-gw", "http://www.mulesoft.org/schema/mule/ip-filter-gw");
  public static final Namespace IP_FILTER_NAMESPACE =
      Namespace.getNamespace("ip-filter", "http://www.mulesoft.org/schema/mule/ip");
  public static final Namespace HTTP_TRANSFORM_NAMESPACE =
      Namespace.getNamespace("http-transform", "http://www.mulesoft.org/schema/mule/http-policy-transform");
  public static final Namespace API_PLATFORM_GW_MULE_3_NAMESPACE =
      Namespace.getNamespace("api-platform-gw", "http://www.mulesoft.org/schema/mule/api-platform-gw");
  public static final Namespace API_GW_MULE_4_NAMESPACE =
      Namespace.getNamespace("api-gateway", "http://www.mulesoft.org/schema/mule/api-gateway");
  public static final Namespace EXPRESSION_LANGUAGE_NAMESPACE =
      Namespace.getNamespace("expression-language", "http://www.mulesoft.org/schema/mule/expression-language-gw");
  public static final Namespace PROXY_NAMESPACE =
      Namespace.getNamespace("proxy", "http://www.mulesoft.org/schema/mule/proxy");
  public static final Namespace THREAT_PROTECTION_GW_NAMESPACE =
      Namespace.getNamespace("threat-protection-gw", "http://www.mulesoft.org/schema/mule/threat-protection-gw");
  public static final Namespace XML_THREAT_PROTECTION_NAMESPACE =
      Namespace.getNamespace("threat-protection-xml", "http://www.mulesoft.org/schema/mule/xml-threat-protection");
  public static final Namespace JSON_THREAT_PROTECTION_NAMESPACE =
      Namespace.getNamespace("threat-protection-json", "http://www.mulesoft.org/schema/mule/json-threat-protection");
  public static final Namespace CLIENT_ID_ENFORCEMENT_NAMESPACE =
      Namespace.getNamespace("client-id-enforcement", "http://www.mulesoft.org/schema/mule/client-id-enforcement");
  public static final Namespace THROTTLING_GW_MULE_3_NAMESPACE =
      Namespace.getNamespace("throttling-gw", "http://www.mulesoft.org/schema/mule/throttling-gw");
  public static final String THROTTLING_MULE_4_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/throttling";
  public static final Namespace THROTTLING_MULE_4_NAMESPACE =
      Namespace.getNamespace("throttling", THROTTLING_MULE_4_NAMESPACE_URI);
  public static final Namespace OAUTH2_GW_NAMESPACE =
      Namespace.getNamespace("oauth2-gw", "http://www.mulesoft.org/schema/mule/oauth2-gw");
  public static final Namespace OPENAM_GW_NAMESPACE =
      Namespace.getNamespace("openam-gw", "http://www.mulesoft.org/schema/mule/openam-gw");
  public static final Namespace OPENIDCONNECT_GW_NAMESPACE =
      Namespace.getNamespace("openidconnect-gw", "http://www.mulesoft.org/schema/mule/openidconnect-gw");
  public static final Namespace PINGFEDERATE_GW_NAMESPACE =
      Namespace.getNamespace("pingfederate-gw", "http://www.mulesoft.org/schema/mule/pingfederate-gw");
  public static final Namespace FEDERATION_NAMESPACE =
      Namespace.getNamespace("federation", "http://www.mulesoft.org/schema/mule/federation");
  public static final Namespace COMPATIBILITY_NAMESPACE =
      Namespace.getNamespace("compatibility", "http://www.mulesoft.org/schema/mule/compatibility");
  public static final Namespace HTTP_NAMESPACE =
      Namespace.getNamespace("http", "http://www.mulesoft.org/schema/mule/http");
  public static final Namespace REST_VALIDATOR_NAMESPACE =
      Namespace.getNamespace("rest-validator", "http://www.mulesoft.org/schema/mule/rest-validator");
  public static final Namespace APIKIT_NAMESPACE =
      Namespace.getNamespace("apikit", "http://www.mulesoft.org/schema/mule/mule-apikit");
  public static final Namespace EE_NAMESPACE =
      Namespace.getNamespace("ee", "http://www.mulesoft.org/schema/mule/ee/core");
}
