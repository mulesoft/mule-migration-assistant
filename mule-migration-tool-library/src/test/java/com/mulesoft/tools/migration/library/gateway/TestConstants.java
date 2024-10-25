/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Namespace;

public class TestConstants {

  public static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  public static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  public static final String MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID = "mule-http-policy-transform-extension";
  public static final String MULE_THROTTLING_EXTENSION_ARTIFACT_ID = "mule-throttling-extension";
  public static final String MULE_FEDERATION_EXTENSION_ARTIFACT_ID = "mule-federation-extension";
  public static final String MULE_CLIENT_ID_ENFORCEMENT_EXTENSION_ARTIFACT_ID = "mule-client-id-enforcement-extension";
  public static final String MULE_RAML_VALIDATOR_EXTENSION = "mule-raml-validator-extension";
  public static final String MULE_REST_VALIDATOR_EXTENSION = "mule-rest-validator-extension";
  public static final String MULE_RAML_VALIDATOR_EXTENSION_VERSION = "someVersion";
  public static final String MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID = "mule-wsdl-functions-extension";

  public static final Path SRC_MAIN_MULE_PATH = Paths.get("src/main/mule");
  public static final Path POLICY_EXAMPLES_PATH = Paths.get("mule/apps/gateway/policy");
  public static final Path MIGRATION_RESOURCES_PATH = Paths.get("src/main/resources/migration");
  public static final Path POLICY_APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/policy/expected");

  public static final String XML_EXTENSION = ".xml";
  public static final String YAML_EXTENSION = ".yaml";
  public static final String TEMPLATE_XML = "template" + XML_EXTENSION;

  public static final String MULE_4_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/core";
  public static final Namespace MULE_4_NAMESPACE = Namespace.getNamespace(MULE_4_NAMESPACE_URI);

  public static final String REST_VALIDATOR_NAMESPACE_PREFIX = "rest-validator";
  public static final String REST_VALIDATOR_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/rest-validator";
  public static final Namespace REST_VALIDATOR_NAMESPACE =
      Namespace.getNamespace(REST_VALIDATOR_NAMESPACE_PREFIX, REST_VALIDATOR_NAMESPACE_URI);

  public static final String APIKIT_NAMESPACE_PREFIX = "apikit";
  public static final String APIKIT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/mule-apikit";
  public static final Namespace APIKIT_NAMESPACE = Namespace.getNamespace(APIKIT_NAMESPACE_PREFIX, APIKIT_NAMESPACE_URI);

  public static final String EE_NAMESPACE_PREFIX = "ee";
  public static final String EE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/core";
  public static final Namespace EE_NAMESPACE = Namespace.getNamespace(EE_NAMESPACE_PREFIX, EE_NAMESPACE_URI);

  public static final String PROXY_NAMESPACE_PREFIX = "proxy";
  public static final String PROXY_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/proxy";
  public static final Namespace PROXY_NAMESPACE = Namespace.getNamespace(PROXY_NAMESPACE_PREFIX, PROXY_NAMESPACE_URI);

  public static final String POLICY_TAG_NAME = "policy";
  public static final String BEFORE_TAG_NAME = "before";
  public static final String DATA_TAG_NAME = "data";
  public static final String MULE_TAG_NAME = "mule";
  public static final String PROXY_TAG_NAME = "proxy";
  public static final String SOURCE_TAG_NAME = "source";
  public static final String TRY_TAG_NAME = "try";
  public static final String EXECUTE_NEXT_TAG_NAME = "execute-next";
  public static final String ERROR_HANDLER_TAG_NAME = "error-handler";
  public static final String ON_ERROR_CONTINUE_TAG_NAME = "on-error-continue";
  public static final String ON_ERROR_PROPAGATE_TAG_NAME = "on-error-propagate";
  public static final String SET_PROPERTY_TAG_NAME = "set-property";
  public static final String SET_PAYLOAD_TAG_NAME = "set-payload";
  public static final String SET_RESPONSE_TAG_NAME = "set-response";
  public static final String SET_VARIABLE_TAG_NAME = "set-variable";
  public static final String SET_ATTRIBUTES_TAG_NAME = "set-attributes";
  public static final String HEADERS_TAG_NAME = "headers";
  public static final String VALIDATE_CLIENT_TAG_NAME = "validate-client";
  public static final String FLOW_TAG_NAME = "flow";
  public static final String SUB_FLOW_TAG_NAME = "sub-flow";

  public static final String RAML_PROXY_CONFIG_TAG_NAME = "raml-proxy-config";
  public static final String RAML = "raml";
  public static final String VALIDATE_REQUEST_TAG_NAME = "validate-request";
  public static final String RAML_LOCATION = "raml.location";
  public static final String RAML_ATTR_VALUE_3X = "![p['" + RAML_LOCATION + "']]";
  public static final String RAML_ATTR_VALUE_3X_HC = "com/mulesoft/anypoint/gw/test.raml";
  public static final String DISABLE_VALIDATIONS_ATTR_NAME = "disableValidations";
  public static final String VALIDATION_DISABLE = "validation.disable";
  public static final String DISABLE_VALIDATIONS_ATTR_VALUE_3X = "![p['" + VALIDATION_DISABLE + "']]";
  public static final String QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME = "queryParamsStrictValidation";
  public static final String QUERY_PARAMS_STRICT_VALIDATION_ATTR_VALUE = "validation.strict.queryParams";
  public static final String HEADERS_STRICT_VALIDATION_ATTR_NAME = "headersStrictValidation";
  public static final String HEADERS_STRICT_VALIDATION_ATTR_VALUE = "validation.strict.headers";
  public static final String API_ATTR_NAME = "api";
  public static final String PARSER_ATTR_VALUE = "parser";
  public static final String IMPLEMENTATION_API_PARSER_ATTR_VALUE = "implementation.api.parser";

  public static final String CONFIG = "config";
  public static final String NAME = "name";
  public static final String CONFIG_REF = "config-ref";

  public static final String GENERIC_TAG_NAME = "foo";
  public static final String GENERIC_TAG_VALUE = "bar";
  public static final String GENERIC_TAG_ATTRIBUTE_NAME = "atrName";
  public static final String GENERIC_TAG_ATTRIBUTE_VALUE = "atrValue";

  public static final String SCHEMA_LOCATION = "schemaLocation";
  public static final String ID = "id";
  public static final String STATUS_CODE = "statusCode";
  public static final String ON_UNACCEPTED = "onUnaccepted";
  public static final String CLIENT_ID = "clientId";
  public static final String CLIENT_SECRET = "clientSecret";
  public static final String PROPERTY_NAME_ATTR_NAME = "propertyName";
  public static final String VALUE_ATTR_NAME = "value";
  public static final String TYPE_ATTR_NAME = "type";
  public static final String LOG_EXCEPTION_ATTR_NAME = "logException";

  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String AUTO = "AUTO";
  public static final String POLICY_ID_ATTR_VALUE = "{{policyId}}";
  public static final String POLICY_ID_BUILD_RESPONSE = "{{policyId}}-build-response";
  public static final String CLIENT_ID_VALUE = "{{clientIdExpression}}";
  public static final String CLIENT_SECRET_VALUE = "{{clientSecretExpression}}";
  public static final String HTTP_STATUS = "http.status";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String VALUE_403 = "403";
  public static final String VALUE_APP_XML = "application/xml";
  public static final String VALUE_APP_JSON = "application/json";
  public static final String VALUE_SOAP_FAULT = "#[soapFault('client', flowVars._invalidClientMessage)]";
  public static final String VALUE_FLOW_VARS = "#[flowVars._invalidClientMessage]";

  public static final String GROUP_ID_VALUE = "{orgId}";

  public static final String EXCHANGE_URL_KEY = "exchange.url";
  public static final String EXCHANGE_URL_VALUE = "https://maven.anypoint.mulesoft.com/api/v1/organizations/{orgId}/maven";
  public static final String MULE_MAVEN_PLUGIN_VERSION_KEY = "mule.maven.plugin.version";

  public static final String EXCHANGE_SERVER_ID = "exchange-server";
  public static final String EXCHANGE_SERVER_NAME = "MuleSoft Exchange Environment";
  public static final String EXCHANGE_SERVER_URL = "${" + EXCHANGE_URL_KEY + "}";

  public static final String DISTRIBUTION_MANAGEMENT_REPOSITORY_NAME = "Corporate Repository";
  public static final String DISTRIBUTION_MANAGEMENT_LAYOUT = "default";

  public static final String MULE_MAVEN_PLUGIN_GROUP_ID = "org.mule.tools.maven";
  public static final String MULE_MAVEN_PLUGIN_ARTIFACT_ID = "mule-maven-plugin";
  public static final String MULE_MAVEN_PLUGIN_VERSION = "${" + MULE_MAVEN_PLUGIN_VERSION_KEY + "}";
  public static final String MULE_MAVEN_PLUGIN_EXTENSIONS = "true";

  public static final String MAVEN_DEPLOY_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
  public static final String MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID = "maven-deploy-plugin";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_ID = "upload-template";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_PHASE = "deploy";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GOAL = "deploy-file";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_REPOSITORY_ID = EXCHANGE_SERVER_ID;
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_URL = EXCHANGE_SERVER_URL;
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_FILE = "${project.basedir}/${project.artifactId}.yaml";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GENERATE_POM = "false";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GROUP_ID = "${project.groupId}";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_ARTIFACT_ID = "${project.artifactId}";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_VERSION = "${project.version}";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_PACKAGING = "yaml";
  public static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_CLASSIFIER = "policy-definition";

  public static final String REPOSITORY_ID_KEY = "repositoryId";
  public static final String URL_KEY = "url";
  public static final String FILE_KEY = "file";
  public static final String GENERATE_POM_KEY = "generatePom";
  public static final String GROUP_ID_KEY = "groupId";
  public static final String ARTIFACT_ID_KEY = "artifactId";
  public static final String VERSION_KEY = "version";
  public static final String PACKAGING_KEY = "packaging";
  public static final String CLASSIFIER_KEY = "classifier";

  public static final String PLUGIN_REPOSITORY_NAME = "Mule Repository";
  public static final String PLUGIN_REPOSITORY_URL = "https://repository.mulesoft.org/nexus/content/repositories/public/";

}
