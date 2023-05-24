# Contributing to Exadel Authoring Kit for AEM
Contributions to Exadel Authoring Kit for AEM (part of Exadel Toolbox) are welcomed and greatly appreciated. We want to make contributing to this project as easy and transparent as possible, whether it is:
- Reporting a bug;
- Proposing new features;
- Discussing the current state of the code;
- Submitting a fix or solutions;
- Becoming a maintainer.

## We develop with Github
We use Github to host code, track issues, accumulate feature requests, review and accept pull requests.

## We use Github Flow
All code changes happen through Pull Requests.

Pull requests are the best way to propose changes to the codebase (please follow this guide [Github Flow](https://guides.github.com/introduction/flow/index.html)).

We actively welcome your pull requests:
1. Fork the repo and create your branch from `develop`.
2. Do code changes. Whenever you create new files, add the "Licenced under the Apache Licence..." header (use any of the  existing files to copy the full header). Whenever you create new methods, add Javadoc / JSDoc. Alter existing Javadoc
   / JSDoc if you change a method's signature.
3. If you've added code that should be tested, add unit tests under the _test_ folder of the respective module. Make sure that the tests pass.
4. If your code covers the features that cannot be verified without live connectivity to an AEM server or a 3rd party service, add an integration test under the _it.tests_ module. Make sure that the tests pass.
5. If you've changed APIs, update the documentation under `docs/content` if needed.
6. Make sure your code lints.
7. Issue the pull request.

#### Procedural pull request questions

Every pull request is dedicated to a single Github issue. Every issue has a tracking number like `EAK-333`.

A branch for the pull request must be named in the format `bugfix/EAK-333` or `feature/EAK-333` where the part before the slash is the kind of PR (reflecting a bug or a feature request, respectively), and the part after the slash in the tracking number.

A pull request's title must start with the tracking number in square brackets; then comes a brief but detailed description of what is done in this PR like `[EAK-333] Fixed NPE when saving file to a removable media`.

A more verbose description in the "description" section is optional but welcomed. You can assign labels from the provided set, such as `bug`, `enhancement`, `documentation`, etc.

Every pull request consists of one or more commits. Commit messages must be presented in the same format as the pull request title. E.g., the following 3 commits: `[EAK-333] Implemented the NPE fix... [EAK-333] Altered Javadoc for the affected method... [EAK-333] Added a unit test for the NPE fix`.

## Licensing
Any contributions you make are understood to be under the  [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) that covers the whole project. Feel free to contact the maintainers if that's a concern.

After creating your first pull request, you will be asked to sign our [Contributor License Agreement](CLA.md) by commenting on your PR with a special message.

## Bug reporting
We use Github's [issues](https://github.com/exadel-inc/etoolbox-authoring-kit/issues) to track public bugs.
Report a bug by opening a new issue.

#### Write bug reports with detail
[This is an template of bug report](https://github.com/exadel-inc/repository-template/blob/main/.github/ISSUE_TEMPLATE/bug_report.md).

## Use a Compliant and Consistent Coding Style

#### For POM files

* We place dependencies in the alphabetic order of their `groupId`-s except for the `uber-jar` that comes last to allow overlaying bundled dependencies.
* Use `dependencyManagement` / `pluginManagement` sections of the main POM to specify the common requisites, scope, and config values of dependencies. Override them in a dependent POM file only if necessary.

#### For Java code

* We stick to the [Code Conventions for the Java Programming Language](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html) and also to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) in essential parts.
* We use 4 spaces for indentation rather than tabs.
* Every import is a separate line, no wildcard imports.
* Avoid static imports (like constants, etc.).
* Give meaningful names to classes, methods, and variables.
* Observe the proper sequence of imports: `java.*`, then `javax.*`, then all the third-part packages after an empty line, last come `com.exadel.*` packages.
* Avoid long lines. No line should exceed 150 characters. Split lengthy lines where appropriate (between method arguments, before ternary operators, etc.).
* Split lengthy methods into smaller parts. A method body must not exceed 20-25 lines.
* Avoid introducing multi-line lambdas.
* Observe the proper sequence of declarations: static variables (constants), then instance variables, then constructors, then instance methods, then static methods, then static nested classes.
* Observe the proper sequence of visibility modifiers: public, then protected, then package-private, then private; public static, then package-private static, etc. You may place a private method immediately after the preceding public if called from this public as a secondary routine.
* Avoid excessive visibility of classes or class member. E.g., do not make a class `public` if it is only called from other classes within this very package. In a public class, do not make a method `public` unless it will be called from a class residing in another package.
* Declare methods that do not have a context (neither read nor mutate the instance fields of a class) as `static`.
* Do not create methods with number of parameters greater than 4.
* Use constants: no "magic" numbers and literals.
* Add `try-catch` blocks to reduce the risk of code termination. Never ignore caught exceptions.
* When unsure, follow the style of the existing code files.

You can use the [.editorconfig file](https://github.com/exadel-inc/etoolbox-authoring-kit/blob/master/.editorconfig) to plug in you IDE.

#### For JavaScript / CSS / LESS code

* Use the predefined [eslint rules](https://github.com/exadel-inc/etoolbox-authoring-kit/blob/master/ui.apps/.eslintrc.json) to verify code with an IDE such as IntelliJ.
* The rules are automatically applied via a Github workflow when you create or modify a pull request.
* When unsure, follow the style of the existing code files.

#### For XML markup files

* Use proper indentation.
* Split long lines into smaller ones by attributes.
* When unsure, follow the style of the existing code files.

## Community and behavioral expectations

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, religion, or sexual identity and orientation.

We expect contributors, reviewers, and participants to express their opinions in a friendly, polite, and clear manner, raise and address issues in most precise, explaining and accurate sentences.

We pledge to act and interact in ways that contribute to an open, welcoming, diverse, inclusive, and healthy community.
