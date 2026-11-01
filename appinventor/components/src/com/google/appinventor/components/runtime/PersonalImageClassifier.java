// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.TransferModelCache;
import com.google.appinventor.components.runtime.util.YailDictionary;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Component that classifies images using a user trained model from the image classification explorer.
 * Based heavily on the Look Extension by kevinzhu@mit.edu (Kevin Zhu) and kelseyc@mit.edu (Kelsey Chan)
 *
 * @author data1013@mit.edu (Danny Tang)
 */

@DesignerComponent(version = YaVersion.PERSONALIMAGECLASSIFIER_COMPONENT_VERSION,
        category = ComponentCategory.EXPERIMENTAL,
        description = "Component that classifies images using a user trained model from the image " +
            "classification explorer. You must provide a WebViewer component in the PersonalImageClassifier " +
            "component's WebViewer property in order for classification to work.",
        iconName = "images/glasses.png",
        nonVisible = true)
@SimpleObject
@UsesAssets(fileNames = "personal_image_classifier.html, personal_image_classifier.js, tfjs-1.5.2.js")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.CAMERA")
public final class PersonalImageClassifier extends AndroidNonvisibleComponent
    implements Component, OnPauseListener, OnResumeListener, OnClearListener {

  private static final String LOG_TAG = PersonalImageClassifier.class.getSimpleName();
  private static final int IMAGE_WIDTH = 500;
  private static final int IMAGE_QUALITY = 100;
  private static final String MODE_VIDEO = "Video";
  private static final String MODE_IMAGE = "Image";
  private static final String ERROR_WEBVIEWER_NOT_SET =
      "You must specify a WebViewer using the WebViewer designer property before you can call %1s";
  private static final String MODEL_PATH_SUFFIX = ".mdl";

  private static final String TRANSFER_MODEL_PREFIX = "https://appinventor.mit.edu/personal-image-classifier/transfer/";
  private static final String PERSONAL_MODEL_PREFIX = "https://appinventor.mit.edu/personal-image-classifier/personal/";

  // other error codes are defined in personal_image_classifier.js
  private static final int ERROR_CLASSIFICATION_NOT_SUPPORTED = -1;
  private static final int ERROR_CLASSIFICATION_FAILED = -2;
  private static final int ERROR_CANNOT_TOGGLE_CAMERA_IN_IMAGE_MODE = -3;
  private static final int ERROR_CANNOT_CLASSIFY_IMAGE_IN_VIDEO_MODE = -4;
  private static final int ERROR_CANNOT_CLASSIFY_VIDEO_IN_IMAGE_MODE = -5;
  private static final int ERROR_INVALID_INPUT_MODE = -6;
  private static final int ERROR_WEBVIEWER_REQUIRED = -7;
  private static final int ERROR_INVALID_MODEL_FILE = -8;
  private static final int ERROR_MODEL_REQUIRED = -9;
  // -10 and -11 are defined in personal_image_classifier.js
  private static final int ERROR_TRANSFER_MODEL_UNAVAILABLE = -12;
  private static final int ERROR_TRANSFER_MODEL_UNSUPPORTED = -13;

  private WebView webview = null;
  private String inputMode = MODE_VIDEO;
  private List<String> labels = Collections.emptyList();
  private String modelPath = null;
  private TransferModelCache.Source transferModelSource = null;
  private final TransferModelCache transferModelCache;
  private boolean running = false;
  private int minClassTime = 0;

  public PersonalImageClassifier(final Form form) {
    super(form);
    transferModelCache = new TransferModelCache(form);
    requestHardwareAcceleration(form);
    WebView.setWebContentsDebuggingEnabled(true);
    Log.d(LOG_TAG, "Created PersonalImageClassifier component");
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void configureWebView(WebView webview) {
    this.webview = webview;
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
    // adds a way to send strings to the javascript
    webview.addJavascriptInterface(new JsObject(), "PersonalImageClassifier");
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Log.d(LOG_TAG, "shouldInterceptRequest called");
        InputStream file = null;
        String charSet;
        String contentType;

        if (url.endsWith(".json")) {
          contentType = "application/json";
          charSet = "UTF-8";
        } else {
          contentType = "application/octet-stream";
          charSet = "binary";
        }

        try {
          if (url.contains(TRANSFER_MODEL_PREFIX)) {
            Log.d(LOG_TAG, "overriding " + url);

            file = openTransferModelFile(url.substring(url.indexOf(TRANSFER_MODEL_PREFIX)
                + TRANSFER_MODEL_PREFIX.length()));
            if (file == null) {
              return new WebResourceResponse(contentType, charSet, 404, "Not Found",
                  new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
            }
          } else if (url.contains(PERSONAL_MODEL_PREFIX)) {
            Log.d(LOG_TAG, "overriding " + url);

            byte[] fileBytes = readModelEntry(url.substring(PERSONAL_MODEL_PREFIX.length()));
            if (fileBytes != null) {
              file = new ByteArrayInputStream(fileBytes);
            }
          }

          if (file != null) {
            if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
              Map<String, String> responseHeaders = new HashMap<>();
              responseHeaders.put("Access-Control-Allow-Origin", "*");
              return new WebResourceResponse(contentType, charSet, 200, "OK", responseHeaders, file);
            } else {
              return new WebResourceResponse(contentType, charSet, file);
            }
          }  
        } catch (IOException e) {
          e.printStackTrace();
          return super.shouldInterceptRequest(view, url);
        }

        Log.d(LOG_TAG, url);
        return super.shouldInterceptRequest(view, url);
      }
    });
    webview.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onPermissionRequest(PermissionRequest request) {
        Log.d(LOG_TAG, "onPermissionRequest called");
        String[] requestedResources = request.getResources();
        for (String r : requestedResources) {
          if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
          }
        }
      }
    });
  }

  public void Initialize() {
    Log.d(LOG_TAG, "webview = " + webview);
    if (webview == null) {
      form.dispatchErrorOccurredEvent(this, "WebViewer",
          ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_WEBVIEWER_REQUIRED, LOG_TAG,
          "You must specify a WebViewer component in the WebViewer property.");
    }

    Log.d(LOG_TAG, "modelPath = " + modelPath);
    if (modelPath == null) {
      form.dispatchErrorOccurredEvent(this, "Model",
          ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_MODEL_REQUIRED, LOG_TAG,
          "You must provide a model file in the Model property");
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":com.google.appinventor.components.runtime.WebViewer")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public void WebViewer(final WebViewer webviewer) {
    Runnable next = new Runnable() {
      public void run() {
        if (webviewer != null) {
          configureWebView((WebView) webviewer.getView());
          webview.requestLayout();
          Log.d(LOG_TAG, "isHardwareAccelerated? " + webview.isHardwareAccelerated());
          webview.loadUrl(form.getAssetPath("personal_image_classifier.html"));
        }
      }
    };
    form.askPermission(Manifest.permission.CAMERA, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          next.run();
        } else {
          form.PermissionDenied(PersonalImageClassifier.this, "WebViewer", permission);
        }
      }
    });
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
  public void Model(String path) {
    Log.d(LOG_TAG, "Personal model path: " + path);

    if (path.endsWith(MODEL_PATH_SUFFIX)) {
      modelPath = path;
      transferModelSource = null;
    } else {
      form.dispatchErrorOccurredEvent(this, "Model",
        ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INVALID_MODEL_FILE, LOG_TAG,
        "Invalid model file format: files must be of format " + MODEL_PATH_SUFFIX);
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES,
      editorArgs = {MODE_VIDEO, MODE_IMAGE})
  @SimpleProperty
  public void InputMode(String mode) {
    if (webview == null) {
      inputMode = mode;
      return;
    }
    if (MODE_VIDEO.equalsIgnoreCase(mode)) {
      webview.evaluateJavascript("setInputMode(\"video\");", null);
      inputMode = MODE_VIDEO;
    } else if (MODE_IMAGE.equalsIgnoreCase(mode)) {
      webview.evaluateJavascript("setInputMode(\"image\");", null);
      inputMode = MODE_IMAGE;
    } else {
      form.dispatchErrorOccurredEvent(this, "InputMode", ErrorMessages.ERROR_EXTENSION_ERROR, ERROR_INVALID_INPUT_MODE, LOG_TAG, "Invalid input mode " + mode);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets or sets the input mode for classification. Valid values are \"Video\" " +
          "(the default) and \"Image\".")
  public String InputMode() {
    return inputMode;
  }

  @SimpleProperty(description = "Gets all of the labels from this model. Only valid after ClassifierReady is signaled.")
  public List<String> ModelLabels() {
    return labels;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Running() {
    return running;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void MinimumInterval(int interval) {
    minClassTime = interval;
    if (webview != null) {
      webview.evaluateJavascript("minClassTime = " + interval + ";", null);
    }
  }

  @SimpleProperty
  public int MinimumInterval() {
    return minClassTime;
  }

  @SimpleFunction(description = "Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.")
  public void ClassifyImageData(final String image) {
    assertWebView("ClassifyImageData");
    Log.d(LOG_TAG, "Entered Classify");
    Log.d(LOG_TAG, image);

    String imagePath = (image == null) ? "" : image;
    BitmapDrawable imageDrawable;
    Bitmap scaledImageBitmap = null;

    try {
      imageDrawable = MediaUtil.getBitmapDrawable(form.$form(), imagePath);
      scaledImageBitmap = Bitmap.createScaledBitmap(imageDrawable.getBitmap(), IMAGE_WIDTH, (int) (imageDrawable.getBitmap().getHeight() * ((float) IMAGE_WIDTH) / imageDrawable.getBitmap().getWidth()), false);
    } catch (IOException ioe) {
      Log.e(LOG_TAG, "Unable to load " + imagePath);
    }

    // compression format of PNG -> not lossy
    Bitmap immagex = scaledImageBitmap;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    immagex.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, baos);
    byte[] b = baos.toByteArray();

    String imageEncodedbase64String = Base64.encodeToString(b, 0).replace("\n", "");
    Log.d(LOG_TAG, "imageEncodedbase64String: " + imageEncodedbase64String);

    webview.evaluateJavascript("classifyImageData(\"" + imageEncodedbase64String + "\");", null);
  }

  @SimpleFunction(description = "Toggles between user-facing and environment-facing camera.")
  public void ToggleCameraFacingMode() {
    assertWebView("ToggleCameraFacingMode");
    webview.evaluateJavascript("toggleCameraFacingMode();", null);
  }

  @SimpleFunction(description = "Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.")
  public void ClassifyVideoData() {
    assertWebView("ClassifyVideoData");
    webview.evaluateJavascript("classifyVideoData();", null);
  }

  @SimpleFunction(description = "Starts continuously classifying frames from the video feed. "
      + "Only valid when InputMode is Video.")
  public void StartContinuousClassification() {
    if (MODE_VIDEO.equals(inputMode) && !running) {
      assertWebView("StartVideoClassification");
      webview.evaluateJavascript("startVideoClassification();", null);
      running = true;
    }
  }

  @SimpleFunction(description = "Stops the continuous classification started by "
      + "StartContinuousClassification.")
  public void StopContinuousClassification() {
    if (MODE_VIDEO.equals(inputMode) && running) {
      assertWebView("StopVideoClassification");
      webview.evaluateJavascript("stopVideoClassification();", null);
      running = false;
    }
  }

  @SimpleEvent(description = "Event indicating that the classifier is ready.")
  public void ClassifierReady() {
    InputMode(inputMode);
    MinimumInterval(minClassTime);
    EventDispatcher.dispatchEvent(this, "ClassifierReady");
  }

  @SimpleEvent(description = "Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].")
  public void GotClassification(YailDictionary result) {
    EventDispatcher.dispatchEvent(this, "GotClassification", result);
  }

  @SimpleEvent(description = "Event indicating that an error has occurred.")
  public void Error(final int errorCode) {
    EventDispatcher.dispatchEvent(this, "Error", errorCode);
  }

  ///REGION: Lifecycle handling

  @Override
  public void onPause() {
    if (MODE_VIDEO.equals(inputMode)) {
      webview.evaluateJavascript("stopVideo();", null);
    }
  }

  @Override
  public void onResume() {
    if (MODE_VIDEO.equals(inputMode)) {
      webview.evaluateJavascript("startVideo();", null);
    }
  }

  @Override
  public void onClear() {
    webview.evaluateJavascript("stopVideo();", null);
  }

  ///ENDREGION

  private static void requestHardwareAcceleration(Activity activity) {
    activity.getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED, LayoutParams.FLAG_HARDWARE_ACCELERATED);
  }

  private void assertWebView(String method) {
    if (webview == null) {
      throw new RuntimeException(String.format(ERROR_WEBVIEWER_NOT_SET, method));
    }
  }

  /**
   * Reads one entry of the user's model file (a zip archive).
   *
   * @return the entry's contents, or null if the model has no such entry
   */
  private byte[] readModelEntry(String name) throws IOException {
    ZipInputStream zipInputStream = new ZipInputStream(MediaUtil.openMedia(form, modelPath));
    try {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if (zipEntry.getName().equals(name)) {
          ByteArrayOutputStream contents = new ByteArrayOutputStream();
          byte[] buffer = new byte[8192];
          int read;
          while ((read = zipInputStream.read(buffer)) != -1) {
            contents.write(buffer, 0, read);
          }
          return contents.toByteArray();
        }
      }
    } finally {
      zipInputStream.close();
    }
    return null;
  }

  /**
   * Finds out where the transfer model (for example MobileNet) used by the user's model comes
   * from by reading the transfer_model.json recorded in the model file.
   */
  private TransferModelCache.Source getTransferModelSource() throws IOException {
    if (transferModelSource == null) {
      byte[] info = readModelEntry("transfer_model.json");
      if (info == null) {
        return null;
      }
      try {
        JSONObject json = new JSONObject(new String(info, "UTF-8"));
        transferModelSource =
            TransferModelCache.resolve(json.optString("name"), json.optString("url"));
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Unable to parse transfer_model.json", e);
      }
    }
    return transferModelSource;
  }

  /**
   * Opens a file of the transfer model, downloading it if it has not been cached yet.
   *
   * @param path the requested path, of the form {@code <model name>/<file>[?query]}
   * @return a stream over the file, or null if it is unavailable
   */
  private InputStream openTransferModelFile(String path) {
    int query = path.indexOf('?');
    if (query >= 0) {
      path = path.substring(0, query);
    }
    int slash = path.indexOf('/');
    if (slash < 0) {
      return null;
    }
    try {
      TransferModelCache.Source source = getTransferModelSource();
      if (source == null) {
        return null;
      }
      return new FileInputStream(transferModelCache.get(source, path.substring(slash + 1)));
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to load transfer model file " + path, e);
      return null;
    }
  }

  private static List<String> parseLabels(String labels) {
    List<String> result = new ArrayList<>();
    try {
      JSONArray arr = new JSONArray(labels);
      for (int i = 0; i < arr.length(); i++) {
        result.add(arr.getString(i));
      }
    } catch (JSONException e) {
      throw new YailRuntimeError("Got unparsable array from Javascript", "RuntimeError");
    }
    return result;
  }

  private class JsObject {
    @JavascriptInterface
    public void ready(String labels) {
      Log.d(LOG_TAG, "Entered ready");
      PersonalImageClassifier.this.labels = parseLabels(labels);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ClassifierReady();
        }
      });
    }

    @JavascriptInterface
    public void reportResult(final String result) {
      Log.d(LOG_TAG, "Entered reportResult: " + result);
      try {
        Log.d(LOG_TAG, "Entered try of reportResult");
        JSONArray list = new JSONArray(result);
        final YailDictionary resultDict = new YailDictionary();
        for (int i = 0; i < list.length(); i++) {
          JSONArray pair = list.getJSONArray(i);
          resultDict.put(pair.getString(0), pair.getDouble(1));
        }
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotClassification(resultDict);
          }
        });
      } catch (JSONException e) {
        Log.d(LOG_TAG, "Entered catch of reportResult");
        e.printStackTrace();
        Error(ERROR_CLASSIFICATION_FAILED);
      }
    }

    @JavascriptInterface
    public void error(final int errorCode) {
      Log.d(LOG_TAG, "Entered error: " + errorCode);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Error(errorCode);
        }
      });
    }
  }
}
