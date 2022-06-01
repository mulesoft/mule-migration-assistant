/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedJDOMFactory;

import java.util.UUID;

/**
 * Element factory that includes a synthetic id to match elements
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class LocatedIdJDOMFactory extends LocatedJDOMFactory {

  @Override
  public Element element(int line, int col, String name, Namespace namespace) {
    Element element = super.element(line, col, name, namespace);
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute("migrationId", UUID.randomUUID().toString()));
    return element;
  }

  @Override
  public Element element(int line, int col, String name) {
    Element element = super.element(line, col, name);
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute("migrationId", UUID.randomUUID().toString()));
    return element;
  }

  @Override
  public Element element(int line, int col, String name, String uri) {
    Element element = super.element(line, col, name, uri);
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute("migrationId", UUID.randomUUID().toString()));
    return element;
  }

  @Override
  public Element element(int line, int col, String name, String prefix,
                         String uri) {
    Element element = super.element(line, col, name, prefix, uri);
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute("migrationId", UUID.randomUUID().toString()));
    return element;
  }

}
