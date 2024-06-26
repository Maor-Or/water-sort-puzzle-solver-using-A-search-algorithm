# Water Sort Puzzle Game Solver

This repository contains a Java implementation of a Water Sort Puzzle Game Solver using the A* algorithm. The solver aims to solve the water sort puzzle by finding the optimal sequence of moves to achieve a goal state where all tubes contain uniform colors.

## Table of Contents
1. [Introduction](#introduction)
2. [Output Examples](#output-examples)
3. [Solution](#solution)
   - [Node Structure](#node-structure)
   - [Search Process](#search-process)
4. [Heuristic Function](#heuristic-function)
5. [Usage](#usage)
   - [Example Command](#example-command)

## Output Examples
![image](https://github.com/Maor-Or/water-sort-puzzle-solver-using-A-search-algorithm/assets/118377261/e2461ea6-63e5-461e-adf1-d7487912cbb1)
![image](https://github.com/Maor-Or/water-sort-puzzle-solver-using-A-search-algorithm/assets/118377261/b3f4a2aa-088c-4c10-9129-a25cf4cd1dc4)



## Introduction

The Water Sort Puzzle is a challenging and fun puzzle game where you have to sort colored water in tubes until all colors are in separate tubes. This solver uses the A* search algorithm to find the optimal solution.

## Solution

The program loads an example from the Examples directory and converts the given state of 2-Dimentional arrays into a node of a graph. Then, the solver employs the A* search algorithm, which is a popular pathfinding and graph traversal algorithm. The algorithm uses a priority queue to explore nodes with the lowest cost, which is determined by a heuristic function.

### Node Structure

Each state of the puzzle is represented by a `Node` which contains:
- `state`: The current configuration of tubes.
- `predecessorNode`: The previous node in the path.
- `hash`: A unique identifier for the state.
- `gValue`: The cost to reach the node.
- `hValue`: The heuristic value estimating the cost to reach the goal.

### Search Process

1. Initialize the open and closed sets.
2. While the open set is not empty, do the following:
   - Extract the node with the lowest `g + h` value.
   - If the node is the goal state, return the solution path.
   - Generate all possible successor nodes.
   - For each successor node:
     - If the node is in the open set with a lower cost, skip it.
     - If the node is in the closed set with a lower cost, move it to the open set.
     - Otherwise, add the node to the open set.
   - Trim the open and closed sets to limit memory usage.

## Heuristic Function

The heuristic function `h` is designed to estimate the cost to reach the goal state. The function takes into account several factors:
- The number of extra blocks in the tubes.
- The number of touching blocks in the tubes.
- The number of solved tubes.
- The number of unique colors at the top of the tubes.
- The number of empty tubes.

The heuristic function is a weighted sum of these factors:

```java
return amountOfExtraBlocks * w1 +
       touchingAmount * w2 +
       (numOfColors - solvedTubesAmount) * w3 +
       (numOfColors - amountOfUniqueToppings(state)) * w4 +
       countEmptyTubes(state);

```
## Usage
To use the solver, follow these steps:

- Clone the repository.
- Compile the Java files.
- Run the Main class with the desired example configuration.

### Example Command
```java
javac Main.java
java Main
```

