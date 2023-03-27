import com.opencsv.CSVWriter;
import graph.GraphProvider;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportConnections {

  public static void main(String[] args) throws Exception {
    var graph = GraphProvider.getGraph();
    List<String[]> connections = new ArrayList<>();
    var path = Paths.get(("src/main/resources/connections.csv"));

    for (int i = 0; i < 100; i++) {
      connections.add(new String[] {graph.getRandomBusStopName(), graph.getRandomBusStopName()});
      System.out.println(Arrays.toString(connections.get(i)));
    }

    writeAllLines(connections, path);
  }

  private static void writeAllLines(List<String[]> lines, Path path) throws IOException {
    try (CSVWriter writer =
        new CSVWriter(
            Files.newBufferedWriter(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE))) {
      lines.forEach(
          line -> {
            writer.writeNext(line);
            try {
              writer.flush();
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    }
  }
}
