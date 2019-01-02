package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.library.mule.steps.core.PreprocessNamespaces;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmNamespaceContribution;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class SplitterTest {

  private static final Path SPLITTER_EXAMPLE_PATHS = Paths.get("mule/apps/splitter-aggregator");
  private static final String DUMMY_APP_NAME = "splitter-aggregator-app";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
            {"collection-splitter-aggregator-01"},
    });
  }

  private final Path configPath;
  private final Path targetPath;
  private Path fileUnderTestPath;

  public SplitterTest(String filePrefix) {
    configPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + "-original.xml");
    targetPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + ".xml");
  }

  private AbstractSplitter splitter;
  private VmNamespaceContribution vmNamespaceContribution;
  private AggregatorsNamespaceContribution aggregatorsNamespaceContribution;
  private RemoveSyntheticMigrationGlobalElements removeSyntheticMigrationGlobalElements;
  private ApplicationModel applicationModel;

  @Before
  public void setUp() throws Exception {
    buildProject();
    applicationModel = generateAppModel(fileUnderTestPath);

    splitter = new CollectionSplitter();
    splitter.setApplicationModel(applicationModel);
    vmNamespaceContribution = new VmNamespaceContribution();
    aggregatorsNamespaceContribution = new AggregatorsNamespaceContribution();
    removeSyntheticMigrationGlobalElements = new RemoveSyntheticMigrationGlobalElements();
  }

  private void buildProject() throws IOException {
    fileUnderTestPath = temporaryFolder.newFolder(DUMMY_APP_NAME).toPath();
    File app = fileUnderTestPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(configPath.toString());
    FileUtils.copyURLToFile(sample, new File(app, configPath.getFileName().toString()));
  }

  @Test
  public void execute() throws Exception {
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);

    vmNamespaceContribution.execute(applicationModel, report.getReport());
    aggregatorsNamespaceContribution.execute(applicationModel, report.getReport());

    getElementsFromDocument(document, splitter.getAppliedTo().getExpression())
            .forEach(node -> splitter.execute(node, report.getReport()));

    getElementsFromDocument(document, removeSyntheticMigrationGlobalElements.getAppliedTo().getExpression())
            .forEach(node -> removeSyntheticMigrationGlobalElements.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(document);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }
}
