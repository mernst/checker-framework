// Test case for issue #4558: https://tinyurl.com/cfissue/4558

import org.checkerframework.checker.index.qual.SameLen;

public abstract class OuterThisJavaExpressionSmall {

  String s;

  OuterThisJavaExpressionSmall(String s) {
    this.s = s;
  }

  final class Inner {

    String s = "different from " + OuterThisJavaExpressionSmall.this.s;

    /*
    @SameLen("OuterThisJavaExpressionSmall.this.s") String f7() {
      // :: error: (return.type.incompatible)
      return s;
    }

    @SameLen("OuterThisJavaExpressionSmall.this.s") String f8() {
      // :: error: (return.type.incompatible)
      return this.s;
    }
    */

    @SameLen("OuterThisJavaExpressionSmall.this.s") String f9() {
      return OuterThisJavaExpressionSmall.this.s;
    }
  }
}
