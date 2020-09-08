<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/logo.png" width="600" alt="Doggo Pathfinder Logo">

#  doggo-pathfinder <img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/dog-emoji.png" width="30" alt="doggo">

 A cartoony pathfinding algorithm visualizer written in Kotlin.js/React
 
 This project has a live demo :arrow_right: [check it out!](https://rrbbrb.github.io/doggo-pathfinder/)
 
## Prerequisites
- JDK 13
- Kotlin 1.4
- Gradle 6.6

## To run the application
After importing the project to your IDE, let gradle build the project, then run the `browserDevelopmentRun` task in the gradle tool window.

## About the project
<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/path.png" width="400" alt="Doggo Pathfinder Logo">

Doggo is looking for his beloved toy -- a baseball. We will make use of some search algorithms to help him find a path. In other words, Doggo is the start node and baseball is the end node. You can draw trees on the board as obstacles which the search algorithms will circumvent.


Here are the algorithms you will find in this project:


<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/algorithms.png" width="400" alt="Doggo Pathfinder Logo"> <img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/maze.png" width="400" alt="Doggo Pathfinder Logo">

- Breadth-first search

- Depth-first search

- Dijkstra's algorithm

- A* search (with Manhattan distance heuristic)

- Recursive division (maze generation)


## How to 'play'
#### Desktop
1. Click or drag on an empty cell to draw trees

<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/draw-trees.gif" width="400" alt="Doggo Pathfinder Logo">


2. Click and drag Doggo or baseball to your desired cell

<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/move-nodes.gif" width="400" alt="Doggo Pathfinder Logo">


3. If you want to generate a random maze instead, click `GENERATE MAZE`.

4. Switch on `REMOVE TREE` if you wish to delete a tree on the board.

5. Hover over `VISUALIZE ALGORITHMS` to start an algorithm of your choice!

<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/maze-remove-dfs.gif" width="400" alt="Doggo Pathfinder Logo">



#### Mobile

This project is mobile friendly. To change positions for Doggo or baseball,  simply tap on it and tap again on your desired cell. To draw trees on the board, tap on an empty cell.

<img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/mobile-heart.png" width="250" alt="Doggo Pathfinder Logo">  <img src="https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/mobile-path.png" width="250" alt="Doggo Pathfinder Logo">

