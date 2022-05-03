/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationflow;

import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;

/**
 * Models a type of component that emits inbound properties
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public interface PropertiesSource {

  InboundToAttributesTranslator.SourceType getType();
}
