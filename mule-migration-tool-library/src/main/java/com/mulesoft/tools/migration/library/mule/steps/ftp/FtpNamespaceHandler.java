/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeConfig.FTP_EE_NAMESPACE;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.jdom2.Namespace;

/**
 * Fixes the FTP namespace declarations
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpNamespaceHandler implements PomContribution {

  private static final String FTP_NAMESPACE_PREFIX = "ftp";
  private static final String FTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ftp";
  public static final Namespace FTP_NAMESPACE = Namespace.getNamespace(FTP_NAMESPACE_PREFIX, FTP_NAMESPACE_URI);

  private ApplicationModel applicationModel;

  @Override
  public String getDescription() {
    return "Update FTP connector namespace.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    getApplicationModel().getApplicationDocuments().values().forEach(doc -> {
      if (doc.getRootElement().getAdditionalNamespaces().contains(FTP_EE_NAMESPACE)) {
        getApplicationModel().removeNameSpace(FTP_EE_NAMESPACE,
                                              "http://www.mulesoft.org/schema/mule/ee/ftp/current/mule-ftp-ee.xsd", doc);
        getApplicationModel().addNameSpace(FTP_NAMESPACE, "http://www.mulesoft.org/schema/mule/ftp/current/mule-ftp.xsd", doc);
      }

    });
  }

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel appModel) {
    this.applicationModel = appModel;
  }
}
