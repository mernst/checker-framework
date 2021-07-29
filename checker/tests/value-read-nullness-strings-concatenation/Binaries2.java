import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;

public class Binaries2 {
  public void add() {
    @NonNull String g = "A";
    if (true) {
      g = "B";
    }
    @StringVal({"AC", "BC"}) String h = g + "C";
  }
}
