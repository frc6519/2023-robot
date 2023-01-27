# Team 6519 Code for FRC 2023 Charged Up

## Dependencies
[`WPILib 2023.2.1`](https://github.com/wpilibsuite/allwpilib/releases/tag/v2023.2.1) or its VS Code Extension

[`Java Development Kit 11`](https://www.oracle.com/java/technologies/downloads/#java11) or Higher

`Gradle 7.5.1` installed by WPILib

## Getting Started
Clone the repository with the command `git clone https://github.com/vegasvortechs6519/2023-robot.git`

## Push changes to GitHub
Commit the changes using `git commit -a -m ""`

Push the changes to GitHub using `git push origin`

## Build Code with Gradle
1. Right-click on the `build.gradle` file in Visual Studio Code and press `Build Robot Code`.
2. Make sure the build was successful within the terminal, fix any problems if not.
3. Connect to the robot wirelessly or through a USB connection.
4. Right-click on the `build.gradle` file in Visual Studio Code and press `Deploy Robot Code`.

## Git Flow commands/guide
- `git branch <>`: Creates a new branch with the name specified.
- `git flow init`: Initializes `git flow` onto the project, and creates new branches for existing repositories.
- `git checkout <>`: Switches to the specified branch. 
- `git flow feature start feature_branch`: Creates a `feature branch`.
- `git flow feature finish feature_branch`: Merges the `feature branch` back into `develop`
- `git flow release track <release name>`: Tracks the release specified.

1. Intialize `git flow` on the project using `git flow init`.
2. Create a `feature branch` using `git flow feature start <feature name>`.
3. Work on project until development is finished, then merge it to `develop` using `git flow feature finish <feature name>`.
4. Start a release using `git flow release start <release name>`.
5. Finish the release and publish it to branch `master` using `git flow release finish <realease name>` and `git push origin`

[Guide and Cheat Sheet](https://danielkummer.github.io/git-flow-cheatsheet/)