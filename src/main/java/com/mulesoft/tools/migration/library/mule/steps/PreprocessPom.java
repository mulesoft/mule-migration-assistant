package com.mulesoft.tools.migration.library.mule.steps;

import com.mulesoft.tools.migration.engine.step.category.PomContribution;
import com.mulesoft.tools.migration.pom.Dependency;
import com.mulesoft.tools.migration.pom.PomModel;

import java.util.List;

public class PreprocessPom implements PomContribution {

  @Override
  public String getDescription() {
    return "Remove mule dependencies from pom";
  }

  @Override
  public void execute(PomModel pomModel) {
    List<Dependency> dependencies = pomModel.getDependencies();
    dependencies.removeIf(d -> d.getGroupId().startsWith("org.mule") || d.getGroupId().startsWith("com.mulesoft"));
    pomModel.setDependencies(dependencies);
  }
}
