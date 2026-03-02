import org.checkerframework.common.value.qual.BoolVal;
import org.checkerframework.common.value.qual.IntRange;

public class FullRange {

  @IntRange(from = -2147483648, to = 2147483647) int int1;

  int int2;

  void intM1() {
    int1 = int2;
  }

  /*
  void intM2() {
    int2 = int1;
  }
  */

  @IntRange(from = -32768, to = 32767) short short1;

  short short2;

  /*
  void shortM1() {
    short1 = short2;
  }

  void shortM2() {
    short2 = short1;
  }
  */

  @IntRange(from = -128, to = 127) byte byte1;

  byte byte2;

  /*
  void byteM1() {
    byte1 = byte2;
  }

  void byteM2() {
    byte2 = byte1;
  }
  */

  @BoolVal({true, false}) boolean boolean1;

  boolean boolean2;

  /*
  void booleanM1() {
    boolean1 = boolean2;
  }

  void booleanM2() {
    boolean2 = boolean1;
  }
  */
}
