/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.AbstractFtpEndpoint.FTP_NS_URI;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the global endpoints of the ftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + FTP_NS_URI + "' and local-name() = 'endpoint']";

  @Override
  public String getDescription() {
    return "Update FTP global endpoints.";
  }

  public FtpGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return Namespace.getNamespace("ftp", "http://www.mulesoft.org/schema/mule/ftp");
  }

}
