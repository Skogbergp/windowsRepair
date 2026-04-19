package org.example.windowsrepair;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

public class CommandService {

    private static final ThreadPoolExecutor queue = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
    );
    public static final IntegerProperty queueSize = new SimpleIntegerProperty(0);

    private static ProcessBuilder buildprocess(String arg1, String arg2 ,String command) {
        ProcessBuilder pb = new ProcessBuilder(arg1, arg2, command);
        pb.redirectErrorStream(true);
        return pb;

    }

    public static void runPowershellCommand(String command, TextArea output){
        runCommand(command, output, buildprocess("powershell.exe", "-Command",command));
    }

    public static void runPowershellScript(String scriptPath, TextArea output) {
        runCommand(scriptPath, output,
                new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath));
    }

    public static void runCMDcommand(String command, TextArea output){
        runCommand(command, output, buildprocess("cmd.exe", "/c",command));
    }
    public static void runCommand(String command, TextArea output,ProcessBuilder pb) {
        queueSize.set(queue.getQueue().size());
        output.clear();
        output.appendText("Command started, waiting for output...\n");
        queue.submit(() -> {
            Platform.runLater(() -> queueSize.set(queue.getQueue().size()));
            try {
                Process p = pb.start();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getInputStream())
                );
                StringBuilder batch = new StringBuilder();
                long lastFlush = System.currentTimeMillis();
                String line;
                while ((line = reader.readLine()) != null) {
                    batch.append(line.trim()).append("\n");
                    long now = System.currentTimeMillis();
                    if (batch.length() > 2000 || now - lastFlush > 700) {
                        String out = batch.toString();
                        Platform.runLater(() -> output.appendText(out));
                        batch.setLength(0);
                        lastFlush = now;
                    }
                }
                if (batch.length() > 0) {
                    String out = batch.toString();
                    Platform.runLater(() -> output.appendText(out));
                }
                p.waitFor();
                Platform.runLater(() -> queueSize.set(queue.getQueue().size()));
                Platform.runLater(() -> output.appendText("\nDone.\n"));
            } catch (Exception e) {
                Platform.runLater(() -> queueSize.set(queue.getQueue().size()));
                Platform.runLater(() -> output.appendText("Error: " + e.getMessage()));
            }
        });
    }
    public static void shutdown() {
        queue.shutdown();
    }
}
