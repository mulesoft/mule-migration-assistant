/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getTopLevelCoreXPathSelector;

import com.mulesoft.tools.migration.library.apikit.steps.AbstractApikitMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;

/**
 * Migrate ApiConfig flow definitions
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrateApiConfigFlows extends AbstractApikitMigrationStep {


  private static final String APIKIT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/mule-apikit";
  private static final String XPATH_SELECTOR_ROUTER =
      "//*[local-name()='router' and namespace-uri()='" + APIKIT_NAMESPACE_URI + "']";
  public static final String XPATH_SELECTOR = getTopLevelCoreXPathSelector("flow");
  static final String MULE_4_API_FOLDER = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "api";

  RamlModelBuilder builder = null;
  RamlModelResult result = null;

  @Override
  public String getDescription() {
    return "Migrate ApiConfig flow definitions";
  }

  public MigrateApiConfigFlows() {
    this.setAppliedTo(XPATH_SELECTOR_ROUTER);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    // resolve config-ref
    String apiConfig = null;

    final Element node = getApplicationModel().getNode(XPATH_SELECTOR);
    if (node != null && node.getAttribute("config-ref") != null) {
      apiConfig = node.getAttribute("config-ref").getValue();

    }
    if (apiConfig != null) {
      // API Kit Router found
      addNameSpace(CORE_EE_NAMESPACE, EE_NAMESPACE_SCHEMA, element.getDocument());


      final String flowName = element.getAttributeValue("name");

      if (flowName.endsWith(":" + apiConfig)) {

        List<String> uriParams = getResourceByFlowName(flowName.replaceAll(":" + apiConfig, ""), report, element);
        if (uriParams != null && uriParams.size() > 0) {
          Element transformElement = new Element("transform", CORE_EE_NAMESPACE);
          transformElement.removeContent();
          final Element variablesElement = new Element("variables", CORE_EE_NAMESPACE);
          variablesElement.removeContent();
          transformElement.addContent(variablesElement);

          for (String name : uriParams) {
            final Element variableElement = new Element("set-variable", CORE_EE_NAMESPACE);
            variableElement.setAttribute("variableName", name);
            variableElement.setContent(new CDATA("attributes.uriParams." + name));
            variablesElement.addContent(variableElement);
          }

          // Add at index 0 to be the first element
          element.addContent(0, transformElement);
        }
      }
    }
  }

  private List<String> getResourceByFlowName(final String flowName, MigrationReport migrationReport, Element element)
      throws RuntimeException {
    List<String> uriParams = null;
    File ramlFile = null;
    if (builder == null) {
      // should use raml attirbute if exists
      final String ramlLocation = getApplicationModel().getPomModel().get().getArtifactId() + ".raml";
      ramlFile =
          getApplicationModel().getProjectBasePath().resolve(MULE_4_API_FOLDER + File.separator + ramlLocation).toFile();
      builder = new RamlModelBuilder();
      result = builder.buildApi(ramlFile);
    }

    if (result.getApiV10() == null) {

      migrationReport.report("raml.invalid", element, element,
                             result.getValidationResults().toString());
      throw new RuntimeException("Ramlfile invalid: " + result.getValidationResults().toString());
    }


    final List<Resource> resources = result.getApiV10().resources();
    boolean found = false;
    for (Resource resource : resources) {
      uriParams = new ArrayList<String>();
      ResourceWrapper resourceWrapper = new ResourceWrapper(resource, uriParams);
      resourceWrapper = getResourcePathWithParent(resourceWrapper);
      final Resource innerResource = resourceWrapper.getResource();
      String normalizedResource = innerResource.resourcePath().replaceAll("/", "\\\\")
          .replaceAll("\\[|\\{", "(")
          .replaceAll("\\]|\\}", ")")
          .replaceAll("#", "_");
      final List<Method> methods = innerResource.methods();
      for (Method method : methods) {
        final String methodResource = method.method() + ":" + normalizedResource;
        final List<TypeDeclaration> bodyTypes = method.body();
        if (flowName.equals(methodResource)) {
          final List<TypeDeclaration> uriParameters = resource.uriParameters();
          for (TypeDeclaration typeDeclaration : uriParameters) {
            uriParams.add(typeDeclaration.name());
          }
          found = true;
        } else {
          for (TypeDeclaration body : bodyTypes) {
            final String name = methodResource + ":" + body.name().replaceAll("/", "\\\\");
            if (flowName.equals(name)) {
              final List<TypeDeclaration> uriParameters = resource.uriParameters();
              for (TypeDeclaration typeDeclaration : uriParameters) {
                uriParams.add(typeDeclaration.name());
              }
              found = true;
            }
          }
        }
      }

      if (found) {
        break;
      } else {
        uriParams = new ArrayList<String>();
      }
    }
    return uriParams;
  }

  private ResourceWrapper getResourcePathWithParent(ResourceWrapper tResourceWrapper) {
    if (tResourceWrapper.getResource().resources().size() > 0) {
      for (Resource subResource : tResourceWrapper.getResource().resources()) {
        final ResourceWrapper subResourceWrapper = new ResourceWrapper(subResource, tResourceWrapper.getUriParams());
        if (subResource.resources().size() > 0) {
          final List<TypeDeclaration> uriParameters = subResource.uriParameters();
          for (TypeDeclaration typeDeclaration : uriParameters) {
            tResourceWrapper.getUriParams().add(typeDeclaration.name());
          }
          return getResourcePathWithParent(subResourceWrapper);
        } else {
          return subResourceWrapper;
        }
      }
    }
    return tResourceWrapper;
  }

  static class ResourceWrapper {

    final private Resource resource;
    final private List<String> uriParams;


    public List<String> getUriParams() {
      return uriParams;
    }

    public ResourceWrapper(Resource resource, List<String> uriParams) {
      this.resource = resource;
      this.uriParams = uriParams;
    }

    public Resource getResource() {
      return resource;
    }
  }
}
