/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * Migration Step that works over the application model adding new namespaces.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface NamespaceContribution extends MigrationStep<ApplicationModel> {

}
