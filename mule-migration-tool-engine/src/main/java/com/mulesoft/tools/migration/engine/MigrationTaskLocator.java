/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.mulesoft.tools.migration.library.mule.steps.compression.CompressionMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.AmqpMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.BatchMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.DbMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.DomainAppMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.EmailMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.EndpointsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ExpressionTransformerMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.FileMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.FiltersMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.FtpMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPCleanupTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.JmsDomainMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.JmsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.JsonMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MigrationCleanTask;
import com.mulesoft.tools.migration.library.mule.tasks.MuleCoreComponentsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MuleDeprecatedCoreComponentsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ObjectStoreMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessGeneral;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.PropertiesMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.QuartzMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.RequestReplyMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ScriptingMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SecurePropertiesMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SecurityCrc32MigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SecurityFiltersMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SecurityOAuth2ProviderMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SftpMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SocketsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SplitterAggregatorTask;
import com.mulesoft.tools.migration.library.mule.tasks.SpringMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.TransformersMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.VMMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ValidationMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.VmDomainMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.WscMigrationTask;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * The goal of this class is to locate migration tasks
 *
 * @author Mulesoft Inc.
 */
public class MigrationTaskLocator {

  private final String from;
  private final String to;

  public MigrationTaskLocator(String from, String to) {
    checkArgument(from != null, "From must not be null");
    checkArgument(to != null, "To must not be null");

    this.from = from;
    this.to = to;
  }

  public List<AbstractMigrationTask> locate() {
    List<AbstractMigrationTask> migrationTasks = newArrayList(new PreprocessMuleApplication());
    migrationTasks.addAll(getCoreMigrationTasks());
    migrationTasks.addAll(getMigrationTasks());
    migrationTasks.addAll(getCoreAfterMigrationTasks());
    return migrationTasks.stream().filter(mt -> shouldNotFilterTask(mt)).collect(Collectors.toList());
  }

  protected List<AbstractMigrationTask> getMigrationTasks() {
    ServiceLoader<AbstractMigrationTask> load = ServiceLoader.load(AbstractMigrationTask.class);
    return newArrayList(load);
  }

  private Boolean shouldNotFilterTask(MigrationTask migrationTask) {
    if (!isProperlyCategorized(migrationTask)) {
      // TODO log;
      return FALSE;
    }
    if (isVersionGreaterOrEquals(migrationTask.getFrom(), from) && isVersionGreaterOrEquals(to, migrationTask.getTo())) {
      return TRUE;
    }
    return FALSE;
  }

  private Boolean isProperlyCategorized(MigrationTask migrationTask) {
    if (migrationTask.getFrom() != null && migrationTask.getTo() != null && migrationTask.getProjectType() != null) {
      return TRUE;
    }
    return FALSE;
  }

  public List<AbstractMigrationTask> getCoreMigrationTasks() {
    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();

    coreMigrationTasks.add(new SecurePropertiesMigrationTask());
    coreMigrationTasks.add(new SecurityCrc32MigrationTask());
    coreMigrationTasks.add(new SecurityFiltersMigrationTask());
    coreMigrationTasks.add(new PropertiesMigrationTask());
    coreMigrationTasks.add(new MuleCoreComponentsMigrationTask());
    coreMigrationTasks.add(new SplitterAggregatorTask());
    coreMigrationTasks.add(new BatchMigrationTask());
    coreMigrationTasks.add(new ValidationMigrationTask());
    coreMigrationTasks.add(new RequestReplyMigrationTask());
    coreMigrationTasks.add(new QuartzMigrationTask());

    coreMigrationTasks.add(new HTTPMigrationTask());
    coreMigrationTasks.add(new EmailMigrationTask());
    coreMigrationTasks.add(new SocketsMigrationTask());
    coreMigrationTasks.add(new WscMigrationTask());
    coreMigrationTasks.add(new DbMigrationTask());
    coreMigrationTasks.add(new ObjectStoreMigrationTask());

    coreMigrationTasks.add(new FileMigrationTask());
    coreMigrationTasks.add(new FtpMigrationTask());
    coreMigrationTasks.add(new SftpMigrationTask());
    coreMigrationTasks.add(new EndpointsMigrationTask());
    coreMigrationTasks.add(new JmsDomainMigrationTask());
    coreMigrationTasks.add(new JmsMigrationTask());
    coreMigrationTasks.add(new AmqpMigrationTask());
    coreMigrationTasks.add(new VmDomainMigrationTask());
    coreMigrationTasks.add(new VMMigrationTask());

    coreMigrationTasks.add(new CompressionMigrationTask());
    coreMigrationTasks.add(new ScriptingMigrationTask());
    coreMigrationTasks.add(new JsonMigrationTask());

    coreMigrationTasks.add(new SecurityOAuth2ProviderMigrationTask());

    coreMigrationTasks.add(new DomainAppMigrationTask());
    coreMigrationTasks.add(new MuleDeprecatedCoreComponentsMigrationTask());
    coreMigrationTasks.add(new MunitMigrationTask());
    coreMigrationTasks.add(new TransformersMigrationTask());
    coreMigrationTasks.add(new ExpressionTransformerMigrationTask());
    // Spring has to run after MUnit, since MUnit in Mule 3 has some custom spring components that are removed by the migrator

    return coreMigrationTasks;
  }

  public List<AbstractMigrationTask> getCoreAfterMigrationTasks() {
    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();

    coreMigrationTasks.add(new FiltersMigrationTask());

    // Spring has to run after MUnit, since MUnit in Mule 3 has some custom spring components that are removed by the migrator
    coreMigrationTasks.add(new SpringMigrationTask());
    coreMigrationTasks.add(new HTTPCleanupTask());
    coreMigrationTasks.add(new MigrationCleanTask());
    coreMigrationTasks.add(new PostprocessGeneral());
    coreMigrationTasks.add(new PostprocessMuleApplication());
    return coreMigrationTasks;
  }
}


