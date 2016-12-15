package org.robolectric.res;

import java.util.HashMap;
import java.util.Map;

public class RoutingResourceIndex extends ResourceIndex {

  private Map<String, ResourceIndex> resourceIndexes = new HashMap<>();

  public RoutingResourceIndex(ResourceIndex... subIndexes) {
    super("");

    for (ResourceIndex subIndex : subIndexes) {
      resourceIndexes.put(subIndex.getPackageName(), subIndex);
    }
  }

  @Override
  public Integer getResourceId(ResName resName) {
    ResourceIndex resourceIndex = resourceIndexes.get(resName.packageName);
    if (resourceIndex == null) {
      // This occurs at present because the XML contains "xmlns:android" elements, we should probably ignore these in the XmlParserImpl
      System.out.println("Resource not found, unknown package: " + resName.packageName);
      return -1;
    }
    return resourceIndex.getResourceId(resName);
  }

  @Override
  public ResName getResName(int resourceId) {
    String packageName = ResourceIds.isFrameworkResource(resourceId) ? "android" : "org.robolectric";
    return resourceIndexes.get(packageName).getResName(resourceId);
  }
}