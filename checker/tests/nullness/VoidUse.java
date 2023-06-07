import org.checkerframework.checker.nullness.qual.*;

public class VoidUse {

  private Class<?> main_class1 = Void.TYPE;

  private Class<?> main_class2 = Void.TYPE;

  public Void voidReturn(Void p) {
    voidReturn(null);
    return null;
  }

  // Void is treated as Nullable.  Is there a value on having it be NonNull?
  public abstract static class VoidTestNode<T extends @NonNull Object> {}

  public static class VoidTestInvNode extends VoidTestNode<@NonNull Void> {}

  class Scanner<P extends @NonNull Object> {
    public void scan(Object tree, P p) {}
  }

  // :: error: (type.argument)
  class MyScanner extends Scanner<Void> {
    void use(MyScanner ms) {
      ms.scan(new Object(), null);
    }
  }

  // :: error: (type.argument)
  class MyScanner2 extends Scanner<@Nullable Object> {
    void use(MyScanner2 ms) {
      ms.scan(new Object(), null);
    }
  }

  // Test case for issue #230
  Class<?> voidClass() {
    return void.class;
  }

  Class<?> VoidClass() {
    return Void.class;
  }

  Class<?> intClass() {
    return int.class;
  }

  Class<?> ListClass() {
    return java.util.List.class;
  }
}
