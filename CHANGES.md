Changes
=======

2.1.1 (unreleased)

2.1.0 (2023-09-25)

- Rename `--xml`-parameter to `--jacoco-xml`-parameter, and rename `XmlReportGenerator` to `JacocoXmlReportGenerator`.
- Add `--sonar-xml` to generate the SonarQube "Generic test coverage report format".
- Add `--cobertura-xml` to generate the Cobertura coverage format.
- Speed up creation of HTML report.

2.0.2 (2023-08-21)

- Fix error where JaCoCo coverage for a class, but the class is not known.
- Cache class dependecy map, severly speeding up conversion.

2.0.1 (2023-01-25)

- Fix extracting proc name crashing on `proc`-method calls

2.0.0 (2023-01-24)

- Merge primary with subsidiary classes
- Better separation of lines hit from primary classes, while constructs are stored on subsidiary classes
- Refactoring

1.1.0 (2023-01-19)

- Fix entry point
- Update dependencies
- Fix method names in XML
- Rename option `--filter-primary` to `--discard-executable`
- Rename option `--product-dir` to `--product-path`
- Support multiple product dirs
- Support procs
- Properly discard lines from executable classes
- Make bundle name configurable with option `--bundle-name`
- Refactoring

1.0.0 (2021-08-29)

- Initial version
