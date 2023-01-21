# Smallworld 5 JaCoCo reporter

[sw5-jacoco-reporter](https://github.com/StevenLooman/sw5-jacoco-reporter) is a tool to convert `jacoco.exec` generated by a Smallworld5 session to useful information for Magik development.


## Introduction

Using [JaCoCo](https://www.eclemma.org/jacoco/) coverage reports can be generated from Smallworld code. However, those reports will contain internal (Java) class names, internal (Java) method names. While very valuable, reading these reports is a bit tedious.

Additionally Smallworld generates "primary" classes which might be less interesting in a unit testing context. The additional classes are generated due to the way Smallworld 5 generates Java Bytecode.

It would be easier if the internal method names are converted to Magik method names, and possibly the additionally "primary" classes are removed/ignored. This tool provides a means to convert the coverage data to the original Magik method names. Optionally, the additionally generated "primary" classes are removed/ignored.


## Workflow to get code coverage data

A specific workflow is required to get the coverage data. The steps are as follows:

1. Compile modules to jars found in the `libs/` directory.
2. (Re)start the Smallworld session with the JaCoCo agent.
3. Run tests, store coverage data in `jacoco.exec` file.


### 1 Compile the modules

To be able to determine which lines were hit during the tests, it is required to compile the modules to jars files, found in the `libs/` directory of the product. This is required for the JaCoCO agent which is used later on.

You can compile to jar files by calling the method `sw:sw_product.compile_all_modules()` of your product(s). This will result in a `libs/` directory contaning a jar for each module. E.g.,

```magik
Magik> prd_dir << "..."
Magik> system.rmdir(prd_dir + "/libs", _true, _true)  # Remove any existing `libs/` directory first!
...
Magik> prd << smallworld_product.add_product(prd_dir)
sw_product(...)
Magik> prd.compile_all_modules()
...
Magik> quit()
```


### 2 Using the JaCoCo agent

JaCoCo inserts itself into the JVM through a so called Java Agent. The [JaCoCO Agent](https://www.eclemma.org/jacoco/trunk/doc/agent.html) records coverage data by instrumenting the loaded classes. Agents can be loaded on JVM startup by using the `-javaagent` option. E.g., `-javaagent:/path_to_agent.jar`.

To instrument the loaded module jars, the JaCoCO agent needs to installed when the JVM starts. The `-javaagent` option needs to the JVM through the run alias command. This can be done using the `-j` option. Combining the `-j` and `-agentpath` options, this results in, for example:
```sh
$ /opt/Smallworld/core/bin/share/runalias -e /opt/Smallworld/core/config/environment -j -javaagent:.../org.jacoco.agent-0.8.8-runtime.jar swaf
```


## 3 Running tests with instrumented code

After the compiling is done and the JVM is restarted with the JaCoCo agent, the modules can now be loaded using the pre-compiled jars found in the `libs/` directory. When loading these modules, Smallworld now using the jar file, instead of loading and compiling the magik files. Run your tests as you would normally do, for example by using:
```magik
Magik> prd_dir << "..."
Magik> prd << smallworld_product.add_product(prd_dir)
sw_product(...)
Magik> prd.run_tests()
...
Magik> quit()
```

After running the tests, quit your session (done through the `quit()` in the example above). A resulting file called `jacoco.exec` is written automatically, containing the coverage data.

This coverage data can be used by this tool to convert it to a human readable coverage report (html), containing Magik methods or coverage report interpretable by other programs (xml).


## Usage

Options:

- `--help`
  - Show help.
- `--product-path \[path_to_product_directory\]`
  - Path to your product.
- `--jacoco-file \[path_to_file\]`
  - Path to the `jacoco.exec` file.
- `--html \[path_to_directory\]`
  - Path to the directory to generate the HTML report in.
- `--xml \[path_to_file\]`
  - Path to the file to generate the XML at.
- `--discard-executable`
  - Discard executable classes, i.e., the class which does not hold any Magik methods.

To generate a HTML report:

```sh
$ java -jar sw5-jacoco-reporter.jar --product-path ... --jacoco-file .../jacoco.exec --html .../coveragereport
```

To generate a XML report:

```sh
$ java -jar sw5-jacoco-reporter.jar --product-path ... --jacoco-file .../jacoco.exec --xml .../coveragereport.xml
```


## Jenkins integration

To integrate the coverage results into Jenkins, the [Code Coverage API](https://plugins.jenkins.io/code-coverage-api/) plugin can be used. This plugin consumes the JaCoCo XML coverage report - created by this tool - and generates reports. Be sure to use the XML reports generated by this tool, otherwise you will have data related to the internal Java class names/methods, instead of the Magik exemplars/methods.


# Inner workings

For each magik source file, Smallworld 5 creates a Primary class and optionally a Subsidiary class. The Primary class contains the statements which are executed when loading the file, through the mehod called `execute()`. E.g., when you do a `show(...)` at the top level of a magik file, the `show` is executed via the `execute()` method.

Any nested "constructs", such as method definitions, procedures, loops, ... are compiled to a Subsidiary class as methods. The Primary class refers to this Subsidiary class through the `preload` method. The methods on the Subsidiary class are refered to via code in the Primary class/`execute` method.

When running the tests using the JaCoCo agent, the java method names are recorded. This software converts the Java names found in the coverage data back to Magik names, and merges the "constructs" where needed. The Primary and Subsidiary classes are merged to a single class, the Primary class.

If the option `--discard-executable` is given. The `<init>`/`preload`/`execute` methods and merged "constructs" are discarded from the Primary classes. These parts are never recorded as covered. Also, when running unit tests, these parts are most likely less important as well.


# Contributing

You can contribute by providing bug reports via [GitHub](https://github.com/StevenLooman/sw5-jacoco-reporter).


# License

This tool is licensed under GPLv3.
