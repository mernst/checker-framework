import org.checkerframework.checker.testchecker.wholeprograminference.qual.Top;

public class ConstructorTest {

  public ConstructorTest(int top) {}

  void test() {
    @Top int top = (@Top int) 0;
    // :: warning: (argument)
    new ConstructorTest(top);
  }
}
