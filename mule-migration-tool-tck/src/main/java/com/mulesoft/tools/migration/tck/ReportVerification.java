/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.tck;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.MigrationReport.Level;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jdom2.Element;
import org.junit.rules.ExternalResource;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a verification to be performed after each test on the report entries generated by a migration.
 * <p>
 * Usage:
 *
 * <pre>
 *
 * &#64;Rule
 * public ReportVerification report = new ReportVerification();
 * </pre>
 *
 * @author Mulesoft Inc.
 * @since 0.3
 */
public class ReportVerification extends ExternalResource {

  private MigrationReport report;
  private List<ReportEntryElementWithStack> reportedElements = new ArrayList<>();
  private List<ReportEntryMatcher> reportedEntryMatchers = new ArrayList<>();

  @Override
  protected void before() throws Throwable {
    super.before();

    report = mock(MigrationReport.class);
    Answer<Void> elementRegisterAnswer = inv -> {
      Element element = inv.getArgument(1);
      Element elementToComment = inv.getArgument(2);

      assertThat(elementToComment, not(nullValue()));

      if (inv.getArguments()[0] instanceof String) {
        String entryKey = inv.getArgument(0);
        String[] messageParams = copyOfRange(inv.getArguments(), 3, inv.getArguments().length, String[].class);

        reportedElements
            .add(new ReportEntryElementWithStack(entryKey, element, elementToComment, new Exception(), messageParams));
      } else {
        reportedElements.add(new ReportEntryElementWithStack(elementToComment, new Exception()));
      }

      return null;
    };
    doAnswer(elementRegisterAnswer).when(report).report(anyString(), any(Element.class), any(Element.class), anyVararg());
    doAnswer(elementRegisterAnswer).when(report).report(any(Level.class), any(Element.class), any(Element.class), anyString(),
                                                        anyVararg());
  }

  @Override
  protected void after() {
    for (ReportEntryElementWithStack reportEntryElementWithStack : reportedElements) {
      StringWriter writer = new StringWriter();
      reportEntryElementWithStack.stackTraceContainer.printStackTrace(new PrintWriter(writer));
      assertThat(reportEntryElementWithStack.elementToComment.toString() + lineSeparator() + writer.toString(),
                 reportEntryElementWithStack.elementToComment.getDocument(), not(nullValue()));
    }

    for (ReportEntryMatcher reportEntryMatcher : reportedEntryMatchers) {
      assertThat(reportedElements, reportEntryMatcher);
    }

    super.after();
  }

  public void expectReportEntry(String entryKey, String... messageParams) {
    expectReportEntry(is(entryKey), Matchers.any(Element.class), Matchers.any(Element.class),
                      messageParams.length > 0 ? contains(messageParams) : emptyIterable());
  }

  public void expectReportEntry(Matcher<String> entryKeyMatcher, Matcher<Iterable<? extends String>> messageParamMatchers) {
    expectReportEntry(entryKeyMatcher, Matchers.any(Element.class), Matchers.any(Element.class),
                      messageParamMatchers);
  }

  public void expectReportEntry(Matcher<String> entryKeyMatcher, Matcher<Element> elementMatcher,
                                Matcher<Iterable<? extends String>> messageParamMatchers) {
    expectReportEntry(entryKeyMatcher, elementMatcher, elementMatcher, messageParamMatchers);
  }

  public void expectReportEntry(Matcher<String> entryKeyMatcher, Matcher<Element> elementMatcher,
                                Matcher<Element> elementToCommentMatcher,
                                Matcher<Iterable<? extends String>> messageParamMatchers) {
    reportedEntryMatchers
        .add(new ReportEntryMatcher(entryKeyMatcher, elementMatcher, elementToCommentMatcher, messageParamMatchers));
  }

  public MigrationReport getReport() {
    return report;
  }

  private static class ReportEntryElementWithStack {

    private String entryKey;
    private Element element;
    private Element elementToComment;
    private String[] messageParams;

    private Throwable stackTraceContainer;

    public ReportEntryElementWithStack(Element elementToComment, Throwable stackTraceContainer) {
      this.elementToComment = elementToComment;
      this.stackTraceContainer = stackTraceContainer;
    }

    public ReportEntryElementWithStack(String entryKey, Element element, Element elementToComment, Throwable stackTraceContainer,
                                       String... messageParams) {
      this.entryKey = entryKey;
      this.element = element;
      this.elementToComment = elementToComment;
      this.stackTraceContainer = stackTraceContainer;
      this.messageParams = messageParams;
    }

    @Override
    public String toString() {
      return entryKey + (messageParams.length > 0 ? (" " + asList(messageParams)) : "");
    }

  }

  private static class ReportEntryMatcher extends TypeSafeMatcher<List<ReportEntryElementWithStack>> {

    private Matcher<String> entryKeyMatcher;
    private Matcher<Element> elementMatcher;
    private Matcher<Element> elementToCommentMatcher;
    private Matcher<Iterable<? extends String>> messageParamMatchers;

    private List<ReportEntryElementWithStack> reportedElements;

    public ReportEntryMatcher(Matcher<String> entryKeyMatcher, Matcher<Element> elementMatcher,
                              Matcher<Element> elementToCommentMatcher,
                              Matcher<Iterable<? extends String>> messageParamMatchers) {
      this.entryKeyMatcher = entryKeyMatcher;
      this.elementMatcher = elementMatcher;
      this.elementToCommentMatcher = elementToCommentMatcher;
      this.messageParamMatchers = messageParamMatchers;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("entryKey ");
      entryKeyMatcher.describeTo(description);
      description.appendText("; element ");
      elementMatcher.describeTo(description);
      description.appendText("; elementToComment ");
      elementToCommentMatcher.describeTo(description);
      description.appendText("; messageParam ");
      messageParamMatchers.describeTo(description);

      description.appendValueList("<", ", ", ">", this.reportedElements.stream().map(e -> e.toString()).collect(toList()));
    }

    @Override
    protected boolean matchesSafely(List<ReportEntryElementWithStack> reportedElements) {
      this.reportedElements = reportedElements;

      return reportedElements.stream().anyMatch(reportEntry -> entryKeyMatcher.matches(reportEntry.entryKey)
          && elementMatcher.matches(reportEntry.element)
          && elementToCommentMatcher.matches(reportEntry.elementToComment)
          && messageParamMatchers.matches(asList(reportEntry.messageParams)));
    }
  }
}
