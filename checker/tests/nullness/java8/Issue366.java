// Test case for Issue 366:
// https://github.com/typetools/checker-framework/issues/366
// @below-java8-jdk-skip-test

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

class Test {
    static Optional<@NonNull String> getPossiblyEmptyString() {
        return Optional.ofNullable(null);
    }

    // The type argument in "Optional<@Nullable String>" is illegal.
    // static Optional<@Nullable String> getPossiblyEmptyString2() {
    //     // The optional returned is still @NonNull.
    //     //:: error: (return.type.incompatible)
    //     return Optional.ofNullable(null);
    // }
}
