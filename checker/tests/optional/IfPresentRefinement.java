// @skip-test until the feature is implemented

import java.util.Optional;

public class IfPresentRefinement {

  @SuppressWarnings("optional.parameter")
  void foo(Optional<String> o) {
    o.ifPresent(s -> o.get());
  }
}
