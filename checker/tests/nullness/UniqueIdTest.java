import org.checkerframework.checker.initialization.qual.UnknownInitialization;

interface UniqueIdInterface {
  public default String className(
      @UnknownInitialization(UniqueIdInterface.class) UniqueIdInterface this) {
    return getClass().getSimpleName() + this.getClass().getSimpleName();
  }
}

class UniqueIdClass {
  public String className(@UnknownInitialization(UniqueIdClass.class) UniqueIdClass this) {
    return getClass().getSimpleName() + this.getClass().getSimpleName();
  }
}
