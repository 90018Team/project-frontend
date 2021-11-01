package com.example.help.util;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorderHelper {
    private MediaRecorder mediaRecorder;
    private static final String TAG = "AudioRecorderHelper";
    private static final int MS_TO_RECORD = 5000;

    public AudioRecorderHelper() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    public void record(FileCallback callback) {
        File audioFile = createFile();
        String path = audioFile.getAbsolutePath();
        mediaRecorder.setOutputFile(path);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d(TAG, "record: start recording");


            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "record: stop recording");
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();

                    callback.onFinishRecord(audioFile);
                }
            }, MS_TO_RECORD); //<-- Execute code after 15000 ms i.e after 15 Seconds.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createFile() {

        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "HELP_voice_recordings");
        audioDir.mkdirs();
        String audioDirPath = audioDir.getAbsolutePath();

        return new File(audioDirPath + "/" + System.currentTimeMillis() + ".3pg");
    }

    // TODO: This method probably belongs in a FirebaseStorageHelper class
    public void storeFile(File file, FilePathCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Uri fileUri = Uri.fromFile(file);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(userId + "/" + fileUri.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(fileUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure: file upload failed");
                callback.onFailure();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "onSuccess: file upload succeeded");
                callback.onSuccess(taskSnapshot.getMetadata().getPath());
            }
        });
    }


    public interface FileCallback {
        void onFinishRecord(File file);
    }

    public interface FilePathCallback {
        void onSuccess(String filePath);
        void onFailure();
    }
}
