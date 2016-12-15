package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.LinkedList;
import java.util.List;

public class ResourceMerger {
  @NotNull
  public static ResourceTable buildResourceTable(AndroidManifest appManifest) {
    List<ResourcePath> allResourcePaths = new LinkedList<>();
    addTransitiveResourcePaths(appManifest.getLibraryManifests(), allResourcePaths);
    allResourcePaths.add(appManifest.getResourcePath());
    return buildResourceTable(appManifest.getPackageName(), appManifest.getResourcePath(), allResourcePaths);
  }

  @NotNull
  private static ResourceTable buildResourceTable(String packageName, ResourcePath appResourcePath, List<ResourcePath> allResourcePaths) {
    PackageResourceIndex resourceIndex = new PackageResourceIndex(packageName);
    ResourceTable resourceTable = new ResourceTable(resourceIndex);
    ResourceExtractor.populate(appResourcePath, resourceIndex);

    for (ResourcePath resourcePath : allResourcePaths) {
      ResourceParser.load(resourcePath, resourceTable);
    }
    ResourceParser.load(appResourcePath, resourceTable);
    return resourceTable;
  }

  private static void addTransitiveResourcePaths(List<AndroidManifest> manifests, List<ResourcePath> resourcePaths) {
    for (AndroidManifest manifest : manifests) {
      if (manifest.getLibraryManifests().size() != 0) {
        addTransitiveResourcePaths(manifest.getLibraryManifests(), resourcePaths);
      }
      resourcePaths.add(manifest.getResourcePath());
    }
  }
}
