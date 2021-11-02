package com.example.help.util;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseStorageHelper {
    private static final String TAG = "FirebaseStorageHelper";

    public void storeFile(Uri uri, StorageListener storageListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(userId + "/" + uri.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return fileRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    storageListener.onSuccess(downloadUri.toString());
                } else {
                    storageListener.onFailure();
                }
            }
        });

    }

    public interface StorageListener {
        void onSuccess(String filePath);
        void onFailure();
    }
}
