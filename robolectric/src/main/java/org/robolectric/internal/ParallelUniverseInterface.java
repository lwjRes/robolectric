package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResourceLoader;

public interface ParallelUniverseInterface {
  void resetStaticState(Config config);

  void setUpApplicationState(Method method, TestLifecycle testLifecycle, ResourceLoader sdkResourceLoader, ResourceLoader systemResourceLoader, ResourceLoader compiletimeSdkResourceLoader, AndroidManifest appManifest, Config config);

  Thread getMainThread();

  void setMainThread(Thread newMainThread);

  void tearDownApplication();

  Object getCurrentApplication();

  void setSdkConfig(SdkConfig sdkConfig);

}
