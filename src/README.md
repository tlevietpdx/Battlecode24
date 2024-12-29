# Battlecode 2024 Scaffold

This is the Battlecode 2024 scaffold, containing an `examplefuncsplayer`. Read https://play.battlecode.org/bc24/getting-started!

**We are using a rewritten version of the client this year, so please let teh devs know
if you encounter any issues or have any feedback!**

### Project Structure

- `README.md`
  This file.
- `build.gradle`
  The Gradle build file used to build and run players.
- `src/`
  Player source code.
- `test/`
  Player test code.
- `client/`
  Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
  Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
  The output folder for match files.
- `maps/`
  The default folder for custom maps.
- `gradlew`, `gradlew.bat`
  The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
  Contains files used by the Gradle wrapper scripts. Can be safely ignored.

### (Pre) Getting Started

Information Retrieved from [BC24](https://play.battlecode.org/bc24/getting-started)

Before getting started, there are a few steps that first need to be accomplished in order to make the Battlecode 2024 Scaffold work.

#### Step 1 - Download and Install Java

You will need an Oracle Java Development Kit (JDK) version 8. Make sure to set an environement variable to the following:

```
$ export JAVA_HOME=< -- PATH replaced here -- >
```

This PATH should look _<u>similar</u>_, perhaps not idential, to the following:

`/Library/Java/JavaVirtualMachines/jdk1.8.0_111.jdk/Contents/Home`

If you are unsure, use the following command to change directories into the JavaVirtualMachines folder:

```
$ cd /Library/Java/JavaVirtualMachines
```

- **<u>Note: higher versions of Java DO NOT work</u>**

#### Step 2 - Ensure Code is Downloaded and Accessible

Assuming you have downloaded the Battlecode 2024 scaffold zip folder, or have cloned a github repository containing the Battlecode 2024 scaffold code, you need to change into the root directory. You will know that you are in the root directory if there exists a script called `gradlew`

#### Step 3 - Initial Set Up for Building

First Run:

```
$ ./gradlew update
```

Then Run:

```
$ ./gradlew build
```

You should now be set to go. The following can be ran to see what other gradle tasks are available. Essentially the 'help' equivalent:

```
$ ./gradlew -q tasks
```

**At this point there should be a folder called `client/`**

### How to get started

You are free to directly edit `examplefuncsplayer`.
However, we recommend you make a new bot by copying `examplefuncsplayer` to a new package under the `src` folder.

### Running Battlecode from the Terminal or IDE

Run games directly with the gradle task:

```
$ ./gradlew run -Pmaps=[map] -PteamA=[Team A] -PteamB=[Team B]
```

if the map and/or team is unspecified, then Battlecode will default to that listed in `gradle.properties`

### Useful Commands

- `./gradlew build`
  Compiles your player
- `./gradlew run`
  Runs a game with the settings in gradle.properties
- `./gradlew update`
  Update configurations for the latest version -- run this often
- `./gradlew zipForSubmit`
  Create a submittable zip file
- `./gradlew tasks`
  See what else you can do!

### Configuration

Look at `gradle.properties` for project-wide configuration.

If you are having any problems with the default client, please report to teh devs and
feel free to set the `compatibilityClient` configuration to `true` to download a different
version of the client.
