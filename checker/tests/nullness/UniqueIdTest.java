import org.checkerframework.checker.initialization.qual.UnknownInitialization;

interface UniqueIdTest {

  public default String getClassAndUid(@UnknownInitialization(UniqueId.class) UniqueId this) {
    return this.getClass().getSimpleName() + "#" + getUid();
  }
}
