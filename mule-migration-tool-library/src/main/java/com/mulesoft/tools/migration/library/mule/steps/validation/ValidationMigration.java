/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate Validation Module
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationMigration extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String VALIDATION_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/validation";
  public static final Namespace VALIDATION_NAMESPACE = Namespace.getNamespace("validation", VALIDATION_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + VALIDATION_NAMESPACE_URI + "']";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate Validation Module.";
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  public ValidationMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(VALIDATION_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("email"), getExpressionMigrator());
    migrateExpression(element.getAttribute("ip"), getExpressionMigrator());
    migrateExpression(element.getAttribute("url"), getExpressionMigrator());
    migrateExpression(element.getAttribute("time"), getExpressionMigrator());
    migrateExpression(element.getAttribute("pattern"), getExpressionMigrator());
    migrateExpression(element.getAttribute("locale"), getExpressionMigrator());
    migrateExpression(element.getAttribute("value"), getExpressionMigrator());
    migrateExpression(element.getAttribute("regex"), getExpressionMigrator());
    migrateExpression(element.getAttribute("caseSensitive"), getExpressionMigrator());
    migrateExpression(element.getAttribute("min"), getExpressionMigrator());
    migrateExpression(element.getAttribute("max"), getExpressionMigrator());
    migrateExpression(element.getAttribute("expression"), getExpressionMigrator());
    migrateExpression(element.getAttribute("minValue"), getExpressionMigrator());
    migrateExpression(element.getAttribute("maxValue"), getExpressionMigrator());
    migrateExpression(element.getAttribute("numberType"), getExpressionMigrator());

    if (element.getName().equals("is-empty")) {
      element.setName("is-empty-collection");
    } else if (element.getName().equals("is-not-empty")) {
      element.setName("is-not-empty-collection");
    }
  }

  protected void addValidationsModule(Document document) {
    addValidationNamespace(document);
    addValidationDependency(getApplicationModel().getPomModel().get());
  }

  public static void addValidationNamespace(Document document) {
    addNameSpace(VALIDATION_NAMESPACE, "http://www.mulesoft.org/schema/mule/validation/current/mule-validation.xsd",
                 document);
  }
}
