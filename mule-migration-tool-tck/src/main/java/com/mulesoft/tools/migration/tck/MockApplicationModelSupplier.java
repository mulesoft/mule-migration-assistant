/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.tck;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a mock for {@link ApplicationModel} configured for unit tests.
 *
 * @author Mulesoft Inc.
 * @since 0.5.0
 */
public class MockApplicationModelSupplier {

  public static ApplicationModel mockApplicationModel(Document doc, TemporaryFolder temp) throws IOException {
    ApplicationModel appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> nodes = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (nodes.size() != 1) {
            throw new IllegalStateException(format("Found %d nodes for xpath expression '%s'", nodes.size(),
                                                   invocation.getArguments()[0]));
          }
          return nodes.stream().findFirst().orElse(null);
        });
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    return appModel;
  }

  private static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
    return getElementsFromDocument(doc, xPathExpression, "mule");
  }

  private static List<Element> getElementsFromDocument(Document doc, String xPathExpression, String defaultNamespacePrefix) {
    try {
      List<Namespace> namespaces = new ArrayList<>();
      namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

      if (namespaces.stream().noneMatch(n -> defaultNamespacePrefix.equals(n.getPrefix()))) {
        namespaces.add(Namespace.getNamespace(defaultNamespacePrefix, doc.getRootElement().getNamespace().getURI()));
      }

      if (namespaces.stream().anyMatch(n -> "".equals(n.getPrefix()))) {
        Namespace coreNs = namespaces.stream().filter(n -> "".equals(n.getPrefix())).findFirst().get();
        namespaces.remove(coreNs);
        namespaces.add(Namespace.getNamespace("mule", coreNs.getURI()));
      }

      XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
      List<Element> nodes = xpath.evaluate(doc);
      return nodes;
    } catch (IllegalArgumentException e) {
      if (e.getMessage().matches("Namespace with prefix '\\w+' has not been declared.")) {
        return emptyList();
      } else {
        throw e;
      }
    }
  }

}
