import org.checkerframework.common.value.qual.IntRange;

public class FullRange {

  @IntRange(from = -2147483648, to = 2147483647) int y;

  int x;

  void m1() {
    x = y;
  }

  void m2() {
    y = x;
  }
}
