# How to release

## Releasing your first version

We will use a modified version of standard-version since we would like it to automatically update our pom.xml file.

`npx dwmkerr/standard-version --packageFiles pom.xml --bumpFiles pom.xml --first-release`

This will release your very first version, you can then run `git push --follow-tags` to push the new release to GitHub which will trigger the build process

## Normal releasing

It assumes you are using Semantic Versioning - which is well worth a read however the tl;dr version is:

You have three types of releases: major.minor.patch each changing the version number by one. You would run
`npx dwmkerr/standard-version --packageFiles pom.xml --bumpFiles pom.xml --release-as <major|minor|patch>`
when you are ready to change that specific version.
