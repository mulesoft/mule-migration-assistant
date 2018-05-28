/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template parser to deal with interpolation cases.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class TemplateParser {

  public static final String WIGGLY_MULE_TEMPLATE_STYLE = "mule";
  private static final char START_EXPRESSION = '#';
  private static final char OPEN_EXPRESSION = '[';
  private static final char CLOSE_EXPRESSION = ']';

  private static final Map<String, PatternInfo> patterns = new HashMap<>();
  public static final String MEL_PREFIX = "mel:";

  /**
   * logger used by this class
   */
  protected static final Logger logger = LoggerFactory.getLogger(TemplateParser.class);

  private final PatternInfo style;

  public static TemplateParser createMuleStyleParser() {
    return new TemplateParser();
  }

  private TemplateParser() {
    this.style = new PatternInfo(WIGGLY_MULE_TEMPLATE_STYLE,
                                 "#\\[((?:#?\\[(?:#?\\[(?:#?\\[(?:#?\\[(?:#?\\[.*?\\]|[^\\[\\]])*?\\]|[^\\[\\]])*?\\]|[^\\[\\]])*?\\]|[^\\[\\]])*?\\]|[^\\[\\]])*?)\\]",
                                 "#[", "]");
  }


  public String translate(String template, ScriptTranslator callback) {
    if (!validateBalanceMuleStyle(template)) {
      return template;
    }

    boolean lastIsBackSlash = false;
    boolean lastStartedExpression = false;
    boolean openSingleQuotes = false;
    boolean openDoubleQuotes = false;

    StringBuilder result = new StringBuilder("#['");
    int currentPosition = 0;
    while (currentPosition < template.length()) {
      char c = template.charAt(currentPosition);

      if (lastStartedExpression && c != OPEN_EXPRESSION) {
        result.append(START_EXPRESSION);
      }

      if (lastIsBackSlash && c != '\'' && c != '"') {
        result.append("\\");
      }

      if (!lastIsBackSlash && c == '\'') {
        openSingleQuotes = !openDoubleQuotes;
      }
      if (!lastIsBackSlash && c == '"') {
        openDoubleQuotes = !openDoubleQuotes;
      }
      if (c == OPEN_EXPRESSION && lastStartedExpression && !(openDoubleQuotes || openSingleQuotes)) {
        int closing = closingBracesPosition(template, currentPosition);
        String enclosingTemplate = template.substring(currentPosition + 1, closing);
        if (enclosingTemplate.startsWith(MEL_PREFIX)) {
          enclosingTemplate = enclosingTemplate.substring(MEL_PREFIX.length());
        }
        String value = callback.translate(enclosingTemplate);
        result.append("$(").append(value).append(")");
        currentPosition = closing;
      } else if (c != START_EXPRESSION && c != '\\') {
        result.append(c == '\'' ? "\\\'" : c);
      }

      lastStartedExpression = c == START_EXPRESSION;
      lastIsBackSlash = c == '\\';
      currentPosition++;
    }

    return result.append("']").toString();
  }

  private int closingBracesPosition(String template, int startingPosition) {
    // This assumes that the template is balanced (simply validate first)
    int openingBraces = 1;
    boolean lastIsBackSlash = false;
    boolean openSingleQuotes = false;
    boolean openDoubleQuotes = false;
    for (int i = startingPosition + 1; i < template.length(); i++) {
      char c = template.charAt(i);
      if (c == CLOSE_EXPRESSION && !(openSingleQuotes || openDoubleQuotes)) {
        openingBraces--;
      } else if (c == OPEN_EXPRESSION && !(openSingleQuotes || openDoubleQuotes)) {
        openingBraces++;
      } else if (!lastIsBackSlash && c == '\'') {
        openSingleQuotes = !openSingleQuotes;
      } else if (!lastIsBackSlash && c == '"') {
        openDoubleQuotes = !openDoubleQuotes;
      }
      lastIsBackSlash = c == '\\';

      if (openingBraces == 0) {
        return i;
      }
    }
    return -1;
  }


  private boolean styleIs(String style) {
    return this.getStyle().getName().equals(style);
  }

  private boolean validateBalanceMuleStyle(String template) {
    Stack<Character> stack = new Stack<>();
    boolean lastStartedExpression = false;
    boolean lastIsBackSlash = false;
    int openBraces = 0;
    int openSingleQuotes = 0;
    int openDoubleQuotes = 0;

    for (int i = 0; i < template.length(); i++) {
      char c = template.charAt(i);
      switch (c) {
        case '\'':
          if (lastIsBackSlash) {
            break;
          }
          if (!stack.empty() && stack.peek().equals('\'')) {
            stack.pop();
            openSingleQuotes--;
          } else {
            stack.push(c);
            openSingleQuotes++;
          }
          break;
        case '"':
          if (lastIsBackSlash) {
            break;
          }
          if (!stack.empty() && stack.peek().equals('"')) {
            stack.pop();
            openDoubleQuotes--;
          } else {
            stack.push(c);
            openDoubleQuotes++;
          }
          break;
        case CLOSE_EXPRESSION:
          if (!stack.empty() && stack.peek().equals(OPEN_EXPRESSION)) {
            stack.pop();
            openBraces--;
          }
          break;
        case OPEN_EXPRESSION:
          if ((lastStartedExpression || openBraces > 0) && !(openDoubleQuotes > 0 || openSingleQuotes > 0)) {
            stack.push(c);
            openBraces++;
          }
          break;
      }
      lastStartedExpression = c == START_EXPRESSION;
      lastIsBackSlash = c == '\\';
    }

    return stack.empty();
  }

  private PatternInfo getStyle() {
    return style;
  }


  /**
   * Migrator of expressions.
   */
  @FunctionalInterface
  public interface ScriptTranslator {

    String translate(String token);
  }

  /**
   * Encloses the pattern information.
   */
  public static class PatternInfo {

    String name;
    String regEx;
    String prefix;
    String suffix;

    PatternInfo(String name, String regEx, String prefix, String suffix) {
      this.name = name;
      this.regEx = regEx;
      if (prefix.length() < 1 || prefix.length() > 2) {
        throw new IllegalArgumentException("Prefix can only be one or two characters long: " + prefix);
      }
      this.prefix = prefix;
      if (suffix.length() != 1) {
        throw new IllegalArgumentException("Suffix can only be one character long: " + suffix);
      }
      this.suffix = suffix;
    }


    public String getName() {
      return name;
    }

    public Pattern getPattern() {
      return Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
    }

  }
}
