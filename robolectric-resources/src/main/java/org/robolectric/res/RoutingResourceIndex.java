package org.robolectric.res;

import java.util.HashMap;
import java.util.Map;

public class RoutingResourceIndex extends PackageResourceIndex {

  private Map<String, ResourceIndex> packageNameToIndex = new HashMap<>();
  private Map<Integer, ResourceIndex> packageIndentifierToIndex = new HashMap<>();

  public RoutingResourceIndex(PackageResourceIndex... subIndexes) {
    super("");

    for (PackageResourceIndex subIndex : subIndexes) {
      packageNameToIndex.put(subIndex.getPackageName(), subIndex);
      packageIndentifierToIndex.put(subIndex.getPackageIdentifier(), subIndex);
    }
  }

  @Override
  public Integer getResourceId(ResName resName) {
    ResourceIndex resourceIndex = packageNameToIndex.get(resName.packageName);
    if (resourceIndex == null) {
      // This occurs at present because the XML contains "xmlns:android" elements, we should probably ignore these in the XmlParserImpl
      System.out.println("Resource not found, unknown package: " + resName.packageName);
      return 0;
    }
    return resourceIndex.getResourceId(resName);
  }

  @Override
  public ResName getResName(int resourceId) {
    int packageIdentifier = ResourceIds.getPackageIdentifier(resourceId);
    ResourceIndex resourceIndex = packageIndentifierToIndex.get(packageIdentifier);
    if (resourceIndex == null) {
      return null;
    }

    return resourceIndex.getResName(resourceId);
  }
}