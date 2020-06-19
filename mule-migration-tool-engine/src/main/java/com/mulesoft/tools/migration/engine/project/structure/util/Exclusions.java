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
package com.mulesoft.tools.migration.engine.project.structure.util;

import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;

import java.io.File;

/**
 * Files/Folders excluded from migration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum Exclusions {

  CLASSPATH(File.separator + ".classpath"),

  GIT(File.separator + ".gitignore"),

  PROJECT(File.separator + ".project"),

  MULE(File.separator + ".mule"),

  SETTINGS(File.separator + ".settings"),

  MULE_PROJECT(File.separator + "mule-project.xml"),

  TARGET(File.separator + "target"),

  CLASSES(File.separator + "classes"),

  // Mule resources are processed elsewhere, not copied as they are

  MULE_THREE_APP(File.separator + MuleThreeApplication.srcMainConfigurationPath),

  MULE_THREE_TEST(File.separator + MuleThreeApplication.srcTestsConfigurationPath),

  MULE_FOUR_APP(File.separator + MuleFourApplication.srcMainConfigurationPath),

  MULE_FOUR_TEST(File.separator + MuleFourApplication.srcTestConfigurationPath),

  MULE_THREE_DOMAIN(File.separator + MuleThreeDomain.srcMainConfigurationPath),

  MULE_FOUR_DOMAIN(File.separator + MuleFourDomain.srcMainConfigurationPath);

  private String exclusion;

  Exclusions(String exclusion) {
    this.exclusion = exclusion;
  }

  public String exclusion() {
    return exclusion;
  }

}
