package org.example.windowsrepair;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainView {

    private final VBox root;
    private final TextArea output;


    public MainView() {
        output = new TextArea();
        output.setEditable(false);
        output.setStyle("-fx-font-family: monospace;");
        VBox.setVgrow(output, Priority.ALWAYS);

        Label statusLabel = new Label("Ready");
        statusLabel.textProperty().bind(
                Bindings.when(CommandService.queueSize.greaterThan(0))
                        .then(CommandService.queueSize.asString().concat(" commands waiting..."))
                        .otherwise("Ready")
        );

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                buildRepairTab(),
                buildNetworkTab(),
                buildPerformanceTab(),
                buildInfoTab()
        );

        root = new VBox(10, tabs, output, statusLabel);
        root.setPadding(new Insets(10));
    }

    private Tab buildRepairTab() {
        FlowPane pane = buildPane();

        Button reregisterBtn = new Button("Re-register Apps");
        reregisterBtn.setPrefWidth(160);
        reregisterBtn.setTooltip(new Tooltip("Re-registers all Windows apps, fixes crashes like Settings not opening"));
        reregisterBtn.setOnAction(e -> {
            String path = getClass().getClassLoader().getResource("reregister-apps.ps1").getPath();
            CommandService.runPowershellScript(path, output);
        });

        pane.getChildren().addAll(
                buildButton("SFC Scan", "sfc /scannow", "Scans and repairs corrupted Windows system files"),
                buildButton("DISM Repair", "DISM /Online /Cleanup-Image /RestoreHealth", "Repairs the Windows image, heavier fix than SFC"),
                buildButton("Check Disk", "chkdsk C: /f /r", "Checks disk for errors and bad sectors, requires reboot"),
                reregisterBtn
        );
        return new Tab("Windows Repair", pane);
    }

    private Tab buildNetworkTab() {
        FlowPane pane = buildPane();
        pane.getChildren().addAll(
                buildButton("Flush DNS", "ipconfig /flushdns", "Clears DNS cache, fixes some website loading issues"),
                buildButton("Reset Winsock", "netsh winsock reset", "Resets network stack, fixes various connection issues"),
                buildButton("Reset TCP/IP", "netsh int ip reset", "Resets TCP/IP stack, fixes deeper network problems"),
                buildButton("Release IP", "ipconfig /release", "Releases your current IP address"),
                buildButton("Renew IP", "ipconfig /renew", "Requests a new IP address from the router"),
                buildButton("Network Info", "ipconfig /all", "Shows all network adapter information")
        );
        return new Tab("Network", pane);
    }

    private Tab buildPerformanceTab() {
        FlowPane pane = buildPane();
        pane.getChildren().addAll(
                buildButton("Disk Cleanup", "cleanmgr", "Opens Windows disk cleanup utility"),
                buildButton("Clear Temp Files", "del /q /f /s %TEMP%\\*", "Deletes all files in your temp folder"),
                buildButton("Stop Windows Update", "net stop wuauserv", "Stops the Windows Update service"),
                buildButton("Start Windows Update", "net start wuauserv", "Starts the Windows Update service")
        );
        return new Tab("Performance", pane);
    }

    private Tab buildInfoTab() {
        FlowPane pane = buildPane();
        pane.getChildren().addAll(
                buildButton("System Info", "systeminfo", "Shows detailed hardware and OS information"),
                buildButton("Windows Version", "winver", "Shows your current Windows version"),
                buildButton("Task List", "tasklist", "Lists all currently running processes"),
                buildButton("Driver List", "driverquery", "Lists all installed device drivers")
        );
        return new Tab("Info", pane);
    }

    private Button buildButton(String label, String command, String description) {
        Button btn = new Button(label);
        btn.setPrefWidth(160);
        btn.setTooltip(new Tooltip(description));
        btn.setOnAction(e -> CommandService.runCMDcommand(command, output));
        return btn;
    }

    private FlowPane buildPane() {
        FlowPane pane = new FlowPane(10, 10);
        pane.setPadding(new Insets(10));
        return pane;
    }

    public Node getRoot() {
        return root;
    }
}