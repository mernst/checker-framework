import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("nullness") // Don't want to depend on @Nullable
public class GenericTest12b {
  class Cell<T1 extends Object> {}

  class Node<CONTENT extends @NonNull Object> {
    public Node(Cell<CONTENT> userObject) {}

    void nodecall(Cell<CONTENT> userObject) {}
  }

  class RootNode extends Node<Void> {
    public RootNode() {
      super(new Cell<Void>());
      call(new Cell<Void>());
      nodecall(new Cell<Void>());
    }

    void call(Cell<Void> userObject) {}
  }
}
