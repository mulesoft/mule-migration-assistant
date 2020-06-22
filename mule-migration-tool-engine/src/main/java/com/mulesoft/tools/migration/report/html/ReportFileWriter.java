/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Handles report file creation and generates the HTML report file name
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ReportFileWriter {

  public String getHtmlFileName(String resourceName, Integer fileCount) {
    String htmlFileName = resourceName;
    if (htmlFileName.contains(".xml")) {
      htmlFileName = htmlFileName.substring(0, htmlFileName.indexOf(".xml")) + "-" + fileCount + ".html";
    } else {
      htmlFileName = htmlFileName + "-" + fileCount + ".html";
    }
    return htmlFileName;
  }

  public void writeToFile(File file, String content) throws IOException {
    checkNotNull(file, "File cannot be null");
    file.getParentFile().mkdirs();
    file.createNewFile();


    Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
    BufferedWriter bw = new BufferedWriter(writer);
    bw.write(content);

    bw.flush();
    bw.close();
    writer.close();
  }

  public void copyFile(String originPath, File destination) throws IOException {
    InputStream resource = this.getClass().getClassLoader().getResourceAsStream(originPath);
    FileUtils.copyInputStreamToFile(resource, destination);
  }
}
