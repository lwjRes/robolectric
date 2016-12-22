package org.robolectric.res;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceRemapperTest {

  private ResourceRemapper remapper = new ResourceRemapper();

  @Test(expected = IllegalArgumentException.class)
  public void forbidFinalRClasses() {
    remapper.remapRClass(false, FinalRClass.class);
  }

  @Test
  public void testRemap() {
    remapper.remapRClass(true, org.robolectric.R.class);
    remapper.remapRClass(false, org.robolectric.lib1.R.class);
    remapper.remapRClass(false, org.robolectric.lib2.R.class);
    remapper.remapRClass(false, org.robolectric.lib3.R.class);

    // Resource identifiers that are common across libraries should be remapped to the same value.
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib1.R.string.in_all_libs);
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib2.R.string.in_all_libs);
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib3.R.string.in_all_libs);

    // Resource identifiers that clash across two libraries should be remapped to different values.
    assertThat(org.robolectric.lib1.R.id.lib1_button)
        .isNotEqualTo(org.robolectric.lib2.R.id.lib2_button);

    // Styleable arrays of values should be updated to match the remapped values.
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib1.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib2.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib3.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.R.attr.offsetX, org.robolectric.R.attr.offsetY);
  }

  public static final class FinalRClass {
    public static final class string {
      public static final int a_final_value = 0x7f020001;
      public static final int another_final_value = 0x7f020002;
    }
  }

}
