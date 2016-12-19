package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.LinkedList;
import java.util.List;

public class  ResourceMerger {
  @NotNull
  public static ResourceTable buildResourceTable(AndroidManifest appManifest) {
    List<ResourcePath> allResourcePaths = new LinkedList<>();
    addTransitiveResourcePaths(appManifest, allResourcePaths);
    return buildResourceTable(appManifest.getPackageName(), appManifest.getResourcePath(), allResourcePaths);
  }

  private static void addTransitiveResourcePaths(AndroidManifest manifest, List<ResourcePath> resourcePaths) {
    resourcePaths.add(manifest.getResourcePath());
    for (AndroidManifest libraryManifest : manifest.getLibraryManifests()) {
      addTransitiveResourcePaths(libraryManifest, resourcePaths);
    }
  }

  @NotNull
  private static ResourceTable buildResourceTable(String packageName, ResourcePath appResourcePath, List<ResourcePath> allResourcePaths) {
    PackageResourceIndex resourceIndex = new PackageResourceIndex(packageName);
    ResourceTable resourceTable = new ResourceTable(resourceIndex);
    ResourceExtractor.populate(appResourcePath, resourceIndex);

    for (ResourcePath resourcePath : allResourcePaths) {
      ResourceParser.load(packageName, resourcePath, resourceTable);
    }
    ResourceParser.load(packageName, appResourcePath, resourceTable);
    return resourceTable;
  }
}
