import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var scanner = new Scanner(System.in);

    System.out.print("Start: ");
    var start = graph.getNode(scanner.next());

    System.out.print("End: ");
    var end = graph.getNode(scanner.next());

    System.out.print("Time: ");
    var time = LocalTime.parse(scanner.next());

    System.out.print("Mode: ");
    var mode = scanner.next();

    System.out.printf("Path from %s to %s\n", start.name(), end.name());
    var currentTime = Instant.now();
    System.out.println("Start time: " + currentTime);

    var path =
        switch (mode) {
          case "t" -> graph.findShortestPathTimeCriteria(start, end, time, 100D);
          case "p" -> graph.findShortestPathBusChangeCriteria(start, end, time, 1D);
          case "d" -> graph.findShortestPathDijkstra(start, end, time);
          default -> null;
        };

    graph.getPath(path, start, end).forEach(System.out::println);
    System.out.println(
        "Duration: " + Duration.between(currentTime, Instant.now()).toMillis() + " ms");
  }
}
