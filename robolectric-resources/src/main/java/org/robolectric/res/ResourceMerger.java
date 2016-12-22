package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.LinkedList;
import java.util.List;

public class  ResourceMerger {
  @NotNull
  public static ResourceTable buildResourceTable(AndroidManifest appManifest) {
    List<ResourcePath> allLibraryResourcePaths = new LinkedList<>();
    addTransitiveResourcePaths(appManifest.getLibraryManifests(), allLibraryResourcePaths);
    return buildResourceTable(appManifest.getPackageName(), appManifest.getResourcePath(), allLibraryResourcePaths);
  }

  private static void addTransitiveResourcePaths(List<AndroidManifest> androidManifests, List<ResourcePath> resourcePaths) {
    for (AndroidManifest androidManifest : androidManifests) {
      resourcePaths.add(androidManifest.getResourcePath());
      addTransitiveResourcePaths(androidManifest.getLibraryManifests(), resourcePaths);
    }
  }

  @NotNull
  private static ResourceTable buildResourceTable(String packageName, ResourcePath appResourcePath, List<ResourcePath> allResourcePaths) {
    PackageResourceIndex resourceIndex = new PackageResourceIndex(packageName);
    ResourceTable resourceTable = new ResourceTable(resourceIndex);

    ResourceRemapper resourceRemapper = new ResourceRemapper();
    resourceRemapper.remapRClass(true, appResourcePath.getRClass());
    ResourceExtractor.populate(resourceIndex, appResourcePath.getRClass());
    for (ResourcePath resourcePath : allResourcePaths) {
      if (resourcePath.getRClass() != null) {
        resourceRemapper.remapRClass(false, resourcePath.getRClass());
        ResourceExtractor.populate(resourceIndex, appResourcePath.getRClass());
      }
    }

    ResourceParser.load(packageName, appResourcePath, resourceTable);
    for (ResourcePath resourcePath : allResourcePaths) {
      ResourceParser.load(packageName, resourcePath, resourceTable);
    }
    return resourceTable;
  }
}
