/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.helper;

public final class MavenTestHelper {

  private MavenTestHelper() {
    // Nothing to do
  }

  public static String emptyPom() {
    return "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
        "  <modelVersion>4.0.0</modelVersion>\n" +
        "  <groupId>groupId</groupId>\n" +
        "  <artifactId>artifactid</artifactId>\n" +
        "  <version>1.0-SNAPSHOT</version>\n" +
        "  <packaging>jar</packaging>\n" +
        "  <name>projectName</name>\n" +
        "  \n" +
        "  <properties>\n" +
        "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
        "  </properties>\n" +
        "  \n" +
        "  <build>\n" +
        "    <plugins>\n" +
        "      <plugin>\n" +
        "        <groupId>org.apache.maven.plugins</groupId>\n" +
        "        <artifactId>maven-compiler-plugin</artifactId>\n" +
        "        <version>2.5.1</version>\n" +
        "        <configuration>\n" +
        "          <source>1.6</source>\n" +
        "          <target>1.6</target>\n" +
        "        </configuration>\n" +
        "      </plugin>\n" +
        "    </plugins>\n" +
        "  </build>\n" +
        "</project>";
  }
}
