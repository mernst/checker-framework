import java.util.stream.IntStream;

public class PrimitiveIteratorTest {

  void jdkClient(IntStream is) {
    // :: error: (boxing)
    for (Integer i : is) {
      System.out.println(i);
    }
  }
}
