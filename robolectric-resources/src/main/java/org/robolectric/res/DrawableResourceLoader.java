package org.robolectric.res;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader {
  private String packageName;
  private final ResBunch resBunch;

  public static boolean isStillHandledHere(String type) {
    return "drawable".equals(type) || "anim".equals(type) || "mipmap".equals(type);
  }

  DrawableResourceLoader(String packageName, ResBunch resBunch) {
    this.packageName = packageName;
    this.resBunch = resBunch;
  }

  /**
   * Returns a collection of resource IDs for all nine-patch drawables in the project.
   *
   * @param resourcePath Resource path.
   */
  void findDrawableResources(ResourcePath resourcePath) {
    FsFile[] files = resourcePath.getResourceBase().listFiles();
    if (files != null) {
      for (FsFile f : files) {
        if (f.isDirectory() && f.getName().startsWith("drawable")) {
          listDrawableResources(f, "drawable");
        } else if (f.isDirectory() && f.getName().startsWith("mipmap")) {
          listDrawableResources(f, "mipmap");
        }
      }
    }
  }

  private void listDrawableResources(FsFile dir, String type) {
    FsFile[] files = dir.listFiles();
    if (files != null) {
      for (FsFile f : files) {
        String name = f.getName();
        if (name.startsWith(".")) continue;

        String shortName;
        boolean isNinePatch;
        if (name.endsWith(".xml")) {
          // already handled, do nothing...
          continue;
        } else if (name.endsWith(".9.png")) {
          String[] tokens = name.split("\\.9\\.png$");
          shortName = tokens[0];
          isNinePatch = true;
        } else {
          shortName = f.getBaseName();
          isNinePatch = false;
        }

        XmlLoader.XmlContext fakeXmlContext = new XmlLoader.XmlContext(packageName, f);
        resBunch.put(type, shortName, new FileTypedResource.Image(f, isNinePatch, fakeXmlContext));
      }
    }
  }
}
