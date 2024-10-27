# Pacathon Submission

## What is your team name?

_Team Name:_ Rukevwe

---

## Who is on your team?

| Name          | BU Email        | BU ID     | GitHub Account      |
|---------------|-----------------|-----------|---------------------|
| Rukevwe Omusi              |  ojomusi@bu.edu               | U48761806          | ojomusi                    |
|               |                 |           |                     |
|               |                 |           |                     |

---

## How does your Pacman work?

My Pac-Man model utilizes A* Search and Breadth-First Search (BFS) algorithms to identify the nearest pellet while prioritizing power pellets based on distance. The model implements a scoring system that penalizes the agent for failing to find any pellets within a 10-second timeframe. Suppose the agent has not located any pellets in 5 seconds. In that case, it reviews its movement history to determine if it has repeatedly moved in the same direction for the last four updates. In such cases, the model evaluates available movement options and directs the agent to change its trajectory.

Additionally, the agent is rewarded for maintaining a consistent direction when moving towards pellets, while also taking into account the closest pellet's position and ensuring that it can navigate without hitting walls. The final direction is chosen based on neural network outputs, which consider factors like movement history, available directions, and distances to pellets. 


---

## Checklist:

I ensure that I:
- [X ] have included my model's weights (the `.json` file) in my commit
- [X ] have configured the tournament behavior to use those weights
- [X ] copied over any code needed to run my model (e.g., all inputs to the neural network)
- [X ] tested the tournament at least once before submission (just to make sure everything is working)
- [X ] have not intentionally modified the game state in any way


# Pacathon

Welcome to the Fall 2024 Pacathon! We have already setup Pacman and some basic Neural Network stuff for you to get started. Your task is to build the best Pacman player. You can use any algorithm you want, but we recommend sticking to Neural Networks through Neat4j (the library we have been building out during AI Society meetings).

To help anybody feeling lost, we also have some video tutorials:
1. Getting Started: https://www.youtube.com/watch?v=L6oWLffBQmA
2. How to start training: https://www.youtube.com/watch?v=QRUsuvQZHrE
3. Saving your training: https://www.youtube.com/watch?v=2mkhrvVbZsI
4. Submitting your final code: https://www.youtube.com/watch?v=mvRI4aAv8d8

## Pacman
For you today, we have a *nearly perfect* implementation of Pacman with a few key differences:
1. Randomness is not seeded, so patterns do not work
2. After level 255, you can keep going

If you have never seen Pacman before, you can see some gameplay [here](https://www.youtube.com/watch?v=i_OjztdQ8iw),
and learn about the internals of the game [here](https://pacman.holenet.info/) (we followed this guide very closely
as we implemented Pacman).

## Teams
You may work solo, or in teams of 2 **or more**. Note that we will only be able to distribute **up to** 2 prizes per team. Thus,
we recommend having teams of 2 for this tournament. 

## Tournament
During the tournament, all submitted neural networks will go through the same gauntlet (The gauntlet, in this tournament, is actually
*much easier* than the base game). In general, here are the levels your pacman goes through:
* `Level 1`: No ghosts
* `Level 2`: Only clyde
* `Level 3`: Only Blinky
* `Level 4`: Only Pinky
* `Level 5`: Blinky + Clyde
* `Level 6`: Blinky + Pinky
* `Level 7`: Blinky + Inky + Clyde
* `Level 8`: Blinky + Pinky + Clyde
* `Level 9`: Blinky + Pinky + Inky
* `Level 10+`: All 4 ghosts
    * As levels progress, ghosts get faster
    * As levels progress, ghosts scatter less often
    * As levels progress, ghosts are frightened for less time

Your Pacman will lose 1 life if they do not collect any score within any 40 second interval ($60 \cdot 40$ frames). 

Pacman is a surprisingly difficult game, so getting to level 10 after <1 day
of training is **seriously impressive**. That being said, *if* any models
get that far, the levels will go on infinitely as needed. 

## Workflow
Although you have access to every file for running NEAT, Pacman, and graphics, you really only need to worry about these: 

1. Make a fork of this repository, and clone your fork locally.
2. Navigate to the `NeatPacmanBehavior` class in `./core/src/main/java/com/buaisociety/pacathon/entity/behavior/NeatPacmanBehavior.java`.
    * This will be where you choose the inputs and rewards for your neural network.
    * *This file is just used for testing*, so feel free to "break" the game here (e.g. randomly kill of pacman early).
3. Navigate to the `SpecialTrainingConditions` class in `./core/src/main/java/com/buaisociety/pacathon/SpecialTrainingConditions.java`.
    * This will be where you can setup special events for your training (e.g. removing ghosts so that pacman can train without dying).
    * *This file is just used for testing*, so feel free to "break" the game here (e.g. clearing ghost lists, changing the board, etc.).
4. Navigate to the `createNeat()` method in `./core/src/main/java/com/buaisociety/pacathon/Main.java`.
    * This is where you can create the `Neat` object that will train your neural network.
    * We have included a simple example to get you started.
    * You can also load a pretrained `Neat` object from your previous runs.
5. Train your network!

Once you have a network ready to submit, you should [submit a pull request](https://github.com/buaiml/Pacathon/compare). Once you submit 
a PR, you are all done!


## Setup
You should be running Java 21 or higher. You can check your java version by running `java -version` in your terminal. If it prints something like `openjdk version "21.0.5"`, than you are good to go (The 21 is the important part). If not, you can download java 21 from amazon corretto for [mac](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/macos-install.html), [windows](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/windows-install.html), or [linux](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/generic-linux-install.html). You'll need to make sure that your PATH is set
up correctly, so that when you run `java -version` it prints the correct version.

> > If you are having any troubles, you can copy-paste these instructions into ChatGPT and ask for help with uninstalling old versions and installing Java 21 on your OS. 

## Modules

- `core`: The actual Pacman game code.
- `lwjgl3`: The desktop launcher for the game.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

For example, you can run the application on desktop with the following command:
```bash
./gradlew lwjgl3:run
```

> > Note: If you are using Windows, you should use Git Bash, WSL, or another terminal of your choice to run the provided commands. Otherwise, you can use the Command Prompt, but you should replace `./gradlew` with `gradlew.bat`.

## Tips
Chances are, none of you have ever done this before. You will have to try
new things in order to succeed. Break the problem down into components, and
ask yourself; "what information do ***I*** need to play Pacman?"

Also consider that you may use any graph or algorithm to feed into the neural
network. 

You are also free to do research!
