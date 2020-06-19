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
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.Async;
import com.mulesoft.tools.migration.library.mule.steps.core.CatchExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ChoiceExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ChoiceExpressions;
import com.mulesoft.tools.migration.library.mule.steps.core.CompatibilityPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.core.CompositeSource;
import com.mulesoft.tools.migration.library.mule.steps.core.Configuration;
import com.mulesoft.tools.migration.library.mule.steps.core.Enricher;
import com.mulesoft.tools.migration.library.mule.steps.core.ExceptionStrategyRef;
import com.mulesoft.tools.migration.library.mule.steps.core.FirstSuccessful;
import com.mulesoft.tools.migration.library.mule.steps.core.Flow;
import com.mulesoft.tools.migration.library.mule.steps.core.ForEachExpressions;
import com.mulesoft.tools.migration.library.mule.steps.core.ForEachScope;
import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.Logger;
import com.mulesoft.tools.migration.library.mule.steps.core.MuleApp;
import com.mulesoft.tools.migration.library.mule.steps.core.MuleDomain;
import com.mulesoft.tools.migration.library.mule.steps.core.Poll;
import com.mulesoft.tools.migration.library.mule.steps.core.ProcessorChain;
import com.mulesoft.tools.migration.library.mule.steps.core.ProcessorChainReference;
import com.mulesoft.tools.migration.library.mule.steps.core.PropertyPlaceholder;
import com.mulesoft.tools.migration.library.mule.steps.core.RollbackExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ScatterGather;
import com.mulesoft.tools.migration.library.mule.steps.core.SetPayload;
import com.mulesoft.tools.migration.library.mule.steps.core.SetVariable;
import com.mulesoft.tools.migration.library.mule.steps.core.SpringImport;
import com.mulesoft.tools.migration.library.mule.steps.core.TransactionalScope;
import com.mulesoft.tools.migration.library.mule.steps.core.UntilSuccessful;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheHttpCachingStrategy;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheInvalidateKey;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheObjectStoreCachingStrategy;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheScope;
import com.mulesoft.tools.migration.library.mule.steps.ee.EETransform;
import com.mulesoft.tools.migration.library.mule.steps.ee.Tracking;
import com.mulesoft.tools.migration.library.mule.steps.properties.MuleAppProperties;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for Mule Core components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleCoreComponentsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Mule Core Components";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new CompatibilityPomContribution(),
                        new MuleApp(),
                        new MuleDomain(),
                        new Logger(),
                        new ChoiceExceptionStrategy(),
                        new CatchExceptionStrategy(),
                        new RollbackExceptionStrategy(),
                        new TransactionalScope(),
                        new ExceptionStrategyRef(),
                        new ForEachScope(),
                        new ScatterGather(),
                        new Enricher(),
                        new Flow(),
                        new CompositeSource(),
                        new Async(),
                        new FirstSuccessful(),
                        new UntilSuccessful(),
                        new Poll(),
                        new ChoiceExpressions(),
                        new ForEachExpressions(),
                        new SetPayload(),
                        new SetVariable(),
                        new EETransform(),
                        new CacheScope(),
                        new CacheInvalidateKey(),
                        new CacheObjectStoreCachingStrategy(),
                        new CacheHttpCachingStrategy(),
                        new Tracking(),
                        new GenericGlobalEndpoint(),
                        new SpringImport(),
                        new PropertyPlaceholder(),
                        new MuleAppProperties(),
                        new ProcessorChain(),
                        new Configuration(),
                        new ProcessorChainReference());
  }
}
