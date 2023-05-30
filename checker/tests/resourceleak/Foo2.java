import org.checkerframework.checker.mustcall.qual.*;

class Foo2 {

  @CreatesMustCallFor("this") // Foo2.this
  void resetFoo2() {}

  void other2() {
    Runnable r =
        new Runnable() {
          @Override
          @CreatesMustCallFor("this")
          // :: error: creates.mustcall.for.override.invalid
          public void run() {
            // This error definitely must be issued, since Foo2.this != this.
            // :: error: reset.not.owning
            resetFoo2();
          }
        };
  }
}
