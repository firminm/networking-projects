// A Java program for Dijkstra's single source shortest path algorithm.
// The program is for adjacency matrix representation of the graph
import java.util.*;
import java.lang.*;
import java.io.*;
 
class ShortestPath
{
    // A utility function to find the vertex with minimum distance value,
    // from the set of vertices not yet included in shortest path tree
    public static int V;
    
    public static final int INFINITY = 9999;
    
    int minDistance(int dist[][], Boolean sptSet[])
    {
        // Initialize min value
        int min = INFINITY, min_index=-1;
 
        //FILL IN THE CODE HERE
 
        return min_index;
    }

    // A utility function to print the constructed distance array
    void printSolution(int dist[][])
    {
        System.out.println("Vertex Distance from Source");
        for (int i = 0; i < V; i++)
            System.out.println("Dest:"+i+" Cost:"+dist[i][0]+" Path:"+dist[i][1]);
    }

    // Function that implements Dijkstra's single source shortest path
    // algorithm for a graph represented using adjacency matrix
    // representation
    int[][] dijkstra(int graph[][], int src)
    {
        int output[][] = new int[4][2];
        
        // FILL IN THE CODE HERE
        
        return output;

    }
 
    // Driver method - example of object creation
    public static void main (String[] args)
    {
    	int graph[][]= new int[][]{{0, 1, 1, 9999},
            					   {1, 0,10, 7},
            					   {1,10, 0, 2},
            					   {9999, 7, 2, 0},
    							   };
    							   
        ShortestPath t = new ShortestPath();
        t.dijkstra(graph, 3);
    }
}
