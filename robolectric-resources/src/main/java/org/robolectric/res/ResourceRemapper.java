package org.robolectric.res;

import com.google.common.collect.HashBiMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.offset;

public class ResourceRemapper {

  private Map<String, Integer> resIds = HashBiMap.create();
  private ResourceIdGenerator resourceIdGenerator = new ResourceIdGenerator(0x7F);

  void remapRClass(boolean allowFinal, Class<?> rClass) {
    if (rClass != null) {
      reconcileResourceIds(allowFinal, rClass);
    }
  }

  /**
   * IDs are in the format
   *
   * 0x PPTTEEEE
   *
   * where:
   *
   * P is unique for the package
   * T is unique for the type
   * E is the entry within that type.
   */
  private void reconcileResourceIds(boolean allowFinal, Class<?> rClass) {
    // Collect all the local attribute id -> name mappings. These are used when processing the stylables to look up
    // the reassigned values.
    Map<Integer, String> localAttributeIds = new HashMap<>();
    for (Class<?> aClass : rClass.getClasses()) {
      if (aClass.getSimpleName().equals("attr")) {
        for (Field field : aClass.getFields()) {
          try {
            localAttributeIds.put(field.getInt(null), field.getName());
          } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not read attr value for " + field.getName(), e);
          }
        }
      }
    }

    for (Class<?> innerClass : rClass.getClasses()) {
      String resourceType = innerClass.getSimpleName();
      if (!resourceType.startsWith("styleable")) {
        for (Field field : innerClass.getFields()) {
          try {
            if (!allowFinal && Modifier.isFinal(field.getModifiers())) {
              throw new IllegalArgumentException(rClass + " contains final fields, these will be inlined by the compiler and cannot be remapped.");
            }

            String resourceName = resourceType + "/" + field.getName();
            Integer value = resIds.get(resourceName);
            if (value != null) {
              field.setAccessible(true);
              field.setInt(null, value);
            } else if (resIds.containsValue(field.getInt(null))) {
              int remappedValue = resourceIdGenerator.generate(resourceType, field.getName());
              field.setInt(null, remappedValue);
            } else {
              resourceIdGenerator.record(field.getInt(null), resourceType, field.getName());
              resIds.put(resourceName, field.getInt(null));
            }
          } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
          }
        }
      } else {
        // Reassign the ids in the style arrays accordingly.
        for (Field field : innerClass.getFields()) {
          if (field.getType().equals(int[].class)) {
            try {
              int[] styleableArray = (int[]) (field.get(null));
              for (int k = 0; k < styleableArray.length; k++) {
                Integer value = resIds.get("attr/" + localAttributeIds.get(styleableArray[k]));
                if (value != null) {
                  styleableArray[k] = value;
                }
              }
            } catch (IllegalAccessException e) {
              throw new IllegalStateException(e);
            }
          }
        }
      }
    }
  }
}
