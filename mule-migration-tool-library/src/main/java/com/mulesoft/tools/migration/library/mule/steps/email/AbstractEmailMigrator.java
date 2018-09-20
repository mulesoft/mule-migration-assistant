/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Namespace;


public abstract class AbstractEmailMigrator extends AbstractApplicationModelMigrationStep {

  public static final Namespace EMAIL_NAMESPACE = Namespace.getNamespace("email", "http://www.mulesoft.org/schema/mule/email");

}
