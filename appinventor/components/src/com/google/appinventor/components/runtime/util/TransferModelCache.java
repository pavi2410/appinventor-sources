// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Downloads TensorFlow.js transfer models (for example MobileNet) on demand and keeps them on
 * disk so that they are shared by every component and project that needs the same base model.
 *
 * <p>Models are addressed by the URL they are published at. Each model lives in its own directory
 * inside the cache, named after a hash of that URL, so two components that ask for the same
 * model will reuse the same files and different versions never collide.
 */
public final class TransferModelCache {
  private static final String LOG_TAG = "TransferModelCache";
  private static final String CACHE_DIR_NAME = "transfer-models";
  private static final String MODEL_JSON = "model.json";
  private static final String TFHUB_QUERY = "?tfjs-format=file";
  private static final int CONNECT_TIMEOUT_MS = 15000;
  private static final int READ_TIMEOUT_MS = 60000;
  private static final Pattern SAFE_PATH = Pattern.compile("[A-Za-z0-9_.\\-]+(/[A-Za-z0-9_.\\-]+)*");

  /** Models known by name, used for model files that do not record where they came from. */
  private static final Map<String, String> KNOWN_MODELS = new HashMap<>();
  static {
    KNOWN_MODELS.put("mobilenet",
        "https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/model.json");
    KNOWN_MODELS.put("mobilenet_v1",
        "https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/model.json");
    KNOWN_MODELS.put("mobilenet_v2",
        "https://storage.googleapis.com/tfjs-models/savedmodel/mobilenet_v2_1.0_224/model.json");
    KNOWN_MODELS.put("mobilenet_v3_small",
        "https://www.kaggle.com/models/google/mobilenet-v3/TfJs/"
            + "small-100-224-feature-vector/1");
  }

  private static final Map<String, Object> LOCKS = new HashMap<>();

  private final File root;

  /** Where one transfer model is published. */
  public static final class Source {
    private final String baseUrl;
    private final boolean fileFormatQuery;

    private Source(String baseUrl, boolean fileFormatQuery) {
      this.baseUrl = baseUrl;
      this.fileFormatQuery = fileFormatQuery;
    }

    public String getBaseUrl() {
      return baseUrl;
    }
  }

  public TransferModelCache(Context context) {
    this.root = new File(context.getCacheDir(), CACHE_DIR_NAME);
  }

  /**
   * Determines where a transfer model can be downloaded from.
   *
   * @param name the model's name as recorded in its transfer_model.json
   * @param url the URL recorded in transfer_model.json, ignored unless it is an https URL
   * @return the model source, or null if neither the URL nor the name identifies a model
   */
  public static Source resolve(String name, String url) {
    String modelUrl = url;
    if (modelUrl == null || !modelUrl.startsWith("https://")) {
      // Models exported without a usable URL (for example from a local development server) are
      // identified by name instead.
      modelUrl = name == null ? null : KNOWN_MODELS.get(name.toLowerCase(Locale.ROOT));
    }
    if (modelUrl == null) {
      return null;
    }
    boolean tfHub = modelUrl.contains("kaggle.com/") || modelUrl.contains("tfhub.dev/");
    String base = modelUrl;
    int query = base.indexOf('?');
    if (query >= 0) {
      base = base.substring(0, query);
    }
    if (base.endsWith("/" + MODEL_JSON)) {
      base = base.substring(0, base.length() - MODEL_JSON.length() - 1);
    }
    while (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return new Source(base, tfHub);
  }

  /**
   * Returns the cached copy of a file belonging to a transfer model, downloading it first if it
   * is not on disk yet.
   *
   * @param source where the model is published
   * @param path path of the file relative to the model, for example "model.json"
   * @return the file on disk
   * @throws IOException if the path is not valid or the file cannot be downloaded
   */
  public File get(Source source, String path) throws IOException {
    if (!SAFE_PATH.matcher(path).matches() || path.contains("..")) {
      throw new IOException("Invalid transfer model path: " + path);
    }
    File dir = new File(root, hash(source.baseUrl));
    File target = new File(dir, path);
    if (target.isFile() && target.length() > 0) {
      return target;
    }
    synchronized (lockFor(target.getPath())) {
      if (target.isFile() && target.length() > 0) {
        return target;
      }
      download(source.baseUrl + "/" + path + (source.fileFormatQuery ? TFHUB_QUERY : ""), target);
    }
    return target;
  }

  private static Object lockFor(String key) {
    synchronized (LOCKS) {
      Object lock = LOCKS.get(key);
      if (lock == null) {
        lock = new Object();
        LOCKS.put(key, lock);
      }
      return lock;
    }
  }

  private static void download(String url, File target) throws IOException {
    File parent = target.getParentFile();
    if (!parent.isDirectory() && !parent.mkdirs()) {
      throw new IOException("Unable to create " + parent);
    }
    File partial = new File(parent, target.getName() + ".part");
    Log.d(LOG_TAG, "Downloading " + url);
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
    try {
      int status = connection.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new IOException("Unexpected response " + status + " for " + url);
      }
      try (InputStream in = connection.getInputStream();
           FileOutputStream out = new FileOutputStream(partial)) {
        byte[] buffer = new byte[16 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
          out.write(buffer, 0, read);
        }
      }
      if (!partial.renameTo(target)) {
        throw new IOException("Unable to store " + target);
      }
    } finally {
      connection.disconnect();
      if (partial.exists() && !partial.delete()) {
        Log.w(LOG_TAG, "Unable to remove " + partial);
      }
    }
  }

  private static String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] bytes = digest.digest(value.getBytes(Charset.forName("UTF-8")));
      StringBuilder hex = new StringBuilder();
      for (byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
