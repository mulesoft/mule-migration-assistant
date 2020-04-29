package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class ProcessorChainReferenceTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private final Path configPath = CORE_CONFIG_EXAMPLES_PATH.resolve("processor-chain-reference-01-original.xml");
  private final Path targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve("processor-chain-reference-01.xml");

  private ProcessorChain processorChain;
  private ProcessorChainReference processorChainReference;

  @Before
  public void setUp() throws Exception {
    ApplicationModel appModel = mock(ApplicationModel.class);
    processorChain = new ProcessorChain();
    processorChainReference = new ProcessorChainReference();
    processorChain.setApplicationModel(appModel);
    processorChainReference.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    getElementsFromDocument(doc, processorChain.getAppliedTo().getExpression())
        .forEach(node -> processorChain.execute(node, report.getReport()));

    getElementsFromDocument(doc, processorChainReference.getAppliedTo().getExpression())
        .forEach(node -> processorChainReference.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
