package com.example.caretakerapp;

//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import org.webrtc.*;

public class ViewPatientActivity extends AppCompatActivity {

//    private DatabaseReference callRef;
//    private SharedPreferences sharedPreferences;
//    private String caretakerEmail, patientEmail;
//    private PeerConnection peerConnection;
//    private PeerConnectionFactory peerConnectionFactory;
//    private VideoTrack videoTrack;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_view_patient);
//
//        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        caretakerEmail = sharedPreferences.getString("user_email", "").replace(".", "_");
//        patientEmail = sharedPreferences.getString("patient_email", null);
//        callRef = FirebaseDatabase.getInstance().getReference("calls").child(patientEmail);
//
//        setupWebRTC();
//        findViewById(R.id.callButton).setOnClickListener(v -> initiateCall());
//    }
//
//    private void setupWebRTC() {
//        PeerConnectionFactory.InitializationOptions options =
//                PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
//        PeerConnectionFactory.initialize(options);
//
//        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
//        VideoCapturer capturer = createCameraCapturer();
//        VideoSource videoSource = peerConnectionFactory.createVideoSource(capturer.isScreencast());
//        capturer.startCapture(1280, 720, 30);
//
//        videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
//    }
//
//    private void initiateCall() {
//        callRef.child("caller").setValue(caretakerEmail);
//    }
//
//    private String getPatientEmail() {
//        return sharedPreferences.getString("linked_patient_email", "").replace(".", "_");
//    }
//
//    private VideoCapturer createCameraCapturer() {
//        return Camera2Enumerator.isSupported(this) ?
//                new Camera2Enumerator(this).createCapturer(Camera2Enumerator.getCameraNames()[0], null) :
//                new Camera1Enumerator(false).createCapturer(Camera1Enumerator.getDeviceNames()[0], null);
//    }
}
