import org.checkerframework.checker.initialization.qual.UnknownInitialization;

interface UniqueIdTest {

  public default String getClassAndUid(
      @UnknownInitialization(UniqueIdTest.class) UniqueIdTest this) {
    return this.getClass().getSimpleName() + "#";
  }
}
