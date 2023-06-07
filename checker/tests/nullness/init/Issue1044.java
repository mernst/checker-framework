// Test case for Issue 1044
// https://github.com/typetools/checker-framework/issues/1044

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Issue1044 {
  static class Inner1<V> {
    // :: error: (initialization.field.uninitialized)
    V f;
  }

  static class Inner2<@Nullable T> {
    // :: error: (initialization.field.uninitialized)
    @NonNull T f;
  }

  static class Inner3<V> {
    V f;

    // :: error: (initialization.fields.uninitialized)
    Inner3() {}
  }

  static class Inner4<@Nullable T> {
    @NonNull T f;

    // :: error: (initialization.fields.uninitialized)
    Inner4() {}
  }

  static class Inner5<V> {
    @Nullable V f;
  }

  static class Inner6<@Nullable T> {
    T f;
  }

  static class Inner7<V> {
    @Nullable V f;

    Inner7() {}
  }

  static class Inner8<@Nullable T> {
    T f;

    Inner8() {}
  }

  static class Inner9<V extends @NonNull Object> {
    // :: error: (initialization.field.uninitialized)
    V f;
  }

  static class Inner10<V extends @NonNull Object> {
    V f;

    // :: error: (initialization.fields.uninitialized)
    Inner10() {}
  }

  static class Inner11<V extends @NonNull Object> {
    @Nullable V f;
  }

  static class Inner12<V extends @NonNull Object> {
    @Nullable V f;

    Inner12() {}
  }
}
