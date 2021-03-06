package org.robolectric.res;

import org.robolectric.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class StyleResolver implements Style {
  private final List<StyleData> styles = new ArrayList<>();
  private final ResourceLoader appResourceLoader;
  private final ResourceLoader systemResourceLoader;
  private final Style theme;
  private final ResName myResName;
  private final String qualifiers;

  public StyleResolver(ResourceLoader appResourceLoader, ResourceLoader systemResourceLoader, StyleData styleData,
                       Style theme, ResName myResName, String qualifiers) {
    this.appResourceLoader = appResourceLoader;
    this.systemResourceLoader = systemResourceLoader;
    this.theme = theme;
    this.myResName = myResName;
    this.qualifiers = qualifiers;
    styles.add(styleData);
  }

  @Override public AttributeResource getAttrValue(ResName resName) {
    for (StyleData style : styles) {
      AttributeResource value = style.getAttrValue(resName);
      if (value != null) return value;
    }
    int initialSize = styles.size();
    while (hasParent(styles.get(styles.size() - 1))) {
      StyleData parent = getParent(styles.get(styles.size() - 1));
      if (parent != null) {
        styles.add(parent);
      } else {
        break;
      }
    }
    for (int i = initialSize; i < styles.size(); i++) {
      StyleData style = styles.get(i);
      AttributeResource value = style.getAttrValue(resName);
      if (value != null) return value;
    }

    // todo: is this tested?
    if (theme != null) {
      AttributeResource value = theme.getAttrValue(resName);
      if (value != null) return value;
    }

    return null;
  }

  private static String getParentStyleName(StyleData style) {
    if (style == null) {
      return null;
    }
    String parent = style.getParent();
    if (parent == null || parent.isEmpty()) {
      parent = null;
      String name = style.getName();
      if (name.contains(".")) {
        parent = name.substring(0, name.lastIndexOf('.'));
        if (parent.isEmpty()) {
          return null;
        }
      }
    }
    return parent;
  }

  private static boolean hasParent(StyleData style) {
    if (style == null) return false;
    String parent = style.getParent();
    return parent != null && !parent.isEmpty();
  }

  private StyleData getParent(StyleData style) {
    String parent = getParentStyleName(style);

    if (parent == null || "Null".equals(parent)) return null;

    if (parent.startsWith("@")) parent = parent.substring(1);

    ResName styleRef = ResName.qualifyResName(parent, style.getPackageName(), "style");

    styleRef = dereferenceResName(styleRef);

    // TODO: Refactor this to a ResourceLoaderChooser
    ResourceLoader resourceLoader = "android".equals(styleRef.packageName) ? systemResourceLoader : appResourceLoader;
    TypedResource typedResource = resourceLoader.getValue(styleRef, qualifiers);

    if (typedResource == null) {
      StringBuilder builder = new StringBuilder("Could not find any resource")
          .append(" from reference ").append(styleRef)
          .append(" from ").append(style)
          .append(" with ").append(theme);
      throw new RuntimeException(builder.toString());
    }

    Object data = typedResource.getData();
    if (data instanceof StyleData) {
      return (StyleData) data;
    } else {
      StringBuilder builder = new StringBuilder(styleRef.toString())
          .append(" does not resolve to a Style.")
          .append(" got ").append(data).append(" instead. ")
          .append(" from ").append(style)
          .append(" with ").append(theme);
      throw new RuntimeException(builder.toString());
    }
  }

  private ResName dereferenceResName(ResName res) {
    ResName styleRef = res;
    boolean dereferencing = true;
    while ("attr".equals(styleRef.type) && dereferencing) {
      dereferencing = false;
      for (StyleData parentStyle : styles) {
        AttributeResource value = parentStyle.getAttrValue(styleRef);
        if (value != null) {
          styleRef = dereferenceAttr(value);
          dereferencing = true;
          break;
        }
      }
      if (!dereferencing && theme != null) {
        AttributeResource value = theme.getAttrValue(styleRef);
        if (value != null) {
          styleRef = dereferenceAttr(value);
          dereferencing = true;
        }
      }
    }

    return styleRef;
  }

  private ResName dereferenceAttr(AttributeResource attr) {
    if (attr.isResourceReference()) {
      return attr.getResourceReference();
    } else if (attr.isStyleReference()) {
      return attr.getStyleReference();
    }
    throw new RuntimeException("Found a " + attr + " but can't cast it :(");
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StyleResolver)) {
      return false;
    }
    StyleResolver other = (StyleResolver) obj;

    return ((theme == null && other.theme == null) || (theme != null && theme.equals(other.theme)))
        && ((myResName == null && other.myResName == null)
        || (myResName != null && myResName.equals(other.myResName)))
        && Strings.equals(qualifiers, other.qualifiers);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    hashCode = 31 * hashCode + (theme != null ? theme.hashCode() : 0);
    hashCode = 31 * hashCode + (myResName != null ? myResName.hashCode() : 0);
    hashCode = 31 * hashCode + Strings.nullToEmpty(qualifiers).hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    return styles.get(0) + " (and parents)";
  }

}