/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.pom.PomModel.DEFAULT_GROUP_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModel.DEFAULT_VERSION;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.getArtifactId;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.getGroupId;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.getVersion;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.generateDocument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getDocumentNamespaces;
import static java.io.File.separator;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Represent the application to be migrated
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationModel {

  private final Map<Path, Document> applicationDocuments;
  private final Map<Path, Document> domainDocuments;
  private ProjectType projectType;
  private String muleVersion;
  private PomModel pomModel;
  private Path sourceProjectBasePath;
  private Path projectBasePath;
  private MuleArtifactJsonModel muleArtifactJsonModel;
  private List<Namespace> supportedNamespaces;
  private Parent projectPomParent;
  private ApplicationGraph applicationGraph;

  protected ApplicationModel(Map<Path, Document> applicationDocuments) {
    this(applicationDocuments, emptyMap());
  }

  protected ApplicationModel(Map<Path, Document> applicationDocuments, Map<Path, Document> domainDocuments) {
    this.applicationDocuments = applicationDocuments;
    this.domainDocuments = domainDocuments;
  }

  /**
   * The key of the map is relative to the source of the target project.
   */
  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  /**
   * The key of the map is relative to the source of the target project.
   * <p>
   * This is just for the domain referenced by an application, this is not used when migrating the domain itself.
   */
  public Map<Path, Document> getDomainDocuments() {
    return domainDocuments;
  }

  /**
   * Returns all the nodes in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public List<Element> getNodes(String xpathExpression) {
    return getNodes(XPathFactory.instance().compile(xpathExpression));
  }

  /**
   * Returns a single node in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return first node matching expression, if not present, it returns null
   */
  public Element getNode(String xpathExpression) {
    List<Element> nodes = getNodes(XPathFactory.instance().compile(xpathExpression));
    if (nodes.isEmpty() || nodes.size() > 1) {
      throw new IllegalStateException(format("Found %d nodes for xpath expression '%s'", nodes.size(), xpathExpression));
    }
    return nodes.stream().findFirst().orElse(null);
  }

  /**
   * Returns a single node in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public Optional<Element> getNodeOptional(String xpathExpression) {
    List<Element> nodes = getNodes(XPathFactory.instance().compile(xpathExpression));
    if (nodes.isEmpty()) {
      return empty();
    } else if (nodes.size() > 1) {
      throw new IllegalStateException(format("Found %d nodes for xpath expression '%s'", nodes.size(), xpathExpression));
    }
    return of(nodes.get(0));
  }

  /**
   * Returns all the nodes in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public List<Element> getNodes(XPathExpression xpathExpression) {
    checkArgument(xpathExpression != null, "The Xpath Expression must not be null nor empty");

    List<Element> nodes = new LinkedList<>();
    for (Document doc : getApplicationDocuments().values()) {
      nodes.addAll(getElementsFromDocument(xpathExpression, doc));
    }
    for (Document doc : getDomainDocuments().values()) {
      nodes.addAll(getElementsFromDocument(xpathExpression, doc));
    }
    return nodes;
  }

  /**
   * Retrieves all elements in the document that have the specified namespace
   *
   * @param document the document where the elements are going to be searched through
   * @param namespace the namespace of the elements that should be retrieved
   * @return a list of elements within the document that have that namespace
   */
  public static List<Element> getElementsWithNamespace(Document document, Namespace namespace,
                                                       ApplicationModel applicationModel) {
    String xPathExpression = "//*[namespace-uri()='" + namespace.getURI() + "']";
    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null,
                                                                     getDocumentNamespaces(document, applicationModel
                                                                         .getSupportedNamespaces()));
    return xpath.evaluate(document);
  }

  /**
   * Adds a namespace to the application documents
   *
   * @param prefix
   * @param uri
   * @param schemaLocation
   */
  public void addNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : applicationDocuments.values()) {
      addNameSpace(namespace, schemaLocation, doc);
    }
    for (Document doc : domainDocuments.values()) {
      addNameSpace(namespace, schemaLocation, doc);
    }
  }

  /**
   * Adds a namespace to an application document
   *
   * @param namespace
   * @param schemaLocation
   * @param document
   */
  public static void addNameSpace(Namespace namespace, String schemaLocation, Document document) {
    document.getRootElement().addNamespaceDeclaration(namespace);

    Attribute schemaLocationAttribute =
        document.getRootElement().getAttribute("schemaLocation", document.getRootElement().getNamespace("xsi"));
    if (schemaLocationAttribute != null && (!schemaLocationAttribute.getValue().contains(namespace.getURI() + " ")
        && !schemaLocationAttribute.getValue().contains(schemaLocation))) {

      StringBuilder value = new StringBuilder(schemaLocationAttribute.getValue());
      value.append(" " + namespace.getURI());
      value.append(" " + schemaLocation);
      schemaLocationAttribute.setValue(value.toString());
    }
  }

  /**
   * Removes a namespace from the application documents
   *
   * @param prefix
   * @param uri
   * @param schemaLocation
   */
  public void removeNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : getApplicationDocuments().values()) {
      removeNameSpace(namespace, schemaLocation, doc);
    }
  }

  /**
   * Removes a namespace from an application document
   *
   * @param namespace
   * @param schemaLocation
   * @param document
   */
  public void removeNameSpace(Namespace namespace, String schemaLocation, Document document) {
    Element rootElement = document.getRootElement();

    rootElement.removeNamespaceDeclaration(namespace);

    Attribute schemaLocationAttribute = rootElement.getAttribute("schemaLocation", rootElement.getNamespace("xsi"));

    if (schemaLocationAttribute != null && schemaLocationAttribute.getValue().contains(namespace.getURI())
        && schemaLocationAttribute.getValue().contains(schemaLocation)) {

      String value = schemaLocationAttribute.getValue();
      value = value.replace(schemaLocation, EMPTY);
      value = value.replace(namespace.getURI(), EMPTY);
      schemaLocationAttribute.setValue(value);
    }
  }

  /**
   * Retrieves all elements that satisfy the given xpath expression in the document
   *
   * @param xpathExpression the xpath query expression
   * @param document the document to query
   * @return all the elements queried by the xpath expression in the given document
   * @throws IllegalArgumentException if the XPath query cannot be compiled
   */
  public List<Element> getElementsFromDocument(String xpathExpression, Document document) {
    return getElementsFromDocument(XPathFactory.instance().compile(xpathExpression), document);
  }

  /**
   * Retrieves all elements that satisfy the given xpath expression in the document
   *
   * @param xpath the xpath query expression
   * @param document the document to query
   * @return all the elements queried by the xpath expression in the given document
   * @throws IllegalArgumentException if the XPath query cannot be compiled
   */
  public List<Element> getElementsFromDocument(XPathExpression xpath, Document document) {
    XPathExpression<Element> compiledXPath =
        XPathFactory.instance().compile(xpath.getExpression(), Filters.element(), null,
                                        getDocumentNamespaces(document, supportedNamespaces));
    try {
      return compiledXPath.evaluate(document);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().matches("Namespace with prefix '\\w+' has not been declared.")) {
        return emptyList();
      } else {
        throw e;
      }
    }
  }

  /**
   * Retrieves all documents that contains an specific string value on any place
   *
   * @param value
   * @return all the documents containing the string
   */
  public List<Document> getDocumentsContainString(String value) {
    List<Document> documents = new ArrayList<>();
    for (Document doc : getApplicationDocuments().values()) {
      if (documentContainsString(value, doc)) {
        documents.add(doc);
      }
    }
    for (Document doc : getDomainDocuments().values()) {
      if (documentContainsString(value, doc)) {
        documents.add(doc);
      }
    }
    return documents;
  }

  private boolean documentContainsString(String value, Document doc) {
    XMLOutputter outputter = new XMLOutputter();
    return outputter.outputString(doc).contains(value);
  }

  /**
   * Sets the {@link PomModel} that represents the pom.xml file present in this application
   *
   * @param pomModel
   */
  private void setPomModel(PomModel pomModel) {
    this.pomModel = pomModel;
  }

  /**
   * Retrieves the {@link PomModel} of this instance. It is optional since the pom is not mandatory in a Mule 3.x application
   *
   * @return tan optional {@link PomModel}
   */
  public Optional<PomModel> getPomModel() {
    return Optional.ofNullable(pomModel);
  }

  public void setProjectType(ProjectType projectType) {
    this.projectType = projectType;
  }

  public ProjectType getProjectType() {
    return projectType;
  }

  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
  }

  public String getMuleVersion() {
    return muleVersion;
  }

  /**
   * The path to the root of the project represented by the application model instance
   *
   * @param sourceProjectBasePath
   */
  private void setSourceProjectBasePath(Path sourceProjectBasePath) {
    this.sourceProjectBasePath = sourceProjectBasePath;
  }

  /**
   * Retrieves the root path of the project represented by the application model instance
   */
  public Path getSourceProjectBasePath() {
    return this.sourceProjectBasePath;
  }

  /**
   * The path to the root of the project represented by the application model instance
   *
   * @param projectBasePath
   */
  private void setProjectBasePath(Path projectBasePath) {
    this.projectBasePath = projectBasePath;
  }

  /**
   * Retrieves the root path of the project represented by the application model instance
   */
  public Path getProjectBasePath() {
    return this.projectBasePath;
  }

  /**
   * Sets the {@link MuleArtifactJsonModel} that represents the mule-artifact.json file
   *
   * @param muleArtifactJsonModel
   */
  private void setMuleArtifactJsonModel(MuleArtifactJsonModel muleArtifactJsonModel) {
    this.muleArtifactJsonModel = muleArtifactJsonModel;
  }

  /**
   * Retrieves the {@link MuleArtifactJsonModel}. It is optional since this resource is not part of a Mule 3.x application
   *
   * @return an optional {@link MuleArtifactJsonModel}
   */
  public Optional<MuleArtifactJsonModel> getMuleArtifactJsonModel() {
    return Optional.ofNullable(muleArtifactJsonModel);
  }

  /**
   * Sets the {@link List<Namespace>} that represents the supported namespaces for the tool.
   *
   * @param supportedNamespaces
   */
  public void setSupportedNamespaces(List<Namespace> supportedNamespaces) {
    this.supportedNamespaces = supportedNamespaces;
  }

  /**
   * Retrieves the {@link List<Namespace>}.
   *
   * @return an optional {@link List<Namespace>}
   */
  public List<Namespace> getSupportedNamespaces() {
    return this.supportedNamespaces;
  }

  /**
   * Retrieves the Project Parent GAV (artifactId:groupId:version). It is optional since the pom is not mandatory in a Mule 3.x
   * application
   *
   * @return tan optional {@link PomModel}
   */

  public Optional<Parent> getProjectPomParent() {
    return Optional.ofNullable(projectPomParent);
  }

  /**
   * Set the Project Parent GAV (artifactId:groupId:version)
   * 
   * @param projectPomParent as (artifactId:groupId:version) String
   */
  private void setProjectPomParent(Parent projectPomParent) {
    this.projectPomParent = projectPomParent;
  }

  /**
   * Updates a reference on the application documents map
   *
   * @param fileName
   * @param newFileName
   */
  public void updateApplicationModelReference(Path fileName, Path newFileName) {
    Path relativeFilePath = projectBasePath.relativize(fileName);
    Path keyToRemove =
        applicationDocuments.keySet().stream().filter(key -> key.equals(relativeFilePath)).findFirst().orElse(null);
    if (keyToRemove != null) {
      Document newDocument = applicationDocuments.get(keyToRemove);
      newDocument.setBaseURI(newFileName.toUri().toString());
      applicationDocuments.put(projectBasePath.relativize(newFileName), newDocument);
      applicationDocuments.remove(keyToRemove);
    }
  }

  /**
   * Set the ApplicationGraph
   *
   * @param graph ApplicationGraph
   */
  public void setApplicationGraph(ApplicationGraph graph) {
    this.applicationGraph = graph;
  }

  /**
   * @return The {@link ApplicationGraph} when running in no-compatibility mode, otherwise null
   */
  public ApplicationGraph getApplicationGraph() {
    return this.applicationGraph;
  }

  /**
   * @return true if running in NO Compatibility mode.
   */
  public boolean noCompatibilityMode() {
    return this.applicationGraph != null;
  }

  public void addApplicationDocument(Path path, Document document) {
    this.applicationDocuments.put(path, document);
  }

  /**
   * It represent the builder to obtain a {@link ApplicationModel}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class ApplicationModelBuilder {

    private Collection<Path> configurationFiles;
    private Collection<Path> testConfigurationFiles;
    private Path muleArtifactJson;
    private Path pom;
    private Path sourceProjectBasePath;
    private Path projectBasePath;
    private Path parentDomainBasePath;
    private ProjectType projectType;
    private String muleVersion;
    private List<Namespace> supportedNamespaces;
    private Parent projectPomParent;
    private String projectGAV;
    private ApplicationGraph applicationGraph;
    private boolean generateElementIds;

    /**
     * Collection of paths to project configuration files
     *
     * @param configurationFiles
     * @return the builder
     */
    public ApplicationModelBuilder withConfigurationFiles(Collection<Path> configurationFiles) {
      this.configurationFiles = configurationFiles;
      return this;
    }

    /**
     * Collection of paths to project test configuration files
     *
     * @param testConfigurationFiles
     * @return the builder
     */
    public ApplicationModelBuilder withTestConfigurationFiles(Collection<Path> testConfigurationFiles) {
      this.testConfigurationFiles = testConfigurationFiles;
      return this;
    }

    /**
     * Path to project mule-artifact.json file
     *
     * @param muleArtifactJson
     * @return the builder
     */
    public ApplicationModelBuilder withMuleArtifactJson(Path muleArtifactJson) {
      this.muleArtifactJson = muleArtifactJson;
      return this;
    }

    /**
     * Path to project pom.xml file
     *
     * @param pom
     * @return the builder
     */
    public ApplicationModelBuilder withPom(Path pom) {
      this.pom = pom;
      return this;
    }

    /**
     * Path to source project base folder
     *
     * @param sourceProjectBasePath
     * @return the builder
     */
    public ApplicationModelBuilder withSourceProjectBasePath(Path sourceProjectBasePath) {
      this.sourceProjectBasePath = sourceProjectBasePath;
      return this;
    }

    /**
     * Path to project base folder
     *
     * @param projectBasePath
     * @return the builder
     */
    public ApplicationModelBuilder withProjectBasePath(Path projectBasePath) {
      this.projectBasePath = projectBasePath;
      return this;
    }

    /**
     * Path to the parent domain of the app being migrated
     *
     * @param parentDomainBasePath
     * @return the builder
     */
    public ApplicationModelBuilder withParentDomainBasePath(Path parentDomainBasePath) {
      this.parentDomainBasePath = parentDomainBasePath;
      return this;
    }

    /**
     * The type of project in the folder
     *
     * @param type
     * @return the builder
     */
    public ApplicationModelBuilder withProjectType(ProjectType type) {
      this.projectType = type;
      return this;
    }

    /**
     * String representing the project version
     *
     * @param muleVersion
     * @return the builder
     */
    public ApplicationModelBuilder withMuleVersion(String muleVersion) {
      this.muleVersion = muleVersion;
      return this;
    }

    /**
     * List representing the supported namespaces
     *
     * @param supportedNamespaces
     * @return the builder
     */
    public ApplicationModelBuilder withSupportedNamespaces(List<Namespace> supportedNamespaces) {
      this.supportedNamespaces = supportedNamespaces;
      return this;
    }

    /**
     * The project parent POM
     * 
     * @param projectPomParent the parent POM
     * @return the builder
     */
    public ApplicationModelBuilder withProjectPomParent(Parent projectPomParent) {
      this.projectPomParent = projectPomParent;
      return this;
    }

    /**
     * POM GAV coordinates used to override auto-generated POM values
     *
     * @param projectGAV GAV coordinates
     * @return the builder
     */
    public ApplicationModelBuilder withProjectPomGAV(String projectGAV) {
      this.projectGAV = projectGAV;
      return this;
    }

    /**
     * The application modeled as a graph
     *
     * @param graph applicationGraph
     * @return the builder
     */
    public ApplicationModelBuilder withApplicationGraph(ApplicationGraph graph) {
      this.applicationGraph = graph;
      return this;
    }

    /**
     * Generate element synthetic ids
     *
     * @param generateElementIds generate ids or not
     * @return the builder
     */
    public ApplicationModelBuilder withGenerateElementIds(boolean generateElementIds) {
      this.generateElementIds = generateElementIds;
      return this;
    }

    /**
     * Build the {@link ApplicationModel}
     *
     * @return an {@link ApplicationModel} instance
     * @throws Exception if project base path is null; if pom.xml or mule-artifact.json file do not exist in the provided path or
     *         are invalid
     */
    public ApplicationModel build() throws Exception {
      checkArgument(projectBasePath != null, "Project base path cannot be null");

      Set<Path> applicationFilePaths = new HashSet<>();
      if (configurationFiles != null) {
        applicationFilePaths.addAll(configurationFiles);
      }
      if (testConfigurationFiles != null) {
        applicationFilePaths.addAll(testConfigurationFiles);
      }

      Map<Path, Document> applicationDocuments = new HashMap<>();
      for (Path afp : applicationFilePaths) {
        try {
          applicationDocuments.put(projectBasePath.relativize(afp), generateDocument(afp, generateElementIds));
        } catch (JDOMException | IOException e) {
          throw new RuntimeException("Application Model Generation Error - Fail to parse file: " + afp, e);
        }
      }

      ApplicationModel applicationModel;
      if (parentDomainBasePath != null) {
        Set<Path> domainFilePaths = new HashSet<>();
        Path domainSourceFilesPath = parentDomainBasePath.resolve("src" + separator + "main" + separator + "domain");
        if (domainSourceFilesPath.toFile().exists()) {
          for (File domainXmlFile : domainSourceFilesPath
              .toFile()
              .listFiles((FilenameFilter) new SuffixFileFilter(".xml"))) {
            domainFilePaths.add(domainXmlFile.toPath());
          }
          Map<Path, Document> domainDocuments = new HashMap<>();
          for (Path dfp : domainFilePaths) {
            try {
              domainDocuments.put(parentDomainBasePath.relativize(dfp), generateDocument(dfp, generateElementIds));
            } catch (JDOMException | IOException e) {
              throw new RuntimeException("Application Model Generation Error - Fail to parse file: " + dfp, e);
            }
          }
          applicationModel = new ApplicationModel(applicationDocuments, domainDocuments);
        } else {
          throw new RuntimeException("Application Model Generation Error - The provided domain isn't a Mule 3 domain: "
              + parentDomainBasePath);
        }
      } else {
        applicationModel = new ApplicationModel(applicationDocuments);
      }


      if (muleArtifactJson != null) {
        MuleArtifactJsonModel.MuleApplicationJsonModelBuilder builder =
            new MuleArtifactJsonModel.MuleApplicationJsonModelBuilder();
        builder.withMuleArtifactJson(muleArtifactJson);
        // First time the project is built there is no such file, so we set the version
        if (!muleArtifactJson.toFile().exists()) {
          builder.withMuleVersion(muleVersion);
        }
        applicationModel.setMuleArtifactJsonModel(builder.build());
      }
      PomModel pomModel;
      if (pom != null && pom.toFile().exists()) {
        pomModel = new PomModel.PomModelBuilder()
            .withPom(pom)
            .build();
      } else {
        pomModel = new PomModel.PomModelBuilder()
            .withGroupId(getGroupId(projectGAV).orElse(DEFAULT_GROUP_ID))
            .withArtifactId(getArtifactId(projectGAV).orElse(projectBasePath.getFileName().toString()))
            .withVersion(getVersion(projectGAV).orElse(DEFAULT_VERSION))
            .withPackaging(projectType.getPackaging())
            .build();
      }
      applicationModel.setProjectType(projectType);
      applicationModel.setMuleVersion(muleVersion);
      applicationModel.setPomModel(pomModel);
      applicationModel.setSourceProjectBasePath(sourceProjectBasePath);
      applicationModel.setProjectBasePath(projectBasePath);
      applicationModel.setProjectPomParent(projectPomParent);

      // Eliminate duplicates, makes for easier debugging inside
      applicationModel.setSupportedNamespaces(supportedNamespaces != null ? new ArrayList<>(new HashSet<>(supportedNamespaces))
          : emptyList());

      applicationModel.setApplicationGraph(applicationGraph);

      return applicationModel;
    }

  }
}
