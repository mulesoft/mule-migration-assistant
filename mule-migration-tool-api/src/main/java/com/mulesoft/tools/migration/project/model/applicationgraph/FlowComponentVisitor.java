/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

/**
 * Visitor to perform operations over FlowComponents
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public interface FlowComponentVisitor {

  void visitMessageProcessor(MessageProcessor messageProcessor);

  void visitSetPropertyProcessor(SetPropertyProcessor processor);

  void visitCopyPropertiesProcessor(CopyPropertiesProcessor processor);

  void visitPropertiesSourceComponent(PropertiesSourceComponent processor, boolean responseComponent);

  void visitRemovePropertyProcessor(RemovePropertyProcessor removePropertyProcessor);
}
