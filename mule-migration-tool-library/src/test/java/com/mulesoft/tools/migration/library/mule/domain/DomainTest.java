/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.domain;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_DOMAIN;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.tasks.DbMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.DomainAppMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.EndpointsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPCleanupTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.JmsDomainMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.JmsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MigrationCleanTask;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessGeneral;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.SpringMigrationTask;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class DomainTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path DOMAIN_AND_APP_CONFIG_EXAMPLES_PATH = Paths.get("mule/domain");

  @Parameters(name = "{0}, {1}")
  public static Object[][] params() {
    return new Object[][] {
        new String[] {"domain-01", "app-01"},
        new String[] {"domain-02", "app-02"},
        new String[] {"domain-03", "app-03"},
        new String[] {"domain-04", "app-04"}
    };
  }

  private final Path domainConfigPath;
  private final Path domainTargetPath;
  private final Path appConfigPath;
  private final Path appTargetPath;

  public DomainTest(String domainPrefix, String appPrefix) {
    domainConfigPath = DOMAIN_AND_APP_CONFIG_EXAMPLES_PATH.resolve(domainPrefix + "-original.xml");
    domainTargetPath = DOMAIN_AND_APP_CONFIG_EXAMPLES_PATH.resolve(domainPrefix + ".xml");
    appConfigPath = DOMAIN_AND_APP_CONFIG_EXAMPLES_PATH.resolve(appPrefix + "-original.xml");
    appTargetPath = DOMAIN_AND_APP_CONFIG_EXAMPLES_PATH.resolve(appPrefix + ".xml");
  }

  private List<MigrationStep> appSteps;
  private List<MigrationStep> domainSteps;

  private Document originalDomainDoc;
  private Document domainDoc;
  private Document appDoc;
  private ApplicationModel domainModel;
  private ApplicationModel appModel;

  public List<Element> getElementsFromDocuments(String xPathExpression) {
    List<Element> elements = new ArrayList<>();

    elements.addAll(getElementsFromDocument(originalDomainDoc, xPathExpression, "domain"));
    elements.addAll(getElementsFromDocument(appDoc, xPathExpression));

    return elements;
  }

  @Before
  public void setUp() throws Exception {
    originalDomainDoc = getDocument(this.getClass().getClassLoader().getResource(domainConfigPath.toString()).toURI().getPath());
    domainDoc = getDocument(this.getClass().getClassLoader().getResource(domainConfigPath.toString()).toURI().getPath());
    appDoc = getDocument(this.getClass().getClassLoader().getResource(appConfigPath.toString()).toURI().getPath());

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));
    domainModel = mock(ApplicationModel.class);
    when(domainModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(domainDoc, (String) invocation.getArguments()[0], "domain"));
    when(domainModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(domainDoc, (String) invocation.getArguments()[0], "domain").iterator()
            .next());
    when(domainModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument =
              getElementsFromDocument(domainDoc, (String) invocation.getArguments()[0], "domain");
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(domainModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(domainModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    final Map<Path, Document> domainModelDocs = new HashMap<>();
    domainModelDocs.put(domainConfigPath, domainDoc);
    when(domainModel.getApplicationDocuments()).thenReturn(domainModelDocs);

    doAnswer(invocation -> {
      String prefix = invocation.getArgumentAt(0, String.class);
      String uri = invocation.getArgumentAt(1, String.class);
      String schemaLocation = invocation.getArgumentAt(2, String.class);

      Namespace namespace = Namespace.getNamespace(prefix, uri);
      addNameSpace(namespace, schemaLocation, domainDoc);

      return null;
    }).when(domainModel).addNameSpace(anyString(), anyString(), anyString());

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocuments((String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocuments((String) invocation.getArguments()[0]).iterator()
            .next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocuments((String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    final Map<Path, Document> appDomainModelDocs = new HashMap<>();
    appDomainModelDocs.put(domainConfigPath, originalDomainDoc);
    when(appModel.getDomainDocuments()).thenReturn(appDomainModelDocs);

    doAnswer(invocation -> {
      String prefix = invocation.getArgumentAt(0, String.class);
      String uri = invocation.getArgumentAt(1, String.class);
      String schemaLocation = invocation.getArgumentAt(2, String.class);

      Namespace namespace = Namespace.getNamespace(prefix, uri);
      addNameSpace(namespace, schemaLocation, originalDomainDoc);
      addNameSpace(namespace, schemaLocation, appDoc);

      return null;
    }).when(appModel).addNameSpace(anyString(), anyString(), anyString());

    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();

    coreMigrationTasks.add(new HTTPMigrationTask());
    coreMigrationTasks.add(new DbMigrationTask());

    coreMigrationTasks.add(new EndpointsMigrationTask());
    coreMigrationTasks.add(new JmsDomainMigrationTask());
    coreMigrationTasks.add(new JmsMigrationTask());
    coreMigrationTasks.add(new DomainAppMigrationTask());

    coreMigrationTasks.add(new SpringMigrationTask());
    coreMigrationTasks.add(new HTTPCleanupTask());
    coreMigrationTasks.add(new MigrationCleanTask());
    coreMigrationTasks.add(new PostprocessGeneral());
    coreMigrationTasks.add(new PostprocessMuleApplication());

    appSteps = new ArrayList<>();
    domainSteps = new ArrayList<>();

    for (AbstractMigrationTask task : coreMigrationTasks) {
      if (task.getApplicableProjectTypes().contains(MULE_FOUR_DOMAIN)) {
        for (MigrationStep step : task.getSteps()) {
          if (step instanceof ExpressionMigratorAware) {
            ((ExpressionMigratorAware) step).setExpressionMigrator(expressionMigrator);
          }
          if (step instanceof ApplicationModelContribution) {
            ((ApplicationModelContribution) step).setApplicationModel(domainModel);
          }

          domainSteps.add(step);
        }
      }

      if (task.getApplicableProjectTypes().contains(MULE_FOUR_APPLICATION)) {
        for (MigrationStep step : task.getSteps()) {
          if (step instanceof ExpressionMigratorAware) {
            ((ExpressionMigratorAware) step).setExpressionMigrator(expressionMigrator);
          }
          if (step instanceof ApplicationModelContribution) {
            ((ApplicationModelContribution) step).setApplicationModel(appModel);
          }
          appSteps.add(step);
        }
      }
    }
  }

  @Test
  public void execute() throws Exception {
    for (MigrationStep domainStep : domainSteps) {
      if (domainStep instanceof ApplicationModelContribution) {
        getElementsFromDocument(domainDoc, ((ApplicationModelContribution) domainStep).getAppliedTo().getExpression(), "domain")
            .forEach(node -> domainStep.execute(node, report.getReport()));
      }
    }

    for (MigrationStep appStep : appSteps) {
      if (appStep instanceof ApplicationModelContribution) {
        getElementsFromDocument(originalDomainDoc, ((ApplicationModelContribution) appStep).getAppliedTo().getExpression(),
                                "domain")
                                    .forEach(node -> appStep.execute(node, report.getReport()));
        getElementsFromDocument(appDoc, ((ApplicationModelContribution) appStep).getAppliedTo().getExpression())
            .forEach(node -> appStep.execute(node, report.getReport()));
      }
    }

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String domainXmlString = outputter.outputString(domainDoc);
    String appXmlString = outputter.outputString(appDoc);

    assertThat(domainXmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(domainTargetPath.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());
    assertThat(appXmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(appTargetPath.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());
  }

}
