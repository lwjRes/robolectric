package org.robolectric.res;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PackageResourceIndex implements ResourceIndex {
  private static final Logger LOGGER = Logger.getLogger(ResourceExtractor.class.getName());

  private final Map<ResName, Integer> resourceNameToId = new HashMap<>();
  private final Map<Integer, ResName> resourceIdToResName = new HashMap<>();

  private Integer maxUsedInt = null;
  private Integer generatedIdStart = null;
  private String packageName;
  private int packageIdentifier;

  public PackageResourceIndex(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public synchronized Integer getResourceId(ResName resName) {
    Integer id = resourceNameToId.get(resName);
    if (id == null && ("android".equals(resName.packageName) || "".equals(resName.packageName))) {
      if (maxUsedInt == null) {
        maxUsedInt = resourceIdToResName.isEmpty() ? 0 : Collections.max(resourceIdToResName.keySet());
        generatedIdStart = maxUsedInt;
      }
      id = ++maxUsedInt;
      resourceNameToId.put(resName, id);
      resourceIdToResName.put(id, resName);
      LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    if (id == null) return 0;

    return id;
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceIdToResName.get(resourceId);
  }

  public String getPackageName() {
    return packageName;
  }

  int getPackageIdentifier() {
    return packageIdentifier;
  }

  void addResource(int id, String type, String name) {
    ResName resName = new ResName(packageName, type, name);
    int resIdPackageIdentifier = ResourceIds.getPackageIdentifier(id);
    if (getPackageIdentifier() == 0) {
      this.packageIdentifier = resIdPackageIdentifier;
    } else if (getPackageIdentifier() != resIdPackageIdentifier) {
      throw new IllegalArgumentException("Attempted to add resId " + resIdPackageIdentifier + " to PackageResourceIndex with packageIdentifier " + getPackageIdentifier());
    }

    resourceNameToId.put(resName, id);
    resourceIdToResName.put(id, resName);
  }
}
