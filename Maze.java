import java.util.*;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int size = 0;
        for (int k = 0; k < w.count(); k++) {
            if (dsu.union(w.a()[k], w.b()[k])) broken[size++] = k;
        }
        return Arrays.copyOf(broken, size);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        int m = w.count();
        // key = cost in the high 32 bits, wall index in the low 32 bits: sorting longs sorts by cost
        long[] keys = new long[m];
        for (int k = 0; k < m; k++) keys[k] = ((long) w.cost()[k] << 32) | k;
        Arrays.sort(keys);

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int size = 0;
        for (long key : keys) {
            int k = (int) key;   // low 32 bits
            if (dsu.union(w.a()[k], w.b()[k])) {
                broken[size++] = k;
                if (size == broken.length) break;   // the tree is ready
            }
        }
        return Arrays.copyOf(broken, size);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells(), m = w.count();
        if (n == 0) return 0;

        // adjacency lists in arrays: start[v] .. start[v+1] hold the walls touching cell v
        int[] start = new int[n + 1];
        for (int k = 0; k < m; k++) { start[w.a()[k] + 1]++; start[w.b()[k] + 1]++; }
        for (int v = 0; v < n; v++) start[v + 1] += start[v];
        int[] fill = Arrays.copyOf(start, n);
        int[] adj = new int[2 * m];
        for (int k = 0; k < m; k++) { adj[fill[w.a()[k]]++] = k; adj[fill[w.b()[k]]++] = k; }

        boolean[] inTree = new boolean[n];
        // queue entry = cost in the high 32 bits, the cell behind the wall in the low 32 bits
        PriorityQueue<Long> pq = new PriorityQueue<>();
        long total = 0;
        int taken = 1;
        inTree[0] = true;
        for (int i = start[0]; i < start[1]; i++) {
            int k = adj[i];
            int other = w.a()[k] == 0 ? w.b()[k] : w.a()[k];
            pq.add(((long) w.cost()[k] << 32) | other);
        }
        while (!pq.isEmpty() && taken < n) {
            long top = pq.poll();
            int v = (int) top;
            if (inTree[v]) continue;   // an old entry: this cell was already added by a cheaper wall
            inTree[v] = true;
            taken++;
            total += top >>> 32;
            for (int i = start[v]; i < start[v + 1]; i++) {
                int k = adj[i];
                int other = w.a()[k] == v ? w.b()[k] : w.a()[k];
                if (!inTree[other]) pq.add(((long) w.cost()[k] << 32) | other);
            }
        }
        return total;
    }
}
