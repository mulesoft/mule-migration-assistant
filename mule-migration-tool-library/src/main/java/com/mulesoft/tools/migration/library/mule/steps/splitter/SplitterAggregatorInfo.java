/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements.MIGRATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static java.lang.Math.abs;
import static java.nio.file.Paths.get;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

import java.util.Objects;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.mule.runtime.api.util.LazyValue;

/**
 * POJO to store information about the splitter being migrated.
 * It handles all naming related to the migrated splitter to make sure that 2 don't have the same name.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SplitterAggregatorInfo {

  private static final String SPLITTER_GLOBAL_VALUES = "splitterGlobalValues";
  private static final String SPLITTER_GLOBAL_INDEXES = "splitterGlobalIndexes";

  private Element splitterElement;
  private ApplicationModel applicationModel;
  private LazyValue<Integer> documentIdLazyValue;
  private LazyValue<Integer> splitterIndexLazyValue;

  public SplitterAggregatorInfo(Element splitterElement, ApplicationModel applicationModel) {
    this.splitterElement = splitterElement;
    this.applicationModel = applicationModel;

    this.documentIdLazyValue =
        new LazyValue<>(() -> abs(Objects.hashCode(get(applicationModel.getProjectBasePath().toUri().toString())
            .relativize(get(splitterElement.getDocument().getBaseURI())).toString())));

    this.splitterIndexLazyValue = new LazyValue<>(
                                                  () -> {
                                                    Optional<Element> splitterGlobalValuesElementOptional = applicationModel
                                                        .getNodeOptional("//*[local-name()='" + SPLITTER_GLOBAL_VALUES
                                                            + "' and namespace-uri()='" + MIGRATION_NAMESPACE.getURI() + "']");
                                                    Element splitterGlobalValuesElement =
                                                        splitterGlobalValuesElementOptional.orElseGet(() -> {
                                                          Element globalValues =
                                                              new Element(SPLITTER_GLOBAL_VALUES, MIGRATION_NAMESPACE);
                                                          Element globalIndexes =
                                                              new Element(SPLITTER_GLOBAL_INDEXES, MIGRATION_NAMESPACE);
                                                          globalValues.addContent(globalIndexes);
                                                          addTopLevelElement(globalValues, splitterElement.getDocument());
                                                          return globalValues;
                                                        });
                                                    int newId = 0;
                                                    try {
                                                      Element globalIndexesElement = splitterGlobalValuesElement
                                                          .getChild(SPLITTER_GLOBAL_INDEXES, MIGRATION_NAMESPACE);
                                                      Attribute newIdAttribute =
                                                          globalIndexesElement.getAttribute(splitterElement.getName());
                                                      if (newIdAttribute == null) {
                                                        newIdAttribute = new Attribute(splitterElement.getName(), "-1");
                                                        globalIndexesElement.setAttribute(newIdAttribute);
                                                      }
                                                      newId = newIdAttribute.getIntValue() + 1;
                                                      newIdAttribute.setValue(Integer.toString(newId));
                                                    } catch (DataConversionException e) {
                                                      //
                                                    }
                                                    return newId;
                                                  });
  }

  private int getSplitterIndex() {
    return splitterIndexLazyValue.get();
  }

  public Element getSplitterElement() {
    return this.splitterElement;
  }

  public String getGroupSizeVariableName() {
    return this.getSplitterUniqueId() + "-group-size";
  }

  public String getAggregatorName() {
    return this.getSplitterUniqueId() + "-aggregator";
  }

  public String getAggregatorListenerFlowName() {
    return this.getAggregatorName() + "-listener-flow";
  }

  public String getAggregationCompleteVariableName() {
    return this.getAggregatorName() + "-complete-aggregation";
  }

  public String getAggregationCompleteExpression() {
    return "#[vars.'" + getAggregationCompleteVariableName() + "' == false]";
  }

  public String getAggregationVariableName() {
    return this.getAggregatorName() + "-aggregation";
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
