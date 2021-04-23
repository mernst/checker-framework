import java.util.Collection;
import java.util.Collections;
import org.checkerframework.checker.boxing.qual.EnhancedForForbidden;
import org.checkerframework.checker.boxing.qual.EnhancedForOk;

public class EnhancedForJdkTest {

  void jdkClient(@EnhancedForOk Collection<String> o, @EnhancedForForbidden Collection<String> f) {
    Collections.addAll(o);
    Collections.addAll(f);

    o.toString();
    f.toString();

    o.size();
    f.size();
  }
}
