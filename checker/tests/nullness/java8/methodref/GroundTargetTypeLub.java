import org.checkerframework.checker.nullness.qual.*;

interface Supplier<T extends @NonNull Object> {
  T supply();
}

interface Supplier2<T> {
  T supply();
}

class GroundTargetType {

  static @Nullable Object myMethod() {
    return null;
  }

  Supplier<?> fn = GroundTargetType::myMethod;
  // :: error: (methodref.return)
  Supplier<@NonNull ?> fn2 = GroundTargetType::myMethod;

  // Supplier2
  // :: error: (methodref.return)
  Supplier2<@NonNull ?> fn3 = GroundTargetType::myMethod;
}
