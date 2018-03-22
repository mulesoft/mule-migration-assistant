/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom.model.predicate;

import com.mulesoft.tools.migration.pom.model.PomModelDependency;
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DependencyPredicateTest {

  public static final String ARTIFACT_ID = "my-dependency";
  public static final String ARTIFACT_ID_1 = "my-dependency1";
  public static final String ARTIFACT_ID_2 = "my-dependency2";
  public static final String GROUP_ID = "org.dependency";
  public static final String GROUP_ID_1 = "org.dependency.1";
  public static final String GROUP_ID_2 = "org.dependency.2";
  public static final String VERSION1 = "1.0.0";
  public static final String VERSION2 = "2.0.0";
  private PomModelDependency dependency;
  private Dependency dependencyToTest;
  private DependencyPredicate predicate;

  @Before
  public void setUp() {
    dependency = new PomModelDependency();
    dependencyToTest = new Dependency();
    predicate = new DependencyPredicate(dependency);
  }

  @Test
  public void notNullTrue() throws Exception {
    assertThat("Predicate should return true", predicate.notNull().test(dependencyToTest), is(true));
  }

  @Test
  public void notNullFalse() throws Exception {
    assertThat("Predicate should return false", predicate.notNull().test(null), is(false));
  }

  @Test
  public void notNullBaseDependencyTrue() throws Exception {
    assertThat("Predicate should return true", predicate.notNull(dependency).test(dependencyToTest), is(true));
  }

  @Test
  public void notNullBaseDependencyFalse() throws Exception {
    assertThat("Predicate should return false", predicate.notNull(null).test(dependencyToTest), is(false));
  }


  @Test
  public void hasSameArtifactIdTrue() throws Exception {
    dependency.setArtifactId(ARTIFACT_ID);
    dependencyToTest.setArtifactId(ARTIFACT_ID);

    assertThat("Predicate should return true", predicate.hasSameArtifactId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameArtifactIdFalse() throws Exception {
    dependency.setArtifactId(ARTIFACT_ID_1);
    dependencyToTest.setArtifactId(ARTIFACT_ID_2);

    assertThat("Predicate should return false", predicate.hasSameArtifactId().test(dependencyToTest), is(false));
  }

  @Test
  public void hasSameArtifactIdBothNull() throws Exception {
    dependency.setArtifactId(null);
    dependencyToTest.setArtifactId(null);

    assertThat("Predicate should return true", predicate.hasSameArtifactId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameArtifactIdOneNull() throws Exception {
    dependency.setArtifactId(ARTIFACT_ID);
    dependencyToTest.setArtifactId(null);

    assertThat("Predicate should return true", predicate.hasSameArtifactId().test(dependencyToTest), is(true));

    dependency.setArtifactId(null);
    dependencyToTest.setArtifactId(ARTIFACT_ID);

    predicate = new DependencyPredicate(dependency);
    assertThat("Predicate should return true", predicate.hasSameArtifactId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameGroupIdTrue() throws Exception {
    dependency.setGroupId(GROUP_ID);
    dependencyToTest.setGroupId(GROUP_ID);

    assertThat("Predicate should return true", predicate.hasSameGroupId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameGroupIdFalse() throws Exception {
    dependency.setGroupId(GROUP_ID_1);
    dependencyToTest.setGroupId(GROUP_ID_2);

    assertThat("Predicate should return false", predicate.hasSameGroupId().test(dependencyToTest), is(false));
  }

  @Test
  public void hasSameGroupIdBothNull() throws Exception {
    dependency.setGroupId(null);
    dependencyToTest.setGroupId(null);

    assertThat("Predicate should return true", predicate.hasSameGroupId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameGroupIdOneNull() throws Exception {
    dependency.setArtifactId(null);
    dependencyToTest.setArtifactId(GROUP_ID);

    assertThat("Predicate should return true", predicate.hasSameGroupId().test(dependencyToTest), is(true));

    dependency.setArtifactId(GROUP_ID);
    dependencyToTest.setArtifactId(null);

    predicate = new DependencyPredicate(dependency);
    assertThat("Predicate should return true", predicate.hasSameGroupId().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameVersionTrue() throws Exception {
    dependency.setVersion(VERSION1);
    dependencyToTest.setVersion(VERSION1);

    assertThat("Predicate should return true", predicate.hasSameVersion().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameVersionFalse() throws Exception {
    dependency.setVersion(VERSION1);
    dependencyToTest.setVersion(VERSION2);

    assertThat("Predicate should return false", predicate.hasSameVersion().test(dependencyToTest), is(false));
  }

  @Test
  public void hasSameVersionBothNull() throws Exception {
    dependency.setVersion(null);
    dependencyToTest.setVersion(null);

    assertThat("Predicate should return true", predicate.hasSameVersion().test(dependencyToTest), is(true));
  }

  @Test
  public void hasSameVersionOneNull() throws Exception {
    dependency.setVersion(null);
    dependencyToTest.setVersion(VERSION1);

    DependencyPredicate predicate = new DependencyPredicate(dependency);
    assertThat("Predicate should return true", predicate.hasSameVersion().test(dependencyToTest), is(true));

    dependency.setVersion(VERSION1);
    dependencyToTest.setVersion(null);

    predicate = new DependencyPredicate(dependency);
    assertThat("Predicate should return true", predicate.hasSameVersion().test(dependencyToTest), is(true));
  }
}
