import org.checkerframework.checker.initialization.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.framework.qual.EnsuresQualifierIf;

public class Initializer {

  public String a;
  public String b = "abc";

  // :: error: (assignment.type.incompatible)
  public String c = null;

  public String d = ("");

  public Initializer() {
    // :: error: (assignment.type.incompatible)
    a = null;
    a = "";
    c = "";
  }

  // :: error: (initialization.fields.uninitialized)
  public Initializer(boolean foo) {}

  public Initializer(int foo) {
    a = "";
    c = "";
  }

  public Initializer(float foo) {
    setField();
    c = "";
  }

  public Initializer(double foo) {
    if (!setFieldMaybe()) {
      a = "";
    }
    c = "";
  }

  // :: error: (initialization.fields.uninitialized)
  public Initializer(double foo, boolean t) {
    if (!setFieldMaybe()) {
      // on this path, 'a' is not initialized
    }
    c = "";
  }

  @EnsuresQualifier(expression = "a", qualifier = NonNull.class)
  public void setField(@UnknownInitialization Initializer this) {
    a = "";
  }

  @EnsuresQualifierIf(result = true, expression = "a", qualifier = NonNull.class)
  public boolean setFieldMaybe(@UnknownInitialization Initializer this) {
    a = "";
    return true;
  }

  String f = "";

    void t1(@UnknownInitialization Initializer this) {
        // :: error: (dereference.of.nullable) :: error: (initialization.invalid.field.access)
        this.f.toString();
    }

  String fieldF = "";
}

class SubInitializer extends Initializer {

    String f = "";

    void subt1(@UnknownInitialization(Initializer.class) SubInitializer this) {
        fieldF.toString();
        super.f.toString();
        // :: error: (dereference.of.nullable) :: error: (initialization.invalid.field.access)
        this.f.toString();
    }

    void subt2(@UnknownInitialization SubInitializer this) {
        // :: error: (dereference.of.nullable)
        fieldF.toString();
        // :: error: (dereference.of.nullable) :: error: (initialization.invalid.field.access)
        super.f.toString();
        // :: error: (dereference.of.nullable) :: error: (initialization.invalid.field.access)
        this.f.toString();
    }
}
