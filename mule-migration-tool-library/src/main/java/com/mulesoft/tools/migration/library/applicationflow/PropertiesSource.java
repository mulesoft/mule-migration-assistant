package com.mulesoft.tools.migration.library.applicationflow;

import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;

public interface PropertiesSource {

  InboundToAttributesTranslator.SourceType getType();
}
