package com.squareup.spoon;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.squareup.spoon.Chmod.chmodPlusR;
import static com.squareup.spoon.Chmod.chmodPlusRWX;

/** Utility class for capturing screenshots for Spoon. */
public final class Spoon {
  static final String SPOON_SCREENSHOTS = "spoon-screenshots";
  static final String NAME_SEPARATOR = "_";
  static final String TEST_CASE_CLASS = "android.test.InstrumentationTestCase";
  static final String TEST_CASE_METHOD = "runMethod";
  private static final String EXTENSION = ".png";
  private static final String TAG = "Spoon";
  private static final Object LOCK = new Object();
  private static final Pattern TAG_VALIDATION = Pattern.compile("[a-zA-Z0-9_-]+");

  /** Whether or not the screenshot output directory needs cleared. */
  private static boolean outputNeedsClear = true;

  /**
   * Take a screenshot with the specified tag.
   *
   * @param activity Activity with which to capture a screenshot.
   * @param tag Unique tag to further identify the screenshot. Must match [a-zA-Z0-9_-]+.
   * @return the image file that was created
   */
  public static File screenshot(Activity activity, Instrumentation instrumentation, String tag) {
    StackTraceElement testClass = findTestClassTraceElement(Thread.currentThread().getStackTrace());
    return getScreenshot(activity, instrumentation, tag, testClass);
  }

  /** Overload method with extra testClass parameters to get screenshot on failure */
  public static File screenshot(Activity activity, Instrumentation instrumentation,
      String tag, StackTraceElement testClass) {
    return getScreenshot(activity, instrumentation, tag, testClass);
  }

  private static File getScreenshot(Activity activity, Instrumentation instrumentation,
      String tag, StackTraceElement testClass) {
    if (!TAG_VALIDATION.matcher(tag).matches()) {
      throw new IllegalArgumentException("Tag must match " + TAG_VALIDATION.pattern() + ".");
    }
    try {
      File screenshotDirectory = obtainScreenshotDirectory(activity, testClass);
      String screenshotName = System.currentTimeMillis() + NAME_SEPARATOR + tag + EXTENSION;
      File screenshotFile = new File(screenshotDirectory, screenshotName);
      if (Build.VERSION.SDK_INT < 18) {
        takeScreenshot(screenshotFile, activity);
      } else {
        SpoonCompatJellyBeanMR2.takeScreenshot(instrumentation, screenshotFile);
      }
      Log.d(TAG, "Captured screenshot '" + tag + "'.");
      return screenshotFile;
    } catch (Exception e) {
      throw new RuntimeException("Unable to capture screenshot.", e);
    }
  }

  private static void takeScreenshot(File file, final Activity activity) throws IOException {
    DisplayMetrics dm = activity.getResources().getDisplayMetrics();
    final Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888);

    if (Looper.myLooper() == Looper.getMainLooper()) {
      // On main thread already, Just Do It™.
      drawDecorViewToBitmap(activity, bitmap);
    } else {
      // On a background thread, post to main.
      final CountDownLatch latch = new CountDownLatch(1);
      activity.runOnUiThread(new Runnable() {
        @Override public void run() {
          try {
            drawDecorViewToBitmap(activity, bitmap);
          } finally {
            latch.countDown();
          }
        }
      });
      try {
        latch.await();
      } catch (InterruptedException e) {
        String msg = "Unable to get screenshot " + file.getAbsolutePath();
        Log.e(TAG, msg, e);
        throw new RuntimeException(msg, e);
      }
    }

    OutputStream fos = null;
    try {
      fos = new BufferedOutputStream(new FileOutputStream(file));
      bitmap.compress(PNG, 100 /* quality */, fos);

      chmodPlusR(file);
    } finally {
      bitmap.recycle();
      if (fos != null) {
        fos.close();
      }
    }
  }

  private static void drawDecorViewToBitmap(Activity activity, Bitmap bitmap) {
    Canvas canvas = new Canvas(bitmap);
    activity.getWindow().getDecorView().draw(canvas);
  }

  private static File obtainScreenshotDirectory(Context context,
      StackTraceElement testClass) throws IllegalAccessException {
    File screenshotsDir = new File(Environment.getExternalStorageDirectory(),
        SPOON_SCREENSHOTS + "/" + context.getApplicationInfo().packageName);

    synchronized (LOCK) {
      if (outputNeedsClear) {
        deletePath(screenshotsDir, false);
        outputNeedsClear = false;
      }
    }
    String className = testClass.getClassName().replaceAll("[^A-Za-z0-9._-]", "_");
    File dirClass = new File(screenshotsDir, className);
    File dirMethod = new File(dirClass, testClass.getMethodName());
    createDir(dirMethod);
    return dirMethod;
  }

  /** Returns the test class element by looking at the method InstrumentationTestCase invokes. */
  static StackTraceElement findTestClassTraceElement(StackTraceElement[] trace) {
    for (int i = trace.length - 1; i >= 0; i--) {
      StackTraceElement element = trace[i];
      if (TEST_CASE_CLASS.equals(element.getClassName()) //
          && TEST_CASE_METHOD.equals(element.getMethodName())) {
            // the way we now call this method it ends up with #screenshot as the folder name
            // when using [i-3]
            if (trace[i - 3].getMethodName().equals("screenshot")) {
              return trace[i - 2];
            } else {
              return trace[i - 3];
            }
      }
    }

    throw new IllegalArgumentException("Could not find test class!");
  }

  private static void createDir(File dir) throws IllegalAccessException {
    File parent = dir.getParentFile();
    if (!parent.exists()) {
      createDir(parent);
    }
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IllegalAccessException("Unable to create output dir: " + dir.getAbsolutePath());
    }
    chmodPlusRWX(dir);
  }

  private static void deletePath(File path, boolean inclusive) {
    if (path.isDirectory()) {
      File[] children = path.listFiles();
      if (children != null) {
        for (File child : children) {
          deletePath(child, true);
        }
      }
    }
    if (inclusive) {
      path.delete();
    }
  }

  private Spoon() {
    // No instances.
  }
}
