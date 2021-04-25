import java.util.Collection;
import java.util.Collections;
import org.checkerframework.checker.boxing.qual.PrimCollection;
import org.checkerframework.checker.boxing.qual.RefCollection;

public class EnhancedForJdkTest {

  void jdkClient(@RefCollection Collection<String> o, @PrimCollection Collection<String> f) {
    Collections.addAll(o, new String[] {});
    Collections.addAll(f, new String[] {});

    o.toString();
    f.toString();

    o.size();
    f.size();
  }
}
