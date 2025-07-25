package org.checkerframework.framework.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.javacutil.BugInCF;
import org.plumelib.util.StringsPlume;

/**
 * Used to create an instance of TestConfiguration. TestConfigurationBuilder is fluent: it returns
 * itself after every call so you can string together configuration methods as follows:
 *
 * <p>{@code new TestConfigurationBuilder() .addOption("-Awarns") .addSourceFile("src1.java")
 * .addDiagnosticFile("src1.out") }
 *
 * @see TestConfiguration
 */
public class TestConfigurationBuilder {

  // Presented first are static helper methods that reduce configuration building to a method
  // call.
  // However, if you need more complex configuration or custom configuration, use the
  // constructors provided below.

  /**
   * This creates a builder for the default configuration used by Checker Framework JUnit tests.
   *
   * @param testSourcePath the path to the Checker test file sources, usually this is the directory
   *     of Checker's tests
   * @param outputClassDirectory the directory to place classes compiled for testing
   * @param classPath the classpath to use for compilation
   * @param testSourceFiles the Java files that compose the test
   * @param processors the checkers or other annotation processors to run over the testSourceFiles
   * @param options the options to the compiler/processors
   * @param shouldEmitDebugInfo true if debug information should be emitted
   * @return the builder that will create an immutable test configuration
   */
  public static TestConfigurationBuilder getDefaultConfigurationBuilder(
      String testSourcePath,
      File outputClassDirectory,
      String classPath,
      Iterable<File> testSourceFiles,
      Iterable<@BinaryName String> processors,
      List<String> options,
      boolean shouldEmitDebugInfo) {

    TestConfigurationBuilder configBuilder =
        new TestConfigurationBuilder()
            .setShouldEmitDebugInfo(shouldEmitDebugInfo)
            .addProcessors(processors)
            .addOption("-Xmaxerrs", "9999")
            .addOption("-g")
            .addOption("-Xlint:unchecked")
            .addSourceFiles(testSourceFiles);

    if (outputClassDirectory != null) {
      configBuilder.addOption("-d", outputClassDirectory.getAbsolutePath());
    }

    configBuilder
        .addOptionIfValueNonEmpty("-sourcepath", testSourcePath)
        .addOption("-implicit:class")
        .addOption("-classpath", classPath);

    configBuilder.addOptions(options);
    return configBuilder;
  }

  /**
   * This is the default configuration used by Checker Framework JUnit tests.
   *
   * @param testSourcePath the path to the Checker test file sources, usually this is the directory
   *     of Checker's tests
   * @param testFile a single test Java file to compile
   * @param processor a single checker to include in the processors field
   * @param options the options to the compiler/processors
   * @param shouldEmitDebugInfo true if debug information should be emitted
   * @return a TestConfiguration with input parameters added plus the normal default options,
   *     compiler, and file manager used by Checker Framework tests
   */
  @SuppressWarnings(
      "signature:cast.unsafe" // for non-array non-primitive class, getName(): @BinaryName
  )
  public static TestConfiguration buildDefaultConfiguration(
      String testSourcePath,
      File testFile,
      Class<?> processor,
      List<String> options,
      boolean shouldEmitDebugInfo) {
    return buildDefaultConfiguration(
        testSourcePath,
        Arrays.asList(testFile),
        Collections.emptyList(),
        Arrays.asList((@BinaryName String) processor.getName()),
        options,
        shouldEmitDebugInfo);
  }

  /**
   * This is the default configuration used by Checker Framework JUnit tests.
   *
   * @param testSourcePath the path to the Checker test file sources, usually this is the directory
   *     of Checker's tests
   * @param testSourceFiles the Java files that compose the test
   * @param processors the checkers or other annotation processors to run over the testSourceFiles
   * @param options the options to the compiler/processors
   * @param shouldEmitDebugInfo true if debug information should be emitted
   * @return a TestConfiguration with input parameters added plus the normal default options,
   *     compiler, and file manager used by Checker Framework tests
   */
  public static TestConfiguration buildDefaultConfiguration(
      String testSourcePath,
      Iterable<File> testSourceFiles,
      Iterable<@BinaryName String> processors,
      List<String> options,
      boolean shouldEmitDebugInfo) {
    return buildDefaultConfiguration(
        testSourcePath,
        testSourceFiles,
        Collections.emptyList(),
        processors,
        options,
        shouldEmitDebugInfo);
  }

  /**
   * This is the default configuration used by Checker Framework JUnit tests.
   *
   * @param testSourcePath the path to the Checker test file sources, usually this is the directory
   *     of Checker's tests
   * @param testSourceFiles the Java files that compose the test
   * @param classpathExtra extra entries for the classpath, needed to compile the source files
   * @param processors the checkers or other annotation processors to run over the testSourceFiles
   * @param options the options to the compiler/processors
   * @param shouldEmitDebugInfo true if debug information should be emitted
   * @return a TestConfiguration with input parameters added plus the normal default options,
   *     compiler, and file manager used by Checker Framework tests
   */
  public static TestConfiguration buildDefaultConfiguration(
      String testSourcePath,
      Iterable<File> testSourceFiles,
      Collection<String> classpathExtra,
      Iterable<@BinaryName String> processors,
      List<String> options,
      boolean shouldEmitDebugInfo) {

    String classPath = getDefaultClassPath();
    if (!classpathExtra.isEmpty()) {
      classPath +=
          System.getProperty("path.separator")
              + String.join(System.getProperty("path.separator"), classpathExtra);
    }

    File outputDir = getOutputDirFromProperty();

    TestConfigurationBuilder builder =
        getDefaultConfigurationBuilder(
            testSourcePath,
            outputDir,
            classPath,
            testSourceFiles,
            processors,
            options,
            shouldEmitDebugInfo);
    return builder.validateThenBuild(true);
  }

  /** The list of files that contain Java diagnostics to compare against. */
  private List<File> diagnosticFiles;

  /** The set of Java files to test against. */
  private List<File> testSourceFiles;

  /** The set of Checker Framework processors to test with. */
  private final Set<@BinaryName String> processors;

  /** The set of options to the Javac command line used to run the test. */
  private final SimpleOptionMap options;

  /** Should the Javac options be output before running the test. */
  private boolean shouldEmitDebugInfo;

  /**
   * Note: There are static helper methods named buildConfiguration and buildConfigurationBuilder
   * that can be used to create the most common types of configurations
   */
  public TestConfigurationBuilder() {
    diagnosticFiles = new ArrayList<>();
    testSourceFiles = new ArrayList<>();
    processors = new LinkedHashSet<>();
    options = new SimpleOptionMap();
    shouldEmitDebugInfo = false;
  }

  /**
   * Create a builder that has all of the options in initialConfig.
   *
   * @param initialConfig initial configuration for the newly-created builder
   */
  @SuppressWarnings("this-escape")
  public TestConfigurationBuilder(TestConfiguration initialConfig) {
    this.diagnosticFiles = new ArrayList<>(initialConfig.getDiagnosticFiles());
    this.testSourceFiles = new ArrayList<>(initialConfig.getTestSourceFiles());
    this.processors = new LinkedHashSet<>(initialConfig.getProcessors());
    this.options = new SimpleOptionMap();
    this.addOptions(initialConfig.getOptions());

    this.shouldEmitDebugInfo = initialConfig.shouldEmitDebugInfo();
  }

  /**
   * Ensures that the minimum requirements for running a test are met. These requirements are:
   *
   * <ul>
   *   <li>There is at least one source file
   *   <li>There is at least one processor (if requireProcessors has been set to true)
   *   <li>There is an output directory specified for class files
   *   <li>There is no {@code -processor} option in the optionMap (it should be added by
   *       addProcessor instead)
   * </ul>
   *
   * @param requireProcessors if true, require that there is at least one processor
   * @return a list of errors found while validating this configuration
   */
  public List<String> validate(boolean requireProcessors) {
    List<String> errors = new ArrayList<>();
    if (testSourceFiles == null || !testSourceFiles.iterator().hasNext()) {
      errors.add("No source files specified!");
    }

    if (requireProcessors && !processors.iterator().hasNext()) {
      errors.add("No processors were specified!");
    }

    Map<String, @Nullable String> optionMap = options.getOptions();
    if (!optionMap.containsKey("-d") || optionMap.get("-d") == null) {
      errors.add("No output directory was specified.");
    }

    if (optionMap.containsKey("-processor")) {
      errors.add("Processors should not be added to the options list");
    }

    return errors;
  }

  /**
   * Adds the given path option to {@code this}.
   *
   * @param key the key to add
   * @param toAppend the path to append
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder adddToPathOption(String key, String toAppend) {
    this.options.addToPathOption(key, toAppend);
    return this;
  }

  /**
   * Adds the given diagnostics file to {@code this}.
   *
   * @param diagnostics the diagnostics file to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addDiagnosticFile(File diagnostics) {
    this.diagnosticFiles.add(diagnostics);
    return this;
  }

  /**
   * Adds the given diagnostics files to {@code this}.
   *
   * @param diagnostics diagnostics files to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addDiagnosticFiles(Iterable<File> diagnostics) {
    this.diagnosticFiles = catListAndIterable(diagnosticFiles, diagnostics);
    return this;
  }

  /**
   * Sets the diagnostics files of {@code this}.
   *
   * @param diagnosticFiles diagnostics files to set on {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder setDiagnosticFiles(List<File> diagnosticFiles) {
    this.diagnosticFiles = new ArrayList<>(diagnosticFiles);
    return this;
  }

  /**
   * Adds the given source file to {@code this}.
   *
   * @param sourceFile source file to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addSourceFile(File sourceFile) {
    this.testSourceFiles.add(sourceFile);
    return this;
  }

  /**
   * Adds the given source files to {@code this}.
   *
   * @param sourceFiles source files to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addSourceFiles(Iterable<File> sourceFiles) {
    this.testSourceFiles = catListAndIterable(testSourceFiles, sourceFiles);
    return this;
  }

  /**
   * Sets the source files of {@code this}.
   *
   * @param sourceFiles source files to set on {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder setSourceFiles(List<File> sourceFiles) {
    this.testSourceFiles = new ArrayList<>(sourceFiles);
    return this;
  }

  /**
   * Sets the given options on {@code this}.
   *
   * @param options options to set on {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder setOptions(Map<String, @Nullable String> options) {
    this.options.setOptions(options);
    return this;
  }

  /**
   * Adds the given option to {@code this}.
   *
   * @param option option to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addOption(String option) {
    this.options.addOption(option);
    return this;
  }

  /**
   * Adds the given option and value to {@code this}.
   *
   * @param option option to add to {@code this}
   * @param value value to add
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addOption(String option, String value) {
    this.options.addOption(option, value);
    return this;
  }

  /**
   * Adds the given option to {@code this} if the value is non-empty.
   *
   * @param option option to add to {@code this}
   * @param value value to add, iff it is non-empty
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addOptionIfValueNonEmpty(String option, String value) {
    if (value != null && !value.isEmpty()) {
      return addOption(option, value);
    }

    return this;
  }

  /**
   * Adds the given options to this.
   *
   * @param options options to add to this
   * @return this
   */
  @SuppressWarnings("nullness:return") // need @PolyInitialized annotation
  @RequiresNonNull("this.options")
  public TestConfigurationBuilder addOptions(
      @UnknownInitialization(TestConfigurationBuilder.class) TestConfigurationBuilder this,
      Map<String, @Nullable String> options) {
    this.options.addOptions(options);
    return this;
  }

  /**
   * Adds the given options to {@code this}.
   *
   * @param newOptions options to add to {@code this}
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder addOptions(Iterable<String> newOptions) {
    this.options.addOptions(newOptions);
    return this;
  }

  /**
   * Set the processors.
   *
   * @param processors the processors to run
   * @return this
   */
  public TestConfigurationBuilder setProcessors(Iterable<@BinaryName String> processors) {
    this.processors.clear();
    for (String proc : processors) {
      this.processors.add(proc);
    }
    return this;
  }

  /**
   * Add a processor.
   *
   * @param processor a processor to run
   * @return this
   */
  public TestConfigurationBuilder addProcessor(@BinaryName String processor) {
    this.processors.add(processor);
    return this;
  }

  /**
   * Add processors.
   *
   * @param processors processors to run
   * @return this
   */
  public TestConfigurationBuilder addProcessors(Iterable<@BinaryName String> processors) {
    for (String processor : processors) {
      this.processors.add(processor);
    }

    return this;
  }

  /**
   * Sets {@code this} to output debug info.
   *
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder emitDebugInfo() {
    this.shouldEmitDebugInfo = true;
    return this;
  }

  /**
   * Sets {@code this} to not output debug info.
   *
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder dontEmitDebugInfo() {
    this.shouldEmitDebugInfo = false;
    return this;
  }

  /**
   * Sets {@code this} to output debug info depending on the parameter.
   *
   * @param shouldEmitDebugInfo if true, emit debug info
   * @return the current object {@code this}
   */
  public TestConfigurationBuilder setShouldEmitDebugInfo(boolean shouldEmitDebugInfo) {
    this.shouldEmitDebugInfo = shouldEmitDebugInfo;
    return this;
  }

  /**
   * Creates a TestConfiguration using the settings in this builder. The settings are NOT validated
   * first.
   *
   * @return a TestConfiguration using the settings in this builder
   */
  public TestConfiguration build() {
    return new ImmutableTestConfiguration(
        diagnosticFiles,
        testSourceFiles,
        new ArrayList<>(processors),
        options.getOptions(),
        shouldEmitDebugInfo);
  }

  /**
   * Creates a TestConfiguration using the settings in this builder. The settings are first
   * validated and a runtime exception is thrown if any errors are found
   *
   * @param requireProcessors true if there should be at least 1 processor specified, see method
   *     validate
   * @return a TestConfiguration using the settings in this builder
   */
  public TestConfiguration validateThenBuild(boolean requireProcessors) {
    List<String> errors = validate(requireProcessors);
    if (errors.isEmpty()) {
      return build();
    }

    throw new BugInCF(
        "Attempted to build invalid test configuration:%n" + "Errors:%n%s%n%s%n",
        String.join("%n", errors), this);
  }

  /**
   * Returns the set of Javac options as a flat list.
   *
   * @return the set of Javac options as a flat list
   */
  public List<String> flatOptions() {
    return options.getOptionsAsList();
  }

  @Override
  public String toString() {
    return StringsPlume.joinLines(
        "TestConfigurationBuilder:",
        "testSourceFiles=" + StringsPlume.join(" ", testSourceFiles),
        "processors=" + String.join(", ", processors),
        "options=" + String.join(", ", options.getOptionsAsList()),
        "shouldEmitDebugInfo=" + shouldEmitDebugInfo);
  }

  /**
   * Returns a list that first has the items from parameter list then the items from iterable.
   *
   * @param <T> the type of the elements in the resulting list
   * @param list a list
   * @param iterable an iterable
   * @return a list that first has the items from parameter list then the items from iterable
   */
  private static <T> List<T> catListAndIterable(
      List<? extends T> list, Iterable<? extends T> iterable) {
    List<T> newList = new ArrayList<>(list);

    for (T iterObject : iterable) {
      newList.add(iterObject);
    }

    return newList;
  }

  /** The output directory for tests. */
  public static final String TESTS_OUTPUTDIR = "tests.outputDir";

  /**
   * Determine the output directory from the {@code tests.outputDir} property.
   *
   * @return the output directory
   */
  public static File getOutputDirFromProperty() {
    return new File(
        System.getProperty(
            "tests.outputDir",
            "tests" + File.separator + "build" + File.separator + "testclasses"));
  }

  /**
   * Determine the default classpath from the {@code tests.classpath} property.
   *
   * @return the default classpath
   */
  public static String getDefaultClassPath() {
    String classpath = System.getProperty("tests.classpath", "tests" + File.separator + "build");
    String globalclasspath = System.getProperty("java.class.path", "");
    return classpath + File.pathSeparator + globalclasspath;
  }
}
