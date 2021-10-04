# Contributing to Exadel Authoring Kit for AEM
Contributions to Exadel Authoring Kit for AEM (part of Exadel Toolbox) are welcomed and greatly appreciated. We want to make contributing to this project as easy and transparent as possible, whether it is:
- Reporting a bug;
- Proposing new features;
- Discussing the current state of the code;
- Submitting a fix or solutions;
- Becoming a maintainer.

## We develop with Github
We use Github to host code, to track issues and feature requests, as well as accept pull requests.

## We use Github Flow
All code changes happen through Pull Requests.

Pull requests are the best way to propose changes to the codebase (please follow this guide [Github Flow](https://guides.github.com/introduction/flow/index.html)).

We actively welcome your pull requests:
1. Fork the repo and create your branch from `develop`.
2. Do code changes.
   Whenever you create new files, add "Licenced under the Apache Licence..." header (use any of the existing files to copy the full header). Whenever you create new methods, add Javadoc / JSDoc. Alter existing Javadoc / JSDoc if you change a method's signature.
3. If you've added code that should be tested, add tests. Make sure that the tests pass.
4. If you've changed APIs, update the README and/or documentation under `docs` if needed.
5. Make sure your code lints.
6. Issue the pull request.

#### Procedural pull request questions

Every pull requests is dedicated to a single Github issue. Every issue has a tracking number like `EDMTP-333`.

A branch for the pull request must be named in the format `bugfix/EDMTP-333` or `feature/EDMTP-333` where the part before the slash is the kind of PR (reflecting a bug or a feature request respectively), and the part after the slash in the tracking number.

A pull request's title must start with the tracking number in square brackets; then comes a brief but detailed description of what is done in this PR like `[EDMTP-333] Fixed NPE when saving file to a removable media`.

Detailed description in "description" section is optional but welcomed. You can assign labels from the provided set, such as `bug`, `enhancement`, `documentation`, etc.

Every pull request consists of one or more commits. Commit messages message must be presented in the same format as the pull request title. E.g., the following 3 commits: `[EDMTP-333] Implemented the NPE fix... [EDMTP-333] Altered Javadoc for the affected method... [EDMTP-333] Added a unit test for the NPE fix`.

## Licensing
Any contributions you make are understood to be under the  [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) that covers the whole project. Feel free to contact the maintainers if that's a concern.

After creating your first pull request, you will be asked to sign our Contributor License Agreement by commenting your PR with a special message.

## Bug reporting
We use Github's [issues](https://github.com/exadel-inc/etoolbox-authoring-kit/issues) to track public bugs.
Report a bug by opening a new issue.

#### Write bug reports with detail
[This is an template of bug report](https://github.com/exadel-inc/repository-template/blob/main/.github/ISSUE_TEMPLATE/bug_report.md).

## Use a Compliant and Consistent Coding Style

#### For Java code

* We stick to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) in essential parts.
* We use 4 spaces for indentation rather than tabs.
* Every import is a separate line.
* Avoid static imports (like constants, etc.) unless absolutely must.
* Give meaningful names to classes, methods and variables.
* Split lengthy methods in smaller parts.
* Observe length of lines, and split lengthy lines where appropriate (between method arguments, before ternary operators, etc.).
* Avoid introducing multi-line lambdas.
* Observe succession of methods: public, then package-private, then private; publish static, then package-private static, etc. You may place a private method immediately after the preceding public if called from this public as a secondary routine.
* Add `try-catch` blocks to reduce the risk of code termination. Never ignore caught exceptions.
* When unsure, follow the style of the existing code files.

You can use the [.editorconfig file](https://github.com/exadel-inc/etoolbox-authoring-kit/blob/master/.editorconfig) to plug in you IDE.

#### For JavaScript / CSS / LESS code

* Use the predefined [eslint rules](https://github.com/exadel-inc/etoolbox-authoring-kit/blob/master/ui.apps/.eslintrc.json) to verify code with  an IDE such as IntelliJ.
* The rules are automatically applied via a Github workflow when you create of modify a pull request.
* When unsure, follow the style of the existing code files.

#### For XML markup files

* Use proper indentation.
* Split long lines into smaller ones by attributes.
* When unsure, follow the style of the existing code files.

## Community and behavioral expectations

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone, regardless of age, body size, visible or invisible disability, ethnicity, sex characteristics, gender identity and expression, level of experience, education, socio-economic status, nationality, personal appearance, race, religion, or sexual identity and orientation.

We expect contributors, reviewers and participants to express their opinions in a friendly, polite and clear manner, raise and address issues in most precise, explaining and accurate sentences.

We pledge to act and interact in ways that contribute to an open, welcoming, diverse, inclusive, and healthy community.
