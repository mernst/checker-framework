import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;

public class MustCallArrayTest {

  @MustCall({}) String mcEmpty;

  @MustCall({"hashCode"}) String mcHashCode;

  @MustCall({"toString"}) String mcToString;

  @MustCallUnknown String mcUnknown;

  void clientSetMcEmpty() {
    mcEmpty = mcHashCode;
    mcEmpty = mcToString;
    mcEmpty = mcUnknown;
  }

  void clientSetMcHashCode() {
    mcHashCode = mcEmpty;
    mcHashCode = mcToString;
    mcHashCode = mcUnknown;
  }

  void clientSetMcToString() {
    mcToString = mcEmpty;
    mcToString = mcHashCode;
    mcToString = mcUnknown;
  }

  void clientSetMcUnknown() {
    mcUnknown = mcEmpty;
    mcUnknown = mcHashCode;
    mcUnknown = mcToString;
  }

  void requiresMustCallEmptyObject(@MustCall({}) Object o) {}

  void requiresMustCallHashCodeObject(@MustCall({"hashCode"}) Object o) {}

  void requiresMustCallToStringObject(@MustCall({"toString"}) Object o) {}

  void requiresMustCallUnknownObject(@MustCallUnknown Object o) {}

  void requiresMustCallEmptyString(@MustCall({}) String s) {}

  void requiresMustCallHashCodeString(@MustCall({"hashCode"}) String s) {}

  void requiresMustCallToStringString(@MustCall({"toString"}) String s) {}

  void requiresMustCallUnknownString(@MustCallUnknown String s) {}

  void client(Integer i, Integer[] ia) {
    requiresMustCallEmptyObject(i);
    requiresMustCallEmptyObject(ia);
    requiresMustCallEmptyObject(mcHashCode);
    requiresMustCallEmptyObject(mcToString);
    requiresMustCallEmptyObject(mcEmpty);
    requiresMustCallEmptyObject(mcUnknown);

    requiresMustCallEmptyString(mcHashCode);
    requiresMustCallEmptyString(mcToString);
    requiresMustCallEmptyString(mcEmpty);
    requiresMustCallEmptyString(mcUnknown);

    requiresMustCallHashCodeObject(i);
    requiresMustCallHashCodeObject(ia);
    requiresMustCallHashCodeObject(mcHashCode);
    requiresMustCallHashCodeObject(mcToString);
    requiresMustCallHashCodeObject(mcEmpty);
    requiresMustCallHashCodeObject(mcUnknown);

    requiresMustCallHashCodeString(mcHashCode);
    requiresMustCallHashCodeString(mcToString);
    requiresMustCallHashCodeString(mcEmpty);
    requiresMustCallHashCodeString(mcUnknown);

    requiresMustCallToStringObject(i);
    requiresMustCallToStringObject(ia);
    requiresMustCallToStringObject(mcHashCode);
    requiresMustCallToStringObject(mcToString);
    requiresMustCallToStringObject(mcEmpty);
    requiresMustCallToStringObject(mcUnknown);

    requiresMustCallToStringString(mcHashCode);
    requiresMustCallToStringString(mcToString);
    requiresMustCallToStringString(mcEmpty);
    requiresMustCallToStringString(mcUnknown);

    requiresMustCallUnknownObject(i);
    requiresMustCallUnknownObject(ia);
    requiresMustCallUnknownObject(mcHashCode);
    requiresMustCallUnknownObject(mcToString);
    requiresMustCallUnknownObject(mcEmpty);
    requiresMustCallUnknownObject(mcUnknown);

    requiresMustCallUnknownString(mcHashCode);
    requiresMustCallUnknownString(mcToString);
    requiresMustCallUnknownString(mcEmpty);
    requiresMustCallUnknownString(mcUnknown);
  }
}
