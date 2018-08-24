package emmy;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.*;
import org.opencv.objdetect.CascadeClassifier;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MediaPane extends BorderPane {
	
	private Media media;
	static private MediaPlayer mediaPlayer;
	private MediaView mediaView;
	private Button endButton;
	private Button foreButton;
	private Button backButton;
	private Button openButton;
	private Slider slVolume;
	private HBox hBox;
	private Slider slProcess;
	private VBox vBox;
	private FileChooser fileChooser;
	private File mediaFile;
	private String MEDIA_URL;
	static private boolean bCanPlay=false;
	private javafx.stage.Stage theStage;
	
	Imgcodecs imageCodecs = new Imgcodecs();
	
	static boolean faceSeen = false;
    
    Imgproc imageProc = new Imgproc();
    
    // a timer for acquiring the video stream
 	public static ScheduledExecutorService timer;
 	
 	public static void pausePlay(boolean cs) {
 		if(faceSeen != cs) {
 			faceSeen = cs;
 			if(cs) {
 				bCanPlay = true;
 				mediaPlayer.play();
 			} 				
 			else {
 				bCanPlay = false;
 				mediaPlayer.pause();
 			}
 		}
 	}
 		
	public MediaPane(javafx.stage.Stage stage){
		
		VideoCapture capture =  new VideoCapture();
        CascadeClassifier faceDetector = new CascadeClassifier(MediaPane.class.getResource("haarcascade_frontalface_alt.xml").getPath());
        
        // start the video capture
     	capture.open(0);
	 	
		theStage = stage;
		setPrefSize(800, 600);
		setStyle("-fx-background-color:black");
		fileChooser=new FileChooser();
		fileChooser.setTitle("select file");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP4","*.mp4"),
				new FileChooser.ExtensionFilter("MP3", "*.mp3"));
		mediaView=new MediaView();
		
		endButton=new Button("stop");
		backButton=new Button("RW");
		foreButton=new Button("FW");
		openButton=new Button("OPEN");
		
		
		endButton.setOnAction(e->{
			if(bCanPlay){
				mediaPlayer.stop();
			}
		});
		
		backButton.setOnAction(e->{
			if(bCanPlay){
				mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(new Duration(10000)));
			}
		});
		
		foreButton.setOnAction(e->{
			if(bCanPlay){
				mediaPlayer.seek(mediaPlayer.getCurrentTime().add(new Duration(10000)));
			}
		});
		
		openButton.setOnAction(e->{
			if(bCanPlay){
				try {
					fileChooser.setInitialDirectory(mediaFile.getParentFile());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
			mediaFile=fileChooser.showOpenDialog(theStage);
			
			if(mediaFile != null){	
				MEDIA_URL = mediaFile.getAbsolutePath();
				if(MEDIA_URL.endsWith(".mp4") || MEDIA_URL.endsWith(".aiff") || 
						MEDIA_URL.endsWith(".mp3") || MEDIA_URL.endsWith(".wav")){
					
					theStage.setTitle(MEDIA_URL);
					
					MEDIA_URL = MEDIA_URL.replace('\\', '/');
					
					if(bCanPlay){
						mediaPlayer.stop();
					}
					
					media = new Media("file:///"+MEDIA_URL);
					mediaPlayer = new MediaPlayer(media);
					mediaPlayer.volumeProperty().bind(slVolume.valueProperty().divide(100));
					mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {		
						@Override
						public void invalidated(Observable observable) {
							// TODO Auto-generated method stub
								slProcess.setValue(mediaPlayer.getCurrentTime().toMillis()/media.getDuration().toMillis()*2000);
						}	
					});
					mediaView.setMediaPlayer(mediaPlayer);
					
					if (capture.isOpened()) {
			        	
			        	Runnable frameGrabber = new Runnable() { 
			            	@Override
			            	public void run() {
			            		
			            		Mat frame = new Mat();
			               	 	capture.read(frame);
			               	 	
				               	MatOfRect faceDetections = new MatOfRect();
				                faceDetector.detectMultiScale(frame, faceDetections);
				                pausePlay(faceDetections.toArray().length > 0);
			            	}
			            };
			            
			            timer = Executors.newSingleThreadScheduledExecutor();
						timer.scheduleAtFixedRate(frameGrabber, 0, 1, TimeUnit.MILLISECONDS);
			                   	
			        }
			        else {
			        	System.out.println("Another service is using the camera, stop that service first.");
			        }
				}
			}
			
			
		});
		
		slVolume=new Slider();
		slVolume.setPrefWidth(150);
		slVolume.setMaxWidth(Region.USE_PREF_SIZE);
		slVolume.setMinWidth(30);
		slVolume.setValue(50);
		
		hBox=new HBox(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(endButton,backButton,foreButton,openButton,new Label("Volume"),slVolume);
		
		slProcess=new Slider();
		slProcess.setValue(0);
		slProcess.setMax(2000);
		
		slProcess.setOnMouseDragged(e->{
			if(bCanPlay){
				mediaPlayer.seek(new Duration(slProcess.getValue()/2000*media.getDuration().toMillis()));
			}
		});
		vBox=new VBox();
		vBox.getChildren().addAll(slProcess,hBox);
		vBox.setAlignment(Pos.CENTER);
		
		setCenter(mediaView);
		mediaView.fitWidthProperty().bind(widthProperty());
		setBottom(vBox);
	}
	
	public void setVideo(String url){
		MEDIA_URL=url;
	}
	
}
