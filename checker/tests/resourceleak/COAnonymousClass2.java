import org.checkerframework.checker.mustcall.qual.*;

class Foo {

  @CreatesMustCallFor("this") // Foo.this
  void resetFoo() {}

  void other2() {
    Runnable r =
        new Runnable() {
          @Override
          @CreatesMustCallFor("this")
          // :: error: creates.mustcall.for.override.invalid
          public void run() {
            // This error definitely must be issued, since Foo.this != this.
            // :: error: reset.not.owning
            resetFoo();
          }
        };
  }
}
