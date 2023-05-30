import org.checkerframework.checker.mustcall.qual.*;

class Foo1 {

  @CreatesMustCallFor("this")
  void resetFoo1() {}

  void other() {

    Runnable r =
        new Runnable() {
          @Override
          @CreatesMustCallFor("Foo1.this")
          // :: error: creates.mustcall.for.override.invalid
          public void run() {
            // [The following explanation is incorrect.  The problem is a bug in creating
            // implicit "this" expressions.]
            // Ideally, we would not issue the following error. However, the Checker Framework's
            // JavaExpression support
            // (https://checkerframework.org/manual/#java-expressions-as-arguments)
            // treats all versions of "this" (including "Foo1.this") as referring to the object
            // that directly contains the annotation, so we treat this call to resetFoo1 as not
            // permitted.
            // :: error: reset.not.owning
            resetFoo1();
          }
        };
  }
}
