/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] out = Graphs.slidesFrom(s);
        int[][] in = Graphs.slidesInto(s);

        // pass 1: DFS on the slides, remember the cells in the order they are finished
        boolean[] seen = new boolean[n];
        int[] order = new int[n];
        int orderSize = 0;
        int[] stack = new int[n];
        int[] next = new int[n];   // next[c] = how many slides of c are already looked at
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            int top = 0;
            stack[top++] = root;
            seen[root] = true;
            while (top > 0) {
                int c = stack[top - 1];
                if (next[c] < out[c].length) {
                    int to = s.to()[out[c][next[c]++]];
                    if (!seen[to]) { seen[to] = true; stack[top++] = to; }
                } else {
                    order[orderSize++] = c;   // finished
                    top--;
                }
            }
        }

        // pass 2: from the last finished cell to the first, DFS on the reversed slides.
        // Everything reached from a cell without a zone is one zone.
        int[] zoneOf = new int[n];
        java.util.Arrays.fill(zoneOf, -1);
        int zones = 0;
        for (int i = n - 1; i >= 0; i--) {
            int root = order[i];
            if (zoneOf[root] != -1) continue;
            int top = 0;
            stack[top++] = root;
            zoneOf[root] = zones;
            while (top > 0) {
                int c = stack[--top];
                for (int slide : in[c]) {
                    int from = s.from()[slide];
                    if (zoneOf[from] == -1) { zoneOf[from] = zones; stack[top++] = from; }
                }
            }
            zones++;
        }
        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        int m = s.count();
        // group the slides by the zone they start in (counting sort), so we can walk zone 0, 1, 2, ...
        int[] first = new int[zoneCount + 1];
        for (int i = 0; i < m; i++) first[zoneOf[s.from()[i]] + 1]++;
        for (int z = 0; z < zoneCount; z++) first[z + 1] += first[z];
        int[] fill = java.util.Arrays.copyOf(first, zoneCount);
        int[] byZone = new int[m];
        for (int i = 0; i < m; i++) byZone[fill[zoneOf[s.from()[i]]]++] = i;

        final long INF = Long.MAX_VALUE / 4;
        long[] stages = new long[zoneCount];   // stages[z] = zones on the longest chain that ends in z
        long[] cost = new long[zoneCount];     // cost[z] = cheapest route from the zone of the start to z
        java.util.Arrays.fill(stages, 1);
        java.util.Arrays.fill(cost, INF);
        int startZone = zoneOf[0], exitZone = zoneOf[s.n() - 1];
        cost[startZone] = 0;

        long longest = 1;
        // every slide between two zones goes from a smaller number to a larger one,
        // so when we reach zone z all the ways into z are already counted
        for (int z = 0; z < zoneCount; z++) {
            longest = Math.max(longest, stages[z]);
            for (int p = first[z]; p < first[z + 1]; p++) {
                int i = byZone[p];
                int to = zoneOf[s.to()[i]];
                if (to == z) continue;   // a slide inside a zone is free and changes nothing
                stages[to] = Math.max(stages[to], stages[z] + 1);
                if (cost[z] < INF) cost[to] = Math.min(cost[to], cost[z] + s.time()[i]);
            }
        }
        return new long[]{longest, cost[exitZone]};
    }
}
