// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.shadows.ShadowActivityCompat;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

/**
 * Tests for the PersonalImageClassifier component: how it wires up its WebView, serves the user's
 * model file and the transfer model to the page, and reports errors.
 */
@Config(shadows = {ShadowActivityCompat.class})
public class PersonalImageClassifierTest extends RobolectricTestBase {
  private static final String PREFIX = "https://appinventor.mit.edu/personal-image-classifier/";
  private static final String PERSONAL = PREFIX + "personal/";
  private static final String TRANSFER = PREFIX + "transfer/mobilenet_v3_small/";

  // This model file (tests/assets/pic_test_model.mdl) records a transfer model published at
  // https://models.test/mobilenet/model.json, a host that does not exist, so a request can only be
  // served if it comes from the on-disk cache.
  private static final String MODEL = "pic_test_model.mdl";
  private static final String TRANSFER_BASE_URL = "https://models.test/mobilenet";

  private PersonalImageClassifier classifier;
  private WebViewer webViewer;

  @Before
  public void setUp() {
    super.setUp();
    classifier = new PersonalImageClassifier(getForm());
    webViewer = new WebViewer(getForm());
  }

  // ---------------------------------------------------------------------------------------------
  // Component metadata
  // ---------------------------------------------------------------------------------------------

  @Test
  public void testIsAnExperimentalNonVisibleComponent() {
    DesignerComponent annotation = PersonalImageClassifier.class
        .getAnnotation(DesignerComponent.class);
    assertNotNull(annotation);
    assertEquals(ComponentCategory.EXPERIMENTAL, annotation.category());
    assertTrue(annotation.nonVisible());
  }

  @Test
  public void testOnlyBundlesThePageAndLibraryNotTheModels() {
    UsesAssets assets = PersonalImageClassifier.class.getAnnotation(UsesAssets.class);
    assertNotNull(assets);
    // Transfer models are downloaded on demand, so none of them should be bundled with apps.
    assertEquals("personal_image_classifier.html, personal_image_classifier.js, tfjs-1.5.2.js",
        assets.fileNames());
  }

  // ---------------------------------------------------------------------------------------------
  // Model / WebViewer / Initialize
  // ---------------------------------------------------------------------------------------------

  @Test
  public void testModelMustBeAnMdlFile() {
    classifier.Model("model.txt");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_EXTENSION_ERROR);
  }

  @Test
  public void testInitializeRequiresAWebViewer() {
    classifier.Model(MODEL);
    classifier.Initialize();
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_EXTENSION_ERROR);
  }

  @Test
  public void testInitializeRequiresAModel() {
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.grantLastRequestedPermissions();
    ShadowEventDispatcher.clearEvents();
    classifier.Initialize();
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_EXTENSION_ERROR);
  }

  @Test
  public void testInitializeDoesNotComplainWhileCameraPermissionIsPending() {
    classifier.Model(MODEL);
    classifier.WebViewer(webViewer);  // asks for the camera; the user has not answered yet
    ShadowEventDispatcher.clearEvents();

    classifier.Initialize();

    ShadowEventDispatcher.assertEventNotFired(getForm(), "ErrorOccurred");
  }

  @Test
  public void testGrantingCameraLoadsTheBundledPage() {
    classifier.Model(MODEL);
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.grantLastRequestedPermissions();

    assertEquals("file:///android_asset/personal_image_classifier.html",
        shadowOf(webView()).getLastLoadedUrl());
  }

  @Test
  public void testCompanionLoadsTheBundledPageNotTheProjectAssets() {
    // In the companion, project assets live in a different directory than the files bundled
    // with the app, and the component's page is one of the bundled files.
    setUpAsRepl();
    classifier = new PersonalImageClassifier(getForm());
    webViewer = new WebViewer(getForm());
    classifier.Model(MODEL);
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.grantLastRequestedPermissions();

    assertEquals("file:///android_asset/personal_image_classifier.html",
        shadowOf(webView()).getLastLoadedUrl());
  }

  @Test
  public void testDenyingCameraDoesNotLoadThePageAndReportsTheDenial() {
    classifier.Model(MODEL);
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.denyLastRequestedPermissions();

    ShadowEventDispatcher.assertPermissionDenied(Manifest.permission.CAMERA);
    String loaded = shadowOf(webView()).getLastLoadedUrl();
    assertTrue("Unexpected page loaded: " + loaded, loaded == null || loaded.isEmpty());

    // The prompt has been answered, so a missing WebView is a real problem again.
    ShadowEventDispatcher.clearEvents();
    classifier.Initialize();
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_EXTENSION_ERROR);
  }

  // ---------------------------------------------------------------------------------------------
  // Serving files to the page
  // ---------------------------------------------------------------------------------------------

  @Test
  public void testServesThePersonalModelFromTheModelFile() throws IOException {
    WebViewClient client = clientFor(MODEL);

    WebResourceResponse labels = client.shouldInterceptRequest(webView(),
        PERSONAL + "model_labels.json");
    assertEquals(200, labels.getStatusCode());
    assertEquals("{\"0\": \"cat\", \"1\": \"dog\"}", read(labels.getData()));
    assertEquals("*", labels.getResponseHeaders().get("Access-Control-Allow-Origin"));

    String info = read(client.shouldInterceptRequest(webView(),
        PERSONAL + "transfer_model.json").getData());
    assertTrue(info, info.contains("\"name\": \"mobilenet_v3_small\""));
    assertTrue(info, info.contains("\"normalization\": \"unit\""));
  }

  @Test
  public void testServesTheTransferModelFromTheDiskCache() throws IOException {
    WebViewClient client = clientFor(MODEL);
    writeCachedTransferFile("model.json", "{\"format\": \"graph-model\"}");
    writeCachedTransferFile("group1-shard1of1.bin", "weights");

    WebResourceResponse model = client.shouldInterceptRequest(webView(), TRANSFER + "model.json");
    assertEquals(200, model.getStatusCode());
    assertEquals("application/json", model.getMimeType());
    assertEquals("{\"format\": \"graph-model\"}", read(model.getData()));

    WebResourceResponse shard = client.shouldInterceptRequest(webView(),
        TRANSFER + "group1-shard1of1.bin");
    assertEquals(200, shard.getStatusCode());
    assertEquals("application/octet-stream", shard.getMimeType());
    assertEquals("weights", read(shard.getData()));
  }

  @Test
  public void testIgnoresQueryStringsOnTransferModelRequests() throws IOException {
    // TensorFlow.js adds ?tfjs-format=file when loading models from TF Hub and Kaggle.
    WebViewClient client = clientFor(MODEL);
    writeCachedTransferFile("model.json", "{}");

    WebResourceResponse model = client.shouldInterceptRequest(webView(),
        TRANSFER + "model.json?tfjs-format=file");

    assertEquals(200, model.getStatusCode());
    assertEquals("{}", read(model.getData()));
  }

  @Test
  public void testRefusesTransferModelPathsOutsideTheModel() {
    WebViewClient client = clientFor(MODEL);

    for (String path : new String[] {"../../secret", "a/../../secret", "/etc/passwd"}) {
      WebResourceResponse response = client.shouldInterceptRequest(webView(), TRANSFER + path);
      assertEquals(path, 404, response.getStatusCode());
    }
  }

  @Test
  public void testRequestsWithoutAModelFileDoNotCrash() {
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.grantLastRequestedPermissions();
    WebViewClient client = shadowOf(webView()).getWebViewClient();

    // The page asks for the model files even when no Model has been chosen.
    WebResourceResponse transfer = client.shouldInterceptRequest(webView(),
        TRANSFER + "model.json");
    assertEquals(404, transfer.getStatusCode());
    client.shouldInterceptRequest(webView(), PERSONAL + "model_labels.json");
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  private WebView webView() {
    return (WebView) webViewer.getView();
  }

  /** Points the component at a model file and returns the client that serves its WebView. */
  private WebViewClient clientFor(String model) {
    classifier.Model(model);
    classifier.WebViewer(webViewer);
    ShadowActivityCompat.grantLastRequestedPermissions();
    WebViewClient client = shadowOf(webView()).getWebViewClient();
    assertNotNull(client);
    return client;
  }

  private void writeCachedTransferFile(String path, String contents) throws IOException {
    File root = new File(getForm().getCacheDir(), "transfer-models");
    File file = new File(new File(root, sha1(TRANSFER_BASE_URL)), path);
    assertTrue(file.getParentFile().isDirectory() || file.getParentFile().mkdirs());
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(contents.getBytes(Charset.forName("UTF-8")));
    }
  }

  private static String read(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    } finally {
      in.close();
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
