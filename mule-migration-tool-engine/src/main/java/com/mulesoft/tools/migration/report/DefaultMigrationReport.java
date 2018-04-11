/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 * Default implementation of a {@link MigrationReport}.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMigrationReport implements MigrationReport {

  private XMLOutputter outp = new XMLOutputter();

  @Override
  public void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks) {
    int i = 0;

    elementToComment.addContent(i++, new Comment(level.name() + ": " + message));
    elementToComment.addContent(i++, new Comment("    For more infromation refer to:"));

    for (String link : documentationLinks) {
      elementToComment.addContent(i++, new Comment("        * " + link));
    }

    elementToComment.addContent(i++, new Comment(outp.outputString(element)));
  }

}
