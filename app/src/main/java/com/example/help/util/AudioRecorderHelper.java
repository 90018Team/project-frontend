package com.example.help.util;

import android.media.AudioRecord;
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

    public void record(AudioRecordListener callback) {
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

                    callback.onFinishRecord(Uri.fromFile(audioFile));
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

    public interface AudioRecordListener {
        void onFinishRecord(Uri uri);
    }

}
