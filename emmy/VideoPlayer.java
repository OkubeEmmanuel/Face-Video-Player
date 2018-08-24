package emmy;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VideoPlayer extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		MediaPane pane=new MediaPane(stage);
		Scene scene=new Scene(pane);
		stage.setScene(scene);
		stage.setTitle("VideoPlayer");
		stage.show();
	}
	
	public static void main(String[] args){
		Application.launch(args);
	}

}