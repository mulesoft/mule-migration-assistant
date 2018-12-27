package com.mulesoft.tools.migration.library.mule.steps.splitter;

import java.util.HashSet;
import java.util.Set;

public class VmInformation {

  private String configurationName;
  private Set<String> queueNames;

  public VmInformation(String configurationName) {
    this.queueNames = new HashSet<>();
    this.configurationName = configurationName;
  }

  public void addQueue(String newQueue) {
    this.queueNames.add(newQueue);
  }

  public Set<String> getQueues() {
    return queueNames;
  }

  public String getConfigurationName() {
    return this.configurationName;
  }
}
