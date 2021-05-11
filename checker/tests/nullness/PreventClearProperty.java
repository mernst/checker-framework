// Same code (but different expected errors) as test PermitClearProperty.java .

import java.util.Properties;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.StringVal;

public class PreventClearProperty {

  static final @StringVal("line.separator") String LINE_SEPARATOR = "line.separator";

  static final @StringVal("my.property.name") String MY_PROPERTY_NAME = "my.property.name";

  @NonNull String getLineSeparator1() {
    return System.getProperty("line.separator");
  }

  @NonNull String getLineSeparator2() {
    // :: error: (return)
    return System.getProperty(LINE_SEPARATOR);
  }

  @NonNull String getMyProperty1() {
    // :: error: (return)
    return System.getProperty("my.property.name");
  }

  @NonNull String getMyProperty2() {
    // :: error: (return)
    return System.getProperty(MY_PROPERTY_NAME);
  }

  @NonNull String getAProperty(String propName) {
    // :: error: (return)
    return System.getProperty(propName);
  }

  @NonNull String clearLineSeparator1() {
    // :: error: (return)
    // :: error: (clear.system.property)
    return System.clearProperty("line.separator");
  }

  @NonNull String clearLineSeparator2() {
    // :: error: (return)
    // :: error: (clear.system.property)
    return System.clearProperty(LINE_SEPARATOR);
  }

  @NonNull String clearMyProperty1() {
    // :: error: (return)
    return System.clearProperty("my.property.name");
  }

  @NonNull String clearMyProperty2() {
    // :: error: (return)
    // :: error: (clear.system.property)
    return System.clearProperty(MY_PROPERTY_NAME);
  }

  @NonNull String clearAProperty(String propName) {
    // :: error: (return)
    // :: error: (clear.system.property)
    return System.clearProperty(propName);
  }

  void callSetProperties(Properties p) {
    // :: error: (clear.system.property)
    System.setProperties(p);
  }

  // All calls to setProperty are legal because they cannot unset a property.

  @NonNull String setLineSeparator1() {
    return System.setProperty("line.separator", "somevalue");
  }

  @NonNull String setLineSeparator2() {
    // :: error: (return)
    return System.setProperty(LINE_SEPARATOR, "somevalue");
  }

  @NonNull String setMyProperty1() {
    // :: error: (return)
    return System.setProperty("my.property.name", "somevalue");
  }

  @NonNull String setMyProperty2() {
    // :: error: (return)
    return System.setProperty(MY_PROPERTY_NAME, "somevalue");
  }

  @NonNull String setAProperty(String propName) {
    // :: error: (return)
    return System.setProperty(propName, "somevalue");
  }

  // These calls to setProperty are illegal because null is not a permitted value.

  @NonNull String setLineSeparatorNull1() {
    // :: error: (argument)
    return System.setProperty("line.separator", null);
  }

  @NonNull String setLineSeparatorNull2() {
    // :: error: (argument)
    // :: error: (return)
    return System.setProperty(LINE_SEPARATOR, null);
  }

  @NonNull String setMyPropertyNull1() {
    // :: error: (argument)
    // :: error: (return)
    return System.setProperty("my.property.name", null);
  }

  @NonNull String setMyPropertyNull2() {
    // :: error: (argument)
    // :: error: (return)
    return System.setProperty(MY_PROPERTY_NAME, null);
  }

  @NonNull String setAPropertyNull(String propName) {
    // :: error: (argument)
    // :: error: (return)
    return System.setProperty(propName, null);
  }
}
