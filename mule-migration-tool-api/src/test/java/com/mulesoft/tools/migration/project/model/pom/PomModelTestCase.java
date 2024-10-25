/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.buildDependency;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.getPomModelDependencies;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.getInternalState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(Enclosed.class)
public class PomModelTestCase {

  @RunWith(PowerMockRunner.class)
  @PrepareForTest(PomModel.PomModelBuilder.class)
  public static class PomModelBuilderTest {

    private PomModel.PomModelBuilder builder;
    private Path pomPathMock;

    @Before
    public void setUp() {
      builder = new PomModel.PomModelBuilder();
      pomPathMock = mock(Path.class, RETURNS_DEEP_STUBS);
      when(pomPathMock.toFile().exists()).thenReturn(true);
    }

    @Test
    public void buildWithNullPath() throws IOException, XmlPullParserException {
      assertThat(builder.withPom(null).build(), not(nullValue()));
    }

    @Test
    public void build() throws Exception {
      Model model = new Model();
      PomModel.PomModelBuilder builderSpy = spy(builder);
      doReturn(model).when(builderSpy, "getModel", pomPathMock);

      PomModel pomModel = builderSpy.withPom(pomPathMock).build();

      verifyPrivate(builderSpy).invoke("getModel", pomPathMock);
      assertThat("Pom model is not the expected", getInternalState(pomModel, "model"), equalTo(model));
    }
  }

  public static class PomModelTest {

    private static final String SIMPLE_POM = "/pommodel/simple-pom/pom.xml";
    private PomModel model;
    private Dependency dependency;

    @Before
    public void setUp() throws URISyntaxException, IOException, XmlPullParserException {
      Path pomPath = Paths.get(getClass().getResource(SIMPLE_POM).toURI());
      model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    }

    @Test
    public void getArtifactId() {
      assertThat("Artifact id is not the expected", model.getArtifactId(), equalTo("my-simple-pom"));
    }

    @Test
    public void getGroupId() {
      assertThat("Group id is not the expected", model.getGroupId(), equalTo("org.simple.pom"));
    }

    @Test
    public void getVersion() {
      assertThat("Version is not the expected", model.getVersion(), equalTo("1.0.0"));
    }

    @Test
    public void getName() {
      assertThat("Name is not the expected", model.getName(), equalTo("Simple Pom"));
    }

    @Test
    public void getPackaging() {
      assertThat("Packaging is not the expected", model.getPackaging(), equalTo("simple-pom-packaging"));
    }

    @Test
    public void getParentGroupId() {
      assertThat("GroupId is not the expected", model.getParent().get().getGroupId(), equalTo("org.simple.pom.parent"));
    }

    @Test
    public void getParentArtifactId() {
      assertThat("ArtifactId is not the expected", model.getParent().get().getArtifactId(), equalTo("parent-mule-api"));
    }

    @Test
    public void getParentVersion() {
      assertThat("Version is not the expected", model.getParent().get().getVersion(), equalTo("1.0.0"));
    }

    @Test
    public void getParentRelativePath() {
      assertThat("RelativePath is not the expected", model.getParent().get().getRelativePath(), equalTo("../pom.xml"));
    }

    @Test
    public void getParentInnerModel() {

      assertThat("InnerModelId is not the expected", model.getParent().get().getInnerModel().getId(),
                 equalTo("org.simple.pom.parent:parent-mule-api:pom:1.0.0"));
    }



    @Test
    public void getProperties() {
      Properties properties = new Properties();
      properties.put("key1", "value1");
      properties.put("key2", "value2");
      Set<Map.Entry<Object, Object>> propertiesEntrySet = properties.entrySet();
      Set<Map.Entry<Object, Object>> modelPropertiesEntrySet = model.getProperties().entrySet();
      assertThat("Not every property is in the expected set", modelPropertiesEntrySet, everyItem(isIn(propertiesEntrySet)));
      assertThat("Does not contain all the expected properties", propertiesEntrySet, everyItem(isIn(modelPropertiesEntrySet)));
    }

    @Test
    public void addProperty() {
      PomModel model = new PomModel();
      String key = "key";
      String value = "value";
      assertThat("Property should not exist in pom model", model.getProperties().getProperty(key), nullValue());
      model.addProperty(key, value);
      assertThat("Property should exist in pom model", model.getProperties().getProperty(key), equalTo(value));
    }

    @Test
    public void removeProperty() {
      PomModel model = new PomModel();
      String key = "key";
      String value = "value";
      model.addProperty(key, value);
      assertThat("Property should exist in pom model", model.getProperties().getProperty(key), equalTo(value));
      model.removeProperty(key);
      assertThat("Property should not exist in pom model", model.getProperties().getProperty(key), nullValue());
    }

    @Test
    public void getDependencies() {
      Set<Dependency> pomModelDependencies = getPomModelDependencies();

      for (Dependency dependency : model.getDependencies()) {
        assertThat("Dependency should be in the expected dependencies set", pomModelDependencies.contains(dependency));
      }

      Set<Dependency> actualPomModelDependencies = new HashSet<>(model.getDependencies());
      for (Dependency dependency : pomModelDependencies) {
        assertThat("Dependency should be in the actual dependencies set", actualPomModelDependencies.contains(dependency));
      }
    }

    @Test
    public void setDependencies() {
      Set<Dependency> pomModelDependencies = getPomModelDependencies();

      model.setDependencies(new ArrayList<>(pomModelDependencies));

      for (Dependency dependency : model.getDependencies()) {
        assertThat("Dependency should be in the expected dependencies set", pomModelDependencies.contains(dependency));
      }

      Set<Dependency> actualPomModelDependencies = new HashSet<>(model.getDependencies());
      for (Dependency dependency : pomModelDependencies) {
        assertThat("Dependency should be in the actual dependencies set", actualPomModelDependencies.contains(dependency));
      }
    }

    @Test
    public void addDependency() {
      dependency = buildDependency("org.dependency", "dependency", "1.0.0", null);
      PomModel model = new PomModel();
      HashSet<Dependency> dependencies = new HashSet<>(model.getDependencies());

      assertThat("Pom model should have no dependencies", dependencies.isEmpty());
      assertThat("Method should return true", model.addDependency(dependency));
      assertThat("Dependency should be contained in pom model dependencies", model.getDependencies(),
                 containsInAnyOrder(dependency));
    }

    @Test
    public void addDependencyTwice() {
      dependency = buildDependency("org.dependency", "dependency", "1.0.0", null);
      PomModel model = new PomModel();
      HashSet<Dependency> dependencies = new HashSet<>(model.getDependencies());

      assertThat("Pom model should have no dependencies", dependencies.isEmpty());
      assertThat("Method should return true", model.addDependency(dependency));
      assertThat("Method should return false", !model.addDependency(dependency));
      assertThat("Pom model should have only one dependency", model.getDependencies().size(), equalTo(1));
    }

    @Test
    public void removeDependency() {
      dependency = buildDependency("org.dependency", "dependency", "1.0.0", null);
      PomModel model = new PomModel();
      model.addDependency(dependency);

      assertThat("Method should return true", model.removeDependency(dependency));
      assertThat("Pom model should have no dependencies", model.getDependencies().size(), equalTo(0));
    }

    @Test
    public void setDistributionManagement() {
      DistributionManagement distributionManagement = new DistributionManagement();
      DeploymentRepository deploymentRepository = new DeploymentRepository();
      deploymentRepository.setName("deploymentRepositoryName");
      distributionManagement.setRepository(deploymentRepository);

      PomModel model = new PomModel();
      model.setDistributionManagement(distributionManagement);

      assertNotNull(model.getDistributionManagement());
      assertNotNull(model.getDistributionManagement().getRepository());
      assertThat(model.getDistributionManagement().getRepository().getName(), is("deploymentRepositoryName"));
    }

    @Test
    public void setDescription() {
      PomModel model = new PomModel();
      String value = "sample";
      model.setDescription(value);

      assertThat("Description should exist on the pom model", model.getDescription(), equalTo(value));
    }

    @Test
    public void setParentGroupId() {
      model.getParent().get().setGroupId("new-group-id");
      assertThat("GroupId is not the expected", model.getParent().get().getGroupId(), equalTo("new-group-id"));
    }

    @Test
    public void setParentArtifactId() {
      model.getParent().get().setArtifactId("new-artifact-id");
      assertThat("GroupId is not the expected", model.getParent().get().getArtifactId(), equalTo("new-artifact-id"));
    }

    @Test
    public void setParentVersion() {
      model.getParent().get().setVersion("2.0.0");
      assertThat("GroupId is not the expected", model.getParent().get().getVersion(), equalTo("2.0.0"));
    }

    @Test
    public void setParentRelativePath() {
      model.getParent().get().setRelativePath("new-path");
      assertThat("RelativePath is not the expected", model.getParent().get().getRelativePath(), equalTo("new-path"));
    }

    @Test
    public void setParent() {

      final Parent parent =
          new Parent.ParentBuilder().withGroupId("myGroupId").withArtifactId("myArtifactId").withVersion("1.0.0").build();
      assertNotNull(model.getParent());
      model.setParent(parent);
      assertNotNull(model.getParent());
      assertThat("Parent is not the expected", parent.getInnerModel(), equalTo(model.getParent().get().getInnerModel()));
    }
  }
}
