// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.appinventor.components.runtime.RobolectricTestBase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import org.junit.Test;

/**
 * Tests for {@link TransferModelCache}, which resolves where transfer models are published and
 * keeps downloaded copies on disk.
 */
public class TransferModelCacheTest extends RobolectricTestBase {
  private static final String V1_URL =
      "https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224";
  private static final String V3_URL =
      "https://www.kaggle.com/models/google/mobilenet-v3/TfJs/small-100-224-feature-vector/1";

  // ---------------------------------------------------------------------------------------------
  // resolve()
  // ---------------------------------------------------------------------------------------------

  @Test
  public void testResolveKnownV1ByName() {
    TransferModelCache.Source source = TransferModelCache.resolve("mobilenet", null);
    assertNotNull(source);
    assertEquals(V1_URL, source.getBaseUrl());
  }

  @Test
  public void testResolveKnownV3ByName() {
    TransferModelCache.Source source = TransferModelCache.resolve("mobilenet_v3_small", "");
    assertNotNull(source);
    assertEquals(V3_URL, source.getBaseUrl());
  }

  @Test
  public void testResolveKnownNamesIgnoreCase() {
    TransferModelCache.Source source = TransferModelCache.resolve("MobileNet_V3_Small", null);
    assertNotNull(source);
    assertEquals(V3_URL, source.getBaseUrl());
  }

  @Test
  public void testResolveUsesRecordedUrlOverName() {
    TransferModelCache.Source source = TransferModelCache.resolve("mobilenet",
        "https://models.test/custom/model.json");
    assertNotNull(source);
    assertEquals("https://models.test/custom", source.getBaseUrl());
  }

  @Test
  public void testResolveStripsModelJsonQueryAndTrailingSlashes() {
    assertEquals("https://models.test/a/b",
        TransferModelCache.resolve("x", "https://models.test/a/b/model.json?x=1").getBaseUrl());
    assertEquals("https://models.test/a/b",
        TransferModelCache.resolve("x", "https://models.test/a/b///").getBaseUrl());
  }

  @Test
  public void testResolveIgnoresNonHttpsUrlAndFallsBackToName() {
    // Models exported from a local development server record a localhost URL.
    TransferModelCache.Source source = TransferModelCache.resolve("mobilenet_v3_small",
        "http://localhost:5000/mobilenet_v3/model.json");
    assertNotNull(source);
    assertEquals(V3_URL, source.getBaseUrl());
  }

  @Test
  public void testResolveUnknownModelReturnsNull() {
    assertNull(TransferModelCache.resolve("not_a_model", null));
    assertNull(TransferModelCache.resolve("not_a_model", "http://localhost/model.json"));
    assertNull(TransferModelCache.resolve(null, null));
  }

  // ---------------------------------------------------------------------------------------------
  // get()
  // ---------------------------------------------------------------------------------------------

  @Test
  public void testGetReturnsCachedFileWithoutDownloading() throws IOException {
    TransferModelCache cache = new TransferModelCache(getForm());
    TransferModelCache.Source source =
        TransferModelCache.resolve("x", "https://models.test/cached/model.json");
    File expected = writeCached(source, "group1-shard1of2.bin", "weights");

    // The host does not exist, so this only succeeds if the cached copy is used.
    File actual = cache.get(source, "group1-shard1of2.bin");

    assertEquals(expected, actual);
    assertEquals("weights", read(actual));
  }

  @Test
  public void testGetKeepsModelsApartByUrl() throws IOException {
    TransferModelCache cache = new TransferModelCache(getForm());
    TransferModelCache.Source a = TransferModelCache.resolve("x", "https://models.test/a");
    TransferModelCache.Source b = TransferModelCache.resolve("x", "https://models.test/b");
    writeCached(a, "model.json", "model a");
    writeCached(b, "model.json", "model b");

    assertEquals("model a", read(cache.get(a, "model.json")));
    assertEquals("model b", read(cache.get(b, "model.json")));
    assertNotEquals(cache.get(a, "model.json").getParentFile(),
        cache.get(b, "model.json").getParentFile());
  }

  @Test
  public void testGetSharesCacheBetweenInstances() throws IOException {
    TransferModelCache.Source source = TransferModelCache.resolve("x", "https://models.test/s");
    writeCached(source, "model.json", "shared");

    // A second component (or project) asking for the same model reuses the same files.
    assertEquals("shared", read(new TransferModelCache(getForm()).get(source, "model.json")));
    assertEquals("shared", read(new TransferModelCache(getForm()).get(source, "model.json")));
  }

  @Test
  public void testGetRejectsInvalidPaths() {
    TransferModelCache cache = new TransferModelCache(getForm());
    TransferModelCache.Source source = TransferModelCache.resolve("x", "https://models.test/m");
    String[] invalid = {
        "", "../model.json", "a/../../b", "/model.json", "a//b", "model.json?x=1", "a b",
        "model.json\n", "%2e%2e/model.json"
    };
    for (String path : invalid) {
      try {
        cache.get(source, path);
        fail("Expected an IOException for path '" + path + "'");
      } catch (IOException e) {
        assertTrue(e.getMessage(), e.getMessage().contains("Invalid transfer model path"));
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  private File writeCached(TransferModelCache.Source source, String path, String contents)
      throws IOException {
    File root = new File(getForm().getCacheDir(), "transfer-models");
    File file = new File(new File(root, sha1(source.getBaseUrl())), path);
    assertTrue(file.getParentFile().isDirectory() || file.getParentFile().mkdirs());
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(contents.getBytes(Charset.forName("UTF-8")));
    }
    return file;
  }

  private static String read(File file) throws IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    }
    return new String(out.toByteArray(), Charset.forName("UTF-8"));
  }

  private static String sha1(String value) {
    try {
      byte[] bytes = MessageDigest.getInstance("SHA-1")
          .digest(value.getBytes(Charset.forName("UTF-8")));
      StringBuilder hex = new StringBuilder();
      for (byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
