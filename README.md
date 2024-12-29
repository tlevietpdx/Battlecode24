# Battlecode 2024 - 4 sprints development simulation

This is the Battlecode 2024 simulation for 4 sprints Agile development cycle. The term project is hosted where we follow professional software engineer routine to development *strategic features* for our *ducks* (agents) to capture enemy team's flag.

Throughout the term, we have constantly stayed at the top 1 ranking for winning most matches against other teams. Our agents are dominant in the battlefield and actively attack to capture the flags in the shortest round possible. 

For each development sprint, we would compare our development features against the previous sprint to make sure that we are making our agents stronger and better.

### Project Structure

- `README.md`
  This file.
- `build.gradle`
  The Gradle build file used to build and run players.
- `src/`
  Team development source code.
- `test/`
  Team development test code.
- `client/`
  Contains the client. The proper executable can be found in this folder.
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

### Credits
Aadit Bagdi -
	*Global Upgrade features*

Viet Thanh Le (myself) -
	*Attack Duck, Navigation, Overall strategy features*

Vienna Wagner -
	*Healer Duck features*

Taylor Hughes -
    *Builder Duck features*
