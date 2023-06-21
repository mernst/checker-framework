// @above-java17-jdk-skip-test TODO: reinstate, false positives may be due to issue #979

import org.checkerframework.checker.fenum.qual.Fenum;
import org.checkerframework.framework.testchecker.lib.UncheckedByteCode;

public class UpperBoundsInByteCode {
  UncheckedByteCode<@Fenum("Foo") String> foo;
  UncheckedByteCode<@Fenum("Bar") Object> bar;

  void typeVarWithNonObjectUpperBound(@Fenum("A") int a) {
    // :: error: (type.argument)
    UncheckedByteCode.methodWithTypeVarBoundedByNumber(a);
    UncheckedByteCode.methodWithTypeVarBoundedByNumber(1);
  }

  void wildcardsInByteCode() {
    UncheckedByteCode.unboundedWildcardParam(foo);
    UncheckedByteCode.lowerboundedWildcardParam(bar);
    // :: error: (argument)
    UncheckedByteCode.upperboundedWildcardParam(foo);
  }

  SourceCode1<@Fenum("Foo") String> foo1;
  SourceCode2<@Fenum("Foo") String> foo2;
  // :: error: (type.argument)
  SourceCode3<@Fenum("Foo") String> foo3;

  class SourceCode1<T> {}

  class SourceCode2<T extends Object> {}

  class SourceCode3<T extends String> {}
}
