# Contributing to NotLib

Thanks for taking the time to contribute! Here's everything you need to get started.

## Getting started

1. **Fork** the repository and clone your fork locally.
2. Make sure you have **Java 21+** and **Maven** installed.
3. Build the project:
   ```bash
   mvn clean package -DskipTests
   ```

## How to contribute

### Reporting bugs

Open a [bug report](https://github.com/NotMarra/NotLib/issues/new?template=bug_report.md) and include:
- NotLib version
- Paper / Folia version
- A minimal reproduction case
- The full stack trace if applicable

### Suggesting features

Open a [feature request](https://github.com/NotMarra/NotLib/issues/new?template=feature_request.md) and describe:
- The problem you're trying to solve
- Your proposed solution
- Any alternatives you've considered

### Submitting a pull request

1. Create a branch from `main`:
   ```bash
   git checkout -b feat/my-feature
   # or
   git checkout -b fix/my-bug
   ```

2. Follow the code style of the surrounding code (no auto-formatter enforced, just be consistent).

3. Write a clear commit message. We loosely follow [Conventional Commits](https://www.conventionalcommits.org):
   ```
   feat: add upsertAll batch method to EntityRepository
   fix: flush() using insertAll instead of upsert causing duplicate key errors
   docs: add QueryBuilder reference page
   ```

4. Open a pull request against `main`. Fill in the PR template.

## Code guidelines

- **No breaking changes** to public APIs without a discussion issue first.
- New public methods should have Javadoc.
- If you add a feature, add a usage example in the relevant test class under `test/`.
- Keep pull requests focused — one feature or fix per PR.

## Questions?

Open a [GitHub Discussion](https://github.com/NotMarra/NotLib/discussions) or ask in the issue tracker.
