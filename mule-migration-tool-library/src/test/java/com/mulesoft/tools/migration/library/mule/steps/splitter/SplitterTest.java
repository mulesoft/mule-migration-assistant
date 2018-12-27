package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
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
import com.mulesoft.tools.migration.library.mule.steps.vm.VmNamespaceContribution;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class SplitterTest {

  private static final Path SPLITTER_EXAMPLE_PATHS = Paths.get("mule/apps/splitter-aggregator");

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

  public SplitterTest(String filePrefix) {
    configPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + "-original.xml");
    targetPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + ".xml");
  }

  private AbstractSplitter splitter;
  private VmNamespaceContribution vmNamespaceContribution;
  private AggregatorsNamespaceContribution aggregatorsNamespaceContribution;
  private VmConfig vmConfig;
  private Document doc;
  private ApplicationModel applicationModel;

  @Before
  public void setUp() throws Exception {
      doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
      Map<Path, Document> appDocs = new HashMap<>();
      appDocs.put(configPath, doc);
      applicationModel = mock(ApplicationModel.class);
      when(applicationModel.getApplicationDocuments())
              .thenAnswer(invocation -> appDocs);


    VmInformation vmInformation = new VmInformation("configName");
    splitter = new AbstractSplitter(vmInformation);
    vmNamespaceContribution = new VmNamespaceContribution();
    aggregatorsNamespaceContribution = new AggregatorsNamespaceContribution();
    vmConfig = new VmConfig(vmInformation);
  }

  @Test
  public void execute() throws Exception {
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);

    vmNamespaceContribution.execute(applicationModel, report.getReport());
    aggregatorsNamespaceContribution.execute(applicationModel, report.getReport());

    getElementsFromDocument(document, splitter.getAppliedTo().getExpression())
            .forEach(node -> splitter.execute(node, report.getReport()));

    getElementsFromDocument(document, vmConfig.getAppliedTo().getExpression())
            .forEach(node -> vmConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(document);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }
}
