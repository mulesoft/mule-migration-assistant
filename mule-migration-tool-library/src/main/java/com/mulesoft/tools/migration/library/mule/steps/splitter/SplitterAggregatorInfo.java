package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes.MIGRATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static java.lang.Math.abs;
import static java.nio.file.Paths.get;
import org.mule.runtime.api.util.LazyValue;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

import java.util.Objects;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

public class SplitterAggregatorInfo {

  private static final String SPLITTER_GLOBAL_VALUES = "splitterGlobalValues";
  private static final String SPLITTER_GLOBAL_INDEX = "splitterGlobalIndex";

  private Element splitterElement;
  private ApplicationModel applicationModel;
  private LazyValue<Integer> documentIdLazyValue;
  private LazyValue<Integer> splitterIndexLazyValue;

  public SplitterAggregatorInfo(Element splitterElement, ApplicationModel applicationModel) {
    this.splitterElement = splitterElement;
    this.applicationModel = applicationModel;

    this.documentIdLazyValue = new LazyValue<>(() -> abs(Objects.hashCode(get(applicationModel.getProjectBasePath().toUri().toString()).relativize(get(splitterElement.getDocument().getBaseURI())).toString())));

    this.splitterIndexLazyValue = new LazyValue<>(
            () -> {
              Optional<Element> splitterGlobalValuesElementOptional = applicationModel.getNodeOptional("//*[local-name()='" + SPLITTER_GLOBAL_VALUES + "']");
              Element splitterGlobalValuesElement = splitterGlobalValuesElementOptional.orElseGet(() -> {
                Element globalValues = new Element(SPLITTER_GLOBAL_VALUES, MIGRATION_NAMESPACE);
                globalValues.setAttribute(SPLITTER_GLOBAL_INDEX, "-1");
                addTopLevelElement(globalValues, splitterElement.getDocument());
                return globalValues;
              });
              int newId = 0;
              try {
                Attribute newIdAttribute = splitterGlobalValuesElement.getAttribute(SPLITTER_GLOBAL_INDEX);
                newId = newIdAttribute.getIntValue() + 1;
                newIdAttribute.setValue(Integer.toString(newId));
              }catch (DataConversionException e) {
                //
              }
              return newId;
            }
    );
  }

  public int getSplitterIndex() {
    return splitterIndexLazyValue.get();
  }

  public Element getSplitterElement() {
    return this.splitterElement;
  }

  public String getGrupSizeVariableName() {
    return this.getSplitterUniqueId() + "-group-size";
  }

  public String getAggregatorName() {
    return this.getSplitterUniqueId() + "-aggregator";
  }

  public String getVmQueueName() {
    return this.getSplitterUniqueId() + "-vm-queue";
  }

  public String getVmConfigName() {
    return "splitter-aggregator-vm-config" + this.getDocumentUniqueId();
  }

  private int getDocumentUniqueId() {
    return this.documentIdLazyValue.get();
  }

  private String getSplitterUniqueId() {
    return splitterElement.getName() + this.getSplitterIndex();
  }

}
