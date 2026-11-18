// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.component;

import junit.framework.TestCase;

/**
 * Unit tests for {@link SupersededExtensions}.
 */
public class SupersededExtensionsTest extends TestCase {
  private static final String PIC_TYPE =
      "edu.mit.appinventor.ai.personalimageclassifier.PersonalImageClassifier";
  private static final String PIC_20210315 =
      "FD5A15E02B689D8CEBFB30CF270FD90D";
  private static final String PIC_20210224 =
      "14D5F4657297882AAC9FC5DCD8CF4097";
  private static final String PIC_20190123 =
      "3BA525C20070CF045F33682E6DDB210C";

  public void testOfficialReleaseIsReplaced() {
    assertEquals("PersonalImageClassifier", SupersededExtensions.getBuiltIn(PIC_TYPE, PIC_20210315));
    // Guava's Hashing prints lowercase hex; the storage layer stores uppercase.
    assertEquals("PersonalImageClassifier",
        SupersededExtensions.getBuiltIn(PIC_TYPE, PIC_20210315.toLowerCase()));
  }

  public void testOtherBuildsAreNotReplaced() {
    // Only the latest release is replaced.
    assertNull(SupersededExtensions.getBuiltIn(PIC_TYPE, PIC_20210224));
    assertNull(SupersededExtensions.getBuiltIn(PIC_TYPE, PIC_20190123));
    assertNull(SupersededExtensions.getBuiltIn(PIC_TYPE, "0000"));
    assertNull(SupersededExtensions.getBuiltIn(PIC_TYPE, null));
    assertNull(SupersededExtensions.getBuiltIn("com.example.Other", PIC_20210315));
  }

  public void testFolderNames() {
    assertEquals(PIC_TYPE,
        SupersededExtensions.getTypeForFolder("edu.mit.appinventor.ai.personalimageclassifier"));
    assertEquals(PIC_TYPE, SupersededExtensions.getTypeForFolder(PIC_TYPE));
    assertNull(SupersededExtensions.getTypeForFolder("edu.mit.appinventor.ai"));
  }
}
