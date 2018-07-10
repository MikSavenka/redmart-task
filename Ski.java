package com.task;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Ski {

    static int maxSize;
    static Map<Point, Way> ways = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Ski ski = new Ski();

        File file = download();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String size = reader.readLine();
        int sizeX = Integer.valueOf(size.split(" ")[0]);
        int sizeY = Integer.valueOf(size.split(" ")[1]);
        maxSize = sizeX - 1;
        int[][] map = new int[sizeX][sizeY];
        for (int i = 0; i < sizeX; i++) {
            String mapLine = reader.readLine();
            String[] mapParts = mapLine.split(" ");
            for (int j = 0; j < sizeX; j++) {
                map[i][j] = Integer.valueOf(mapParts[j]);
            }
        }

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                ski.findWay(map, new Point(i, j));
            }
        }
        Collection<Way> waySet = ways.values();
        Way max = (Way) waySet.stream().max(Comparator.<Way>naturalOrder()).get();
        System.out.println(max.length + "" + max.steep);
    }

    private static File download() {
        URL url;
        HttpURLConnection connection = null;
        File file = new File("downloaded.txt");
        try {
            url = new URL("http://s3-ap-southeast-1.amazonaws.com/geeks.redmart.com/coding-problems/map.txt");
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        try (InputStream in = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[512];
            while (true) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private void findWay(int[][] map, Point start) {
        List<Way> foundWays = new ArrayList<>();
        for (Point neighbour : getNeighbours(start)) {
            if (map[neighbour.x][neighbour.y] < map[start.x][start.y]) {
                Way way = new Way();
                way.first = start;
                way.length = 1;
                way.steep = map[start.x][start.y] - map[neighbour.x][neighbour.y];

                Point neighborPoint = new Point(neighbour.x, neighbour.y);
                Way existing = ways.get(neighborPoint);
                Way wayFound;
                if (existing != null) {
                    wayFound = merge(way, existing);
                } else {
                    wayFound = exploreWay(map, way, neighbour);
                }
                foundWays.add(wayFound);
            }
        }
        if (foundWays.size() > 0) {
            Collections.sort(foundWays);
            ways.put(start, foundWays.get(foundWays.size() - 1));
        }
    }

    private Way exploreWay(int[][] map, Way way, Point current) {
        List<Way> foundWays = new ArrayList<>();

        boolean continuing = false;
        Way comeWay = new Way(way);
        for (Point neighbour : getNeighbours(current)) {
            if (map[neighbour.x][neighbour.y] < map[current.x][current.y]) {
                Way exploringWay;
                if (!continuing) {
                    continuing = true;
                    exploringWay = way;
                } else {
                    exploringWay = new Way(comeWay);
                }
                exploringWay.length++;
                exploringWay.steep += map[current.x][current.y] - map[neighbour.x][neighbour.y];

                Point neighborPoint = new Point(neighbour.x, neighbour.y);
                Way existing = ways.get(neighborPoint);
                Way wayFound;
                if (existing != null) {
                    wayFound = merge(exploringWay, existing);
                } else {
                    wayFound = exploreWay(map, exploringWay, neighbour);
                }
                foundWays.add(wayFound);
            }
        }
        if (!continuing) {
            way.last = current;
        }
        if (foundWays.size() > 0) {
            Collections.sort(foundWays);
            return foundWays.get(foundWays.size() - 1);
        } else {
            return way;
        }
    }

    private Way merge(Way way, Way existing) {
        way.length += existing.length;
        way.steep += existing.steep;
        way.last = existing.last;
        return way;
    }

    private List<Point> getNeighbours(Point start) {
        List<Point> points = new ArrayList<>();
        if (start.x != 0) {
            points.add(new Point(start.x - 1, start.y));
        }
        if (start.x != 0 && start.y != 0) {
            points.add(new Point(start.x - 1, start.y - 1));
        }
        if (start.x != 0 && start.y != maxSize) {
            points.add(new Point(start.x - 1, start.y + 1));
        }
        if (start.y != 0) {
            points.add(new Point(start.x, start.y - 1));
        }
        if (start.y != maxSize) {
            points.add(new Point(start.x, start.y + 1));
        }
        if (start.x != maxSize) {
            points.add(new Point(start.x + 1, start.y));
        }
        if (start.x != maxSize && start.y != 0) {
            points.add(new Point(start.x + 1, start.y - 1));
        }
        if (start.x != maxSize && start.y != maxSize) {
            points.add(new Point(start.x + 1, start.y + 1));
        }

        return points;
    }

    private class Way implements Comparable {

        private Point first;
        private Point last;
        private Integer length;
        private Integer steep;

        public Way(Way way) {
            this.first = way.first;
            this.length = way.length;
            this.steep = way.steep;
        }

        public Way() {
        }

        @Override
        public int compareTo(Object o) {
            int compareLength = length.compareTo(((Way) o).length);
            return compareLength == 0
                    ? steep.compareTo(((Way) o).steep)
                    : compareLength;
        }
    }

    private static class Point {
        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (x != point.x) return false;
            if (y != point.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }
}
