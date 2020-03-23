package edge;

import config.CLIConstants;

public class EdgeApp {

    public static void main(String[] args) throws Exception {
        boolean logResults = false;
        boolean websocket = false;
        if (args.length >= 1) {
            if (CLIConstants.LOG_RESULTS.equals(args[0])) {
                logResults = true;
            }
        }

        if (args.length >= 2) {
            if (CLIConstants.WEBSOCKET.equals(args[1])) {
                websocket = true;
            }
        }

        if (websocket) {
            EdgeWebSocket edge = new EdgeWebSocket(logResults);
            edge.run();
        } else {
            Edge edge = new Edge(logResults);
            edge.run();
        }
    }
}
