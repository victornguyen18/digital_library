package entry_point;

import py4j.GatewayServer;

public class TestEntryPoint {

    public static void main(String[] args) {
        SearchEntryPoint application = new SearchEntryPoint();
        GatewayServer server = new GatewayServer(application);
        System.out.println("Open JavaGatewayServer: " + server.getPort());
        server.start();
    }
}
