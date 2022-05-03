/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

/**
 * Models a mule message source or message processor
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public interface FlowComponent {

  Flow getParentFlow();

  Element getXmlElement();
}
