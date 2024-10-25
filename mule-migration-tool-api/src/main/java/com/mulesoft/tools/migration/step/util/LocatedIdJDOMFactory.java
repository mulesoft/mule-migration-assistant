/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_ID_ATTRIBUTE;

import java.util.UUID;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedJDOMFactory;

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
    return addMigrationIdAttribute(element);
  }

  @Override
  public Element element(int line, int col, String name) {
    Element element = super.element(line, col, name);
    return addMigrationIdAttribute(element);
  }

  @Override
  public Element element(int line, int col, String name, String uri) {
    Element element = super.element(line, col, name, uri);
    return addMigrationIdAttribute(element);
  }

  @Override
  public Element element(int line, int col, String name, String prefix, String uri) {
    Element element = super.element(line, col, name, prefix, uri);
    return addMigrationIdAttribute(element);
  }

  private Element addMigrationIdAttribute(Element element) {
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute(MIGRATION_ID_ATTRIBUTE, UUID.randomUUID().toString()));
    return element;
  }

}
