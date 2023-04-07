import org.checkerframework.checker.mustcall.qual.MustCall;
// import org.checkerframework.checker.mustcall.qual.MustCallUnknown;

public class MustCallArrayTestSmall {

  // @MustCall({}) String mcEmpty;

  @MustCall({"hashCode"}) String mcHashCode;

  @MustCall({"toString"}) String foo() {
    return mcHashCode;
  }

  // @MustCall({"toString"}) String mcToString;

  // @MustCallUnknown String mcUnknown;

  // void clientSetMcEmpty() {
  //   mcEmpty = mcHashCode;
  //   mcEmpty = mcToString;
  //   mcEmpty = mcUnknown;
  // }

  // void clientSetMcHashCode() {
  //   // mcHashCode = mcEmpty;
  //   mcHashCode = mcToString;
  //   // mcHashCode = mcUnknown;
  // }

  // void clientSetMcToString() {
  //   // mcToString = mcEmpty;
  //   mcToString = mcHashCode;
  //   // mcToString = mcUnknown;
  // }

  // void clientSetMcUnknown() {
  //   mcUnknown = mcEmpty;
  //   mcUnknown = mcHashCode;
  //   mcUnknown = mcToString;
  // }
}
