package com.example.myapplication;


import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoWriter;


import java.io.File;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {
    CameraBridgeViewBase cameraBridgeViewBase;
    QRCodeDetector qrCodeDetector;
    Mat rgba;
    Mat gray;

    TextView logTextView;
    int Width;
    int Height;
    Button btnRecord;
    VideoWriter videoWriter;
    private static final int FRAME_RATE = 30;

    boolean isRecording=false;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getPermission();
        logTextView = findViewById(R.id.logTextView);
        cameraBridgeViewBase=findViewById(R.id.cameraview);



                cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                    @Override
                    public void onCameraViewStarted(int width, int height) {
                        rgba=new Mat();
                        gray=new Mat();
                        Height=height;
                        Width=width;









                    }

                    @Override
                    public void onCameraViewStopped() {
                        rgba.release();
                        gray.release();
                        //rects.release();


                    }

                    @Override
                    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                        rgba = inputFrame.rgba();
                        gray = inputFrame.gray();


                        if (rgba.empty() || gray.empty()) {
                            updateLog("Empty frames detected");
                            return rgba;
                        }

                        Mat points = new Mat();
                        String decodedText;

                        try {
                            decodedText = qrCodeDetector.detectAndDecode(gray, points);

                            if (decodedText.isEmpty()) {
                                //updateLog("No QR Code detected");

                            } else {
                                //updateLog("Decoded QR Code: " + decodedText);


                                if (!points.empty()) {

                                    double[] tempDouble;
                                    Point[] pts = new Point[4];
                                    for (int i = 0; i < 4; i++) {
                                        tempDouble = points.get(0, i);
                                        if (tempDouble != null) {
                                            pts[i] = new Point(tempDouble[0], tempDouble[1]);
                                        }
                                    }

                                    for (int i = 0; i < 4; i++) {
                                        Imgproc.line(rgba, pts[i], pts[(i + 1) % 4], new Scalar(0, 255, 0), 3);
                                    }
                                }

                            }
                        } catch (CvException e) {
                            updateLog("OpenCV error in QR code detection: " + e.getMessage());
                        } catch (Exception e) {
                            updateLog("General error in QR code detection: " + e.getMessage());
                        } finally {
                            if (!points.empty()) {
                                points.release();
                            }
                        }
                        if (isRecording) {
                            videoWriter.write(rgba);
                            updateLog("The video work");
                        }

                        return rgba;
                    }
                });


        btnRecord=findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    isRecording = true;
                    btnRecord.setText("Stop");

                    String fileName = "video_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".avi";
                    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                    File videoFile = new File(storageDir, fileName);

                    videoWriter = new VideoWriter(videoFile.getAbsolutePath(),
                            VideoWriter.fourcc('M', 'J', 'P', 'G'),
                            30,
                            new Size(Width, Height));

                    // Ensure you start videoWriter here
                } else {
                    // Stop recording
                    isRecording = false;
                    btnRecord.setText("Record");

                    if (videoWriter.isOpened()) {
                        videoWriter.release();
                    }
                    // Handle file saving or notification here
                }
            }
        });

        if(OpenCVLoader.initDebug()){
            cameraBridgeViewBase.enableView();
            qrCodeDetector=new QRCodeDetector();







        }

    }
    protected void onResume(){
        super.onResume();
        cameraBridgeViewBase.enableView();
    }
    private void updateLog(String message) {
        runOnUiThread(() -> {
            logTextView.append(message + "\n");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void getPermission(){
        requestPermissions(new String[]{Manifest.permission.CAMERA},101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&&grantResults[0]!= PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }



}