
public class Main {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var start = graph.getRandomNode();
    var end = graph.getRandomNode();

    System.out.printf("Path from %s to %s\n", start.name(), end.name());
    System.out.println(graph.Dijkstra(start, end));
  }
}
