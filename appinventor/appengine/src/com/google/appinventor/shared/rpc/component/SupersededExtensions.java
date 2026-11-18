// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extensions that have been replaced by a built-in component with the same name and API.
 *
 * <p>Only official releases are replaced, recognized by the MD5 of the extension's classes.jar.
 * That file is stored unchanged both in the .aix and in projects that use the extension, and the
 * server already stores the MD5 of every asset file, so the same hash identifies a release on
 * import and on project load without reading the file. A project that bundles one of these
 * releases has the extension removed when it is loaded, so its components and blocks attach to the
 * built-in component. Importing one of these releases is refused. Other builds of the same
 * extension, such as forks, keep working as extensions.
 */
public final class SupersededExtensions {

  /** The file whose hash identifies a release, relative to the extension's folder. */
  public static final String FINGERPRINT_FILE = "classes.jar";

  private static final class Replacement {
    final String builtIn;
    final Set<String> releaseHashes;

    Replacement(String builtIn, String... releaseHashes) {
      this.builtIn = builtIn;
      this.releaseHashes = new HashSet<>(Arrays.asList(releaseHashes));
    }
  }

  /** Maps the extension's fully qualified type to its replacement. */
  private static final Map<String, Replacement> REPLACEMENTS = new HashMap<>();

  static {
    // Releases from https://github.com/mit-cml/extensions (data/extensions/PersonalImageClassifier.aix).
    // Only the latest release, 20210315, is replaced.
    REPLACEMENTS.put("edu.mit.appinventor.ai.personalimageclassifier.PersonalImageClassifier",
        new Replacement("PersonalImageClassifier",
            // 20210315
            "FD5A15E02B689D8CEBFB30CF270FD90D"));
  }

  private SupersededExtensions() {
  }

  /**
   * Returns the fully qualified type of the replaceable extension stored in the given folder under
   * assets/external_comps, or null if there is none. Extensions are stored in a folder named after
   * their package, or in older projects after their fully qualified type. The caller must still
   * check the release with {@link #getBuiltIn(String, String)}.
   */
  public static String getTypeForFolder(String folderName) {
    for (String type : REPLACEMENTS.keySet()) {
      String packageName = type.substring(0, type.lastIndexOf('.'));
      if (folderName.equals(type) || folderName.equals(packageName)) {
        return type;
      }
    }
    return null;
  }

  /**
   * Returns the built-in component that replaces the extension with the given fully qualified
   * type, or null if the extension has no replacement or the release is not an official one.
   *
   * @param extensionType the extension's fully qualified type
   * @param fingerprint the hex MD5 of the extension's {@link #FINGERPRINT_FILE}
   */
  public static String getBuiltIn(String extensionType, String fingerprint) {
    Replacement replacement = REPLACEMENTS.get(extensionType);
    if (replacement == null || fingerprint == null
        || !replacement.releaseHashes.contains(fingerprint.toUpperCase())) {
      return null;
    }
    return replacement.builtIn;
  }
}
