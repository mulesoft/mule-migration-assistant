/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.util.Deque;

/**
 * Models a node in the graph
 *
 * @param <T>
 * @param <R>
 *
 * @author Mulesoft Inc.
 */
public interface GraphNode<T, R> {

  void next(T nextComponent);

  void resetNext(T nextComponent);

  Deque<T> next();

  Deque<T> previous();

  T rewire(Deque<R> stack);
}
