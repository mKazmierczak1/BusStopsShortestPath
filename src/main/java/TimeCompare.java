import com.opencsv.CSVReader;
import graph.GraphProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeCompare {

  private static final String FILE_NAME = "connections.csv";
  private static final int ITERATIONS_COUNT = 20;

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    var connections = getBusStopsNames(Paths.get(ClassLoader.getSystemResource(FILE_NAME).toURI()));
    var time = LocalTime.parse("13:00:00");
    var mode = "t";
    var totalAvg = 0;

    for (var connection : connections) {
      var start = graph.getNode(connection[0]);
      var end = graph.getNode(connection[1]);
      System.out.printf("Path from %s to %s\n", start.name(), end.name());

      var results = new ArrayList<Long>();

      for (int i = 0; i < ITERATIONS_COUNT; i++) {
        var currentTime = Instant.now();

        switch (mode) {
          case "t" -> graph.findShortestPathTimeCriteria(start, end, time, 10D);
          case "p" -> graph.findShortestPathBusChangeCriteria(start, end, time, 1D);
          case "d" -> graph.findShortestPathDijkstra(start, end, time);
        }

        var duration = Duration.between(currentTime, Instant.now()).toMillis();
        results.add(duration);
        System.out.println("Duration: " + duration + " ms");
      }

      var avg = 0L;
      var falseResultsCount = 0;

      for (var result : results) {
        avg += result;

        if (result == 0) {
          falseResultsCount++;
        }
      }

      if (falseResultsCount == ITERATIONS_COUNT) {
        continue;
      }

      totalAvg += avg / (ITERATIONS_COUNT - falseResultsCount);
      System.out.println(
          "Average duration: " + avg / (ITERATIONS_COUNT - falseResultsCount) + " ms");
    }

    System.out.println("Total average duration: " + totalAvg / connections.size() + " ms");
  }

  private static List<String[]> getBusStopsNames(Path filePath) throws Exception {
    try (var reader = Files.newBufferedReader(filePath)) {
      try (var csvReader = new CSVReader(reader)) {
        return csvReader.readAll();
      }
    }
  }
}
