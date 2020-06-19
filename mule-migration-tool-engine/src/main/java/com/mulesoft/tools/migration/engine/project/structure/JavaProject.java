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
package com.mulesoft.tools.migration.engine.project.structure;

import java.io.File;
import java.nio.file.Path;

/**
 * Represent a Java project
 * 
 * @author Mulesoft Inc.
 */
public class JavaProject extends BasicProject {

  public static final String srcMainJavaPath = "src" + File.separator + "main" + File.separator + "java";
  public static final String srcMainResourcesPath = "src" + File.separator + "main" + File.separator + "resources";
  public static final String srcTestJavaPath = "src" + File.separator + "test" + File.separator + "java";
  public static final String srcTestResourcesPath = "src" + File.separator + "test" + File.separator + "resources";

  public JavaProject(Path baseFolder) {
    super(baseFolder);
  }

  public Path srcMainJava() {
    return baseFolder.resolve(srcMainJavaPath);
  }

  public Path srcMainResources() {
    return baseFolder.resolve(srcMainResourcesPath);
  }

  public Path srcTestJava() {
    return baseFolder.resolve(srcTestJavaPath);
  }

  public Path srcTestResources() {
    return baseFolder.resolve(srcTestResources());
  }

  // TODO add access to pom file and account for non maven projects

}
