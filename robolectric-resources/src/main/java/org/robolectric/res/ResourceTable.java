package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

public class ResourceTable {
  final ResBunch data = new ResBunch();
  final ResBundle xmlDocuments = new ResBundle();
  final ResBundle rawResources = new ResBundle();
  private ResourceIndex resourceIndex;
  private Object packageName;

  public ResourceTable(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  public String getPackageName() {
    return resourceIndex.getPackageName();
  }

  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return data.get(resName, qualifiers);
  }

  public XmlBlock getXml(ResName resName, String qualifiers) {
    TypedResource typedResource = xmlDocuments.get(resName, qualifiers);
    return typedResource == null ? null : (XmlBlock) typedResource.getData();
  }

  public InputStream getRawValue(ResName resName, String qualifiers) {
    TypedResource typedResource = rawResources.get(resName, qualifiers);
    FsFile file = typedResource == null ? null : (FsFile) typedResource.getData();
    try {
      return file == null ? null : file.getInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
