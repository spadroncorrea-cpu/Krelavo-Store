package krelavo.store;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import krelavo.store.controller.LoginController;
import krelavo.store.controller.CatalogController;

public class Main extends Application {
    private Stage stage;
    public void start(Stage s) {
        stage=s; stage.setTitle("Krelavo Store"); stage.setMinWidth(960); stage.setMinHeight(820);
        showLogin(); stage.show();
    }
    public void showLogin() { setScene(new LoginController(this).createView()); }
    public void showCatalog() { setScene(new CatalogController(this).createView()); }
    private void setScene(javafx.scene.Parent root) {
        Scene scene=new Scene(root,1440,900);
        scene.getStylesheets().add(getClass().getResource("/krelavo/store/resources/css/styles.css").toExternalForm());
        stage.setScene(scene); stage.setWidth(1440); stage.setHeight(940); stage.centerOnScreen();
    }
    public static void main(String[] args) { launch(args); }
}
