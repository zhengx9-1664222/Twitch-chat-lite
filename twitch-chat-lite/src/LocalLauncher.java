import chat.ClientApp;
import config.CLIConstants;
import edge.EdgeApp;
import pubsub.PubSubApp;
import room.RoomApp;

public class LocalLauncher {

    public static void main(String[] args) {
        if (args.length >= 1 && CLIConstants.WEB_SERVER.equals(args[0])) {
            launchWebServer(args);
        } else {
            launchCommandLine(args);
        }
    }

    public static void launchWebServer(String[] args) {
        new Thread(() -> runRoomApplication(args)).start();
        new Thread(() -> runPubSubApplication(args)).start();
        new Thread(() -> runEdgeApplication(new String[] {CLIConstants.LOG_RESULTS, CLIConstants.WEBSOCKET})).start();
    }

    public static void launchCommandLine(String[] args) {
        new Thread(() -> runRoomApplication(args)).start();
        new Thread(() -> runPubSubApplication(args)).start();
        new Thread(() -> runEdgeApplication(args)).start();

        ClientApp.main(args);
    }

    public static void runRoomApplication(String[] args) {
        try {
            RoomApp.main(args);
        } catch (Exception e) {
            // If exception is thrown, simply end execution
        }
    }

    public static void runPubSubApplication(String[] args) {
        try {
            PubSubApp.main(args);
        } catch (Exception e) {
            // If exception is thrown, simply end execution
        }
    }

    public static void runEdgeApplication(String[] args) {
        try {
            EdgeApp.main(args);
        } catch (Exception e) {
            // If exception is thrown, simply end execution
        }
    }
}
