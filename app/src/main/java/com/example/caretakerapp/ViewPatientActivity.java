package com.example.caretakerapp;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;




public class ViewPatientActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private String myAppId = "e96507b0abbc41969519575c26fa5814";
    private String channelName = "channel2";
    private String token = "007eJxTYPC3y/m+U0iDTe2JnvSPJyeffQpcrrX5UJlA+wPmwzdu6MoqMKRampkamCcZJCYlJZsYWppZmhpampqbJhuZpSWaWhiaPLP4kN4QyMhg1PufhZEBAkF8DobkjMS8vNQcIwYGAOdPIWs=";
    private RtcEngine mRtcEngine;
    private DatabaseReference videocallsRef;
    private String pushedKey;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        // Callback when successfully joining the channel
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            showToast("Joined channel " + channel);
        }
        // Callback when a remote user or host joins the current channel
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(() -> {
                // When a remote user joins the channel, display the remote video stream for the specified uid
                setupRemoteVideo(uid);
                showToast("User joined: " + uid); // Show toast for user joining
            });
        }
        // Callback when a remote user or host leaves the current channel
        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> {
                showToast("User offline: " + uid); // Show toast for user going offline
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);
        videocallsRef = FirebaseDatabase.getInstance().getReference("videocalls");

        // Get the pushedKey from DashboardActivity (if passed via Intent)
        pushedKey = getIntent().getStringExtra("pushedKey");
        // Set up the "End Call" button
        FloatingActionButton btnEndCall = findViewById(R.id.btnEndCall);
        btnEndCall.setOnClickListener(v -> endVideoCall());
        if (checkPermissions()) {
            startVideoCalling();
        } else {
            requestPermissions();
        }
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
    }
    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID && checkPermissions()) {
            startVideoCalling();
        }
    }
    private void startVideoCalling() {
        initializeAgoraVideoSDK();
        enableVideo();
        setupLocalVideo();
        joinChannel();
    }
    private void initializeAgoraVideoSDK() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = myAppId;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing RTC engine: " + e.getMessage());
        }
    }
    private void enableVideo() {
        mRtcEngine.enableVideo();
        mRtcEngine.startPreview();
    }
    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
    }
    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        options.publishCameraTrack = true;
        options.publishMicrophoneTrack = true;
        mRtcEngine.joinChannel(token, channelName, 0, options);
    }
    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupAgoraEngine();
    }
    private void cleanupAgoraEngine() {
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
            mRtcEngine = null;
        }
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(ViewPatientActivity.this, message, Toast.LENGTH_SHORT).show());
    }
    private void endVideoCall() {


        // 2. Delete the patient email from Firebase
        if (pushedKey != null) {
            videocallsRef.child(pushedKey).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to end call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            finish(); // Close even if no key (fallback)
        }
    }

}