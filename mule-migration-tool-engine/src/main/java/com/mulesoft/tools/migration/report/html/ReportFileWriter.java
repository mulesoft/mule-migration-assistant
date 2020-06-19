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
