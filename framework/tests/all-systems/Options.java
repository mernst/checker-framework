public class Options {

  private Class main_class;

  @SuppressWarnings("initialization.invalid.field.access")
  public Options() {
    throw new Error("" + main_class);
  }
}
