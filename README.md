# Assignment 3. Maze Escape

Student ID: 252924
Name: Adilbek Nurassyl
Group: (fill in your group)
Maze size (rows x columns): 11 x 8 (88 cells, 157 walls, 118 slides)

This file is the report. How to start: `START_HERE.md`. The task: `docs/Assignment3.pdf`.

## Program output

The 13 lines my program prints on my data (`java -cp out Main`):

    Broken: 87
    Kept: 70
    Last broken wall: 127
    MST cost: 26355
    Heaviest broken wall: 893
    Path length: 27
    DFS visited: 67
    Dead ends: 29
    Zones: 10
    Largest zone: 15
    Is DAG: 0
    Flow stages: 7
    Fastest route cost: 12

`java tools/Check.java` shows all 13 lines right on the three examples (`tiny`, `small`, `dag`).
I also checked all 13 numbers on my data with a separate Python script (own Kruskal, Prim, recursive DFS,
SCC by reachability, Dijkstra and longest path on the zone graph): all values are equal.

My maze (the red line is the way out):

![maze](output/maze.png)

## Questions

**Mission 0. What do find and union do, and what makes them fast in your code?**
`find(x)` returns the root of the set of x. `union(a, b)` finds the two roots and, if they differ, hangs the smaller tree under the larger one and returns true; if the roots are equal it does nothing and returns false. Two things make it fast. Union by size keeps the trees low (height at most log n). Path compression in `find` makes every cell on the way point straight to the root, so later calls are almost one step. Together one operation costs O(alpha(n)), which is practically constant. `find` is written with loops, not recursion, so a chain of a million cells does not overflow the stack (I test this in `DisjointSetTest`).

**Mission 1. Why is Broken always R*C - 1, and Kept always (R-1)*(C-1)?**
At the start every cell is its own set: R*C sets. A wall is broken only when `union` returns true, and each such union reduces the number of sets by exactly one. The grid is connected, so in the end there is one set. Going from R*C sets to 1 set takes exactly R*C - 1 successful unions, so Broken = R*C - 1. The number of inner walls is R*(C-1) + (R-1)*C = 2RC - R - C. Kept = 2RC - R - C - (RC - 1) = RC - R - C + 1 = (R-1)*(C-1). Check on my data: 11*8 - 1 = 87 and 10*7 = 70.

**Mission 2. My MST cost from Kruskal and from Prim, and why they are equal:**
Both give 26355 (heaviest broken wall 893). All costs are different, so the minimum spanning tree is unique. Kruskal and Prim are both correct greedy algorithms (cut property: the cheapest wall that crosses a cut belongs to some MST), and a unique MST means both must find the same tree and therefore the same total cost. Also checked on a 1000 x 1000 maze: both give 534420590936.

**Mission 3. Why is there exactly one path between two cells? How did you avoid a stack overflow on a huge maze?**
The broken walls join R*C cells with R*C - 1 edges and the graph is connected, so it is a tree. A connected graph with n vertices and n - 1 edges has no cycle (a cycle would let us remove an edge and stay connected, but then n - 2 edges could not connect n vertices). Two different paths between the same cells would make a cycle, so the path is unique. The DFS does not use recursion: I keep my own stack as an int array (the cells of the current path) and, for every cell, the next direction to try (up, right, down, left). When the exit is marked, the stack is exactly the path from the start to the exit. I tested it on 1000 x 1000 with a small thread stack (`-Xss512k`): no overflow, about 170 ms.

**Mission 4. Why does the second pass of Kosaraju go on the reversed graph? Why do the slides have no cycle exactly when Zones equals the number of cells?**
After pass 1 the cell that finishes last lies in a source zone (a zone without incoming slides from other zones). On the original graph a DFS from it would leak into the zones after it. On the reversed graph the slides between zones point the other way, so the DFS from this cell can reach only cells that can reach it, and those inside its own zone, and it stops exactly at the zone border. Then the next zone is taken in the same way, and zones are found in topological order. For the second question: a zone is a set of cells that all reach each other. A cycle of slides puts all its cells in one zone, so a zone with 2 or more cells exists exactly when there is a cycle. (There are no slides from a cell to itself, so a one-cell zone has no cycle.) So no cycle means every zone has one cell, and Zones = number of cells; the other way round, Zones = number of cells means every zone has one cell, so there is no cycle.

**Mission 5. Why is it enough to go through the zones in the order of their numbers? What does this pass cost?**
The zones are numbered in topological order: every slide between two different zones goes from a smaller number to a larger one. So when the loop reaches zone z, all the slides into z come from zones with smaller numbers, which are already processed, and the values for z (longest chain, cheapest cost) are final. Therefore no priority queue is needed (Dijkstra is for graphs that can have cycles). Each slide is looked at once, and each zone once, so the pass costs O(Z + S), where Z is the number of zones and S the number of slides (plus O(Z + S) for grouping the slides by zone with counting sort).

## AI use

Tool: Claude (Anthropic), in the chat.
What for: Claude wrote the first version of the code in `DisjointSet`, `Maze`, `Escape` and `Zones`, the extra checks in `DisjointSetTest`, and the draft of this README, and it checked the results with a separate script. I read the code, ran it myself and I can explain every line.
No code or text was taken from a book or a website.

## Timing (Mission 7)

Milliseconds, 5 runs each, from `java -cp out Bench` (average of 5 runs; the first runs are slower because the JVM is still warming up).

| maze | Kruskal | Prim |
|---|---|---|
| 100 x 100 | 19 | 17 |
| 300 x 300 | 50 | 95 |
| 1000 x 1000 | 322 | 1200 |

What do the numbers say about how the time grows? From 100 x 100 to 1000 x 1000 the number of cells grows 100 times, and the time grows about 17 times for Kruskal and about 70 times for Prim, so both are close to linear-times-a-log factor (O(m log m)), not quadratic. Kruskal is faster on large mazes because it sorts the walls once (a primitive `long` array) and then does almost-constant DSU steps, while Prim puts boxed numbers into a priority queue with random memory access, which is slower when the data is big. The small mazes are dominated by JVM warm-up, so their times are not exact. (These numbers were measured on my sandbox; on another computer they will differ, but the growth should look the same.)

## Complexity

n is the number of cells, m the number of walls (or slides), Z the number of zones.

| algorithm | time | space | assumptions |
|---|---|---|---|
| DSU find and union | O(alpha(n)) per operation, practically O(1) | O(n) | union by size and path compression |
| Kruskal | O(m log m) (sort) + O(m alpha(n)) = O(m log m) | O(n + m) | walls sorted by cost, tree is ready after n - 1 unions |
| Prim | O(m log m) = O(m log n) | O(n + m) | binary-heap priority queue with old entries skipped (lazy), adjacency lists |
| DFS | O(n + m) | O(n) | iterative, own stack, a tree maze has m = n - 1 |
| Kosaraju | O(n + m) | O(n + m) | two DFS passes, lists of slides for the graph and for the reversed graph |
| Route in the zone graph | O(Z + m) | O(Z + m) | zones in topological order, counting sort of slides by zone, no priority queue |

## Before you submit

- data/walls.txt, data/slides.txt and output/maze.png are in the repository.
- `java tools/Check.java` shows all examples right.
- This README is filled in, the program output and the section AI use included.
- The work is pushed to main (at least one commit per mission) and the repository is public.
- The link is attached in Moodle.
