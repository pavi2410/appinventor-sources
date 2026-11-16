// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import java.util.HashMap;
import java.util.Map;

/**
 * Extensions that have been replaced by a built-in component with the same name and API.
 *
 * <p>A project that bundles one of these extensions has the extension removed when it is loaded,
 * so its components and blocks attach to the built-in component. Importing one of these
 * extensions is refused.
 */
public final class SupersededExtensions {

  /** Maps the extension's fully qualified type to the built-in component's type name. */
  private static final Map<String, String> BUILT_IN_BY_EXTENSION_TYPE = new HashMap<>();

  static {
    BUILT_IN_BY_EXTENSION_TYPE.put(
        "edu.mit.appinventor.ai.personalimageclassifier.PersonalImageClassifier",
        "PersonalImageClassifier");
  }

  private SupersededExtensions() {
  }

  /**
   * Returns the built-in component that replaces the extension with the given fully qualified
   * type, or null if the extension has not been replaced.
   */
  public static String getBuiltInForType(String extensionType) {
    return BUILT_IN_BY_EXTENSION_TYPE.get(extensionType);
  }

  /**
   * Returns the built-in component that replaces the extension stored in the given folder under
   * assets/external_comps, or null if the extension has not been replaced. Extensions are stored
   * in a folder named after their package, or in older projects after their fully qualified type.
   */
  public static String getBuiltInForFolder(String folderName) {
    for (Map.Entry<String, String> entry : BUILT_IN_BY_EXTENSION_TYPE.entrySet()) {
      String type = entry.getKey();
      String packageName = type.substring(0, type.lastIndexOf('.'));
      if (folderName.equals(type) || folderName.equals(packageName)) {
        return entry.getValue();
      }
    }
    return null;
  }
}
