# Running Java Project Instructions

## Running 100 games

1. Open the project in IntelliJ IDEA.
2. Navigate to the `src/main/java/org.acquire in the Project Explorer.
3. Using IDE, run Main.java
   1. Run the following command to run 100 games: `mvn exec:java`
4. If you want to modify the players or their strategies, look at line 20 in Main.java which holds the String jsonData which you can modify.
5. For reference, the "start" request that runs a game with the automated players (as seen in the jsonData) follows the format:
   1. { "request" : "start", "players" : [Player...] }
   2. Player = { "player" : String, "strategy" : String}
   3. Strategy can be "ordered", "randoom", "smallest anti" or "largest alpha.
6. Results of 100 games are saved in 'game-results.txt'

## Running Tests Using Maven Terminal Command

1. Open a terminal or command prompt.
2. Navigate to the root directory of your Maven project.
3. Run the following command to execute all tests: `mvn test`

# Adding Test Input and Output Files

To add test input and output files for your Java project, follow these steps:

1. **Test Input Files**: Place your test input files in the `src/test/resources/inputJson` directory. This location is standard for this Maven projects and ensures that your test resources are properly managed and accessible during test execution.

2. **Test Output Files**: Place your test input files in the `src/test/resources/outputJson` directory.
3. **Change I/O Directory**: In `pom.xml` change the values of `inputDirectory` and `outputDirectory` key's to your preference, inside the `build`.

Remember to adjust file paths in your test cases accordingly.
   