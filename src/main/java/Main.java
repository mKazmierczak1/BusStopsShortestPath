import java.time.LocalTime;

public class Main {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var start = graph.getNode("KRZYKI");
    var end = graph.getNode("KROMERA");
    var time = LocalTime.parse("19:00:00");

    // graph.printDirect();
    System.out.printf("Path from %s to %s\n", start.name(), end.name());
    graph.printPath(graph.findShortestPath(start, end, time), start, end, time);
  }
}
