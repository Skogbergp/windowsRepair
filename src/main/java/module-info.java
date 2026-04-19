module org.example.windowsrepair {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.windowsrepair to javafx.fxml;
    exports org.example.windowsrepair;
}