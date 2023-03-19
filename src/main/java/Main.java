import java.time.LocalTime;

public class Main {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var start = graph.getNode("Wielka");
    var end = graph.getNode("ZOO");
    var time = LocalTime.parse("17:59:00");

    // graph.printDirect();
    System.out.printf("Path from %s to %s\n", start.name(), end.name());
    graph.printPath(graph.findShortestPathDijkstra(start, end, time), start, end, time);
  }
}
