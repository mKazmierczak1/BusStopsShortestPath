import com.opencsv.CSVReader;
import graph.BusConnectionsGraph;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import model.BusStop;
import model.Connection;

public class GraphProvider {

  private static BusConnectionsGraph graph = null;

  public static BusConnectionsGraph getGraph() throws Exception {
    if (graph == null) {
      graph = fillGraph();
    }

    return graph;
  }

  private static BusConnectionsGraph fillGraph() throws Exception {
    var data = readAllDataFromCSV();
    var graph = new BusConnectionsGraph(new HashMap<>(), new HashMap<>(), new HashMap<>());

    data.forEach(
        row -> {
          var start = new BusStop(row[5], Double.parseDouble(row[7]), Double.parseDouble(row[8]));
          var end = new BusStop(row[6], Double.parseDouble(row[9]), Double.parseDouble(row[10]));
          var connection = new Connection(row[2], getTime(row[3]), getTime(row[4]), start, end);

          graph.addNode(start, end);
          graph.addEdge(connection);
        });

    return graph;
  }

  private static List<String[]> readAllDataFromCSV() throws Exception {
    var data =
        readAllLinesFromCSV(
            Paths.get(ClassLoader.getSystemResource("connection_graph.csv").toURI()));

    return data.subList(1, data.size());
  }

  private static List<String[]> readAllLinesFromCSV(Path filePath) throws Exception {
    try (var reader = Files.newBufferedReader(filePath)) {
      try (var csvReader = new CSVReader(reader)) {
        return csvReader.readAll();
      }
    }
  }

  private static LocalTime getTime(String time) {
    var splitHour = time.split(":");
    var hour = Integer.parseInt(splitHour[0]);

    if (hour < 24) {
      return LocalTime.parse(time);
    } else {
      splitHour[0] = "0" + (hour % 24);
      return LocalTime.parse(String.join(":", splitHour));
    }
  }

  private GraphProvider() {}
}
