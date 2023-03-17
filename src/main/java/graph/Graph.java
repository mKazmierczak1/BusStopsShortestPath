package graph;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Graph<E> {

  private final List<Node> nodes;
  private final List<Edge> edges;

  private Graph() {
    nodes = new ArrayList<>();
    edges = new ArrayList<>();
  }

  @SafeVarargs
  public final void addNode(E... values) {
    Arrays.stream(values)
        .forEach(
            value -> {
              if (!containsNode(value)) {
                nodes.add(new Node(value));
              }
            });
  }

  public boolean removeNode(E value) {
    if (containsNode(value)) {
      int index = indexOfNode(value);

      for (Node n : nodes) {
        removeEdge(value, n.value);
        removeEdge(n.value, value);
      }

      nodes.remove(index);
      return true;
    } else return false;
  }

  public boolean addEdge(E leftValue, E rightValue, long weight) {
    if (containsNode(leftValue) && containsNode(rightValue)) {
      edges.add(
          new Edge(nodes.get(indexOfNode(leftValue)), nodes.get(indexOfNode(rightValue)), weight));

      return true;
    } else return false;
  }

  public boolean removeEdge(E leftValue, E rightValue) {
    if (indexOfEdge(leftValue, rightValue) != -1) {
      edges.remove(indexOfEdge(leftValue, rightValue));
      return true;
    } else return false;
  }

  public void showGraph() {
    for (Node node : nodes) {
      System.out.print(node + ": ");
      findAllEdges(node);
      System.out.println();
    }
  }

  public Collection<E> Dijkstra(E start, E end) {
    if (containsNode(start) && containsNode(end)) {
      List<E> path = new LinkedList<>();
      Node v;
      Queue<Node> Q = new LinkedList<>();
      Long[] d = new Long[nodes.size()];
      int[] p = new int[nodes.size()];

      for (int i = 0; i < nodes.size(); i++) {
        d[i] = Long.MAX_VALUE;
        p[i] = -1;
      }

      d[indexOfNode(start)] = 0L;
      Q.add(nodes.get(indexOfNode(start)));

      while (!Q.isEmpty()) {
        v = Q.poll();

        for (Node n : nodes) {
          if (indexOfEdge(v.value, n.value) != -1
              && d[indexOfNode(n.value)]
                  > edges.get(indexOfEdge(v.value, n.value)).weight + d[indexOfNode(v.value)]) {
            d[indexOfNode(n.value)] =
                edges.get(indexOfEdge(v.value, n.value)).weight + d[indexOfNode(v.value)];
            p[indexOfNode(n.value)] = indexOfNode(v.value);
          }
        }

        for (int i = 0; i < nodes.size(); i++) {
          if (p[i] == nodes.indexOf(v)) Q.add(nodes.get(i));
        }
      }

      v = nodes.get(indexOfNode(end));
      path.add(v.value);

      while (v != nodes.get(indexOfNode(start))) {
        int pIndex = p[indexOfNode(v.value)];

        if (pIndex != -1) path.add(nodes.get(pIndex).value);
        else return null;

        v = nodes.get(pIndex);
      }

      Collections.reverse(path);

      return path;
    }

    return null;
  }

  public E getRandomNode() {
    Random rand = new Random();

    return nodes.get(rand.nextInt(nodes.size())).value;
  }

  private boolean containsNode(E value) {
    for (Node n : nodes) {
      if (n.value.equals(value)) return true;
    }

    return false;
  }

  private int indexOfNode(E value) {
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i).value.equals(value)) return i;
    }

    return -1;
  }

  private int indexOfEdge(E leftValue, E rightValue) {
    for (int i = 0; i < edges.size(); i++) {
      if (edges.get(i).left.value.equals(leftValue) && edges.get(i).right.value.equals(rightValue))
        return i;
    }

    return -1;
  }

  private void findAllEdges(Node n) {
    for (Edge e : edges) {
      if (e.left.equals(n)) System.out.print(e.right.toString() + " ");
      else if (e.right.equals(n)) System.out.print(e.left.toString() + " ");
    }
  }

  @Value
  private class Node {
    E value;
  }

  @Value
  private class Edge implements Comparable<Edge> {
    Node left;
    Node right;
    long weight;

    @Override
    public int compareTo(Edge o) {
      return Long.compare(weight, o.weight);
    }
  }
}
