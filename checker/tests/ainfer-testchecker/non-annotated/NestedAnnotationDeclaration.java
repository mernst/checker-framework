// Tests that WPI does not crash on a nested annotation declaration that declares a constant
// field.  Ajava-based WPI creates no wrapper for an annotation declaration, because inference
// is not supported for annotation declarations, but the members of the annotation declaration
// are visited nonetheless.  A constant field is such a member; an annotation element is not,
// because an annotation element is not a field declaration.

import org.checkerframework.checker.testchecker.ainfer.qual.AinferSibling1;

public class NestedAnnotationDeclaration {

  @interface NestedAnno {
    /** A constant field, which is not an annotation element. */
    String CONSTANT = "constant";

    String value();
  }

  private Object field;

  void setField(@AinferSibling1 Object o) {
    field = o;
  }

  void useField() {
    // :: warning: [assignment]
    @AinferSibling1 Object o = field;
  }
}
