package com.example.help.ui.chatRoom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.help.R;
import com.example.help.databinding.ActivityChatBinding;
import com.example.help.models.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int REQUEST_IMAGE = 2; // No idea what the hell is this magic number, maybe in the doc somewhere ----Caleb
    private static String MESSAGES_CHILD = "/emergency_event/"; //it is the 'topic' that we subscribe in the real-time db
    private ActivityChatBinding mBinding;
    private String imageUri = null;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"; //TODO: bad practice, will need to be removed (not urgent) -- Caleb


    // Firebase instance variables
    public DatabaseReference mFirebaseDatabaseReference;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        // fetch data
        // New child entries
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
        DatabaseReference finalMessagesRef = messagesRef;
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(getUserName())) {
                    // run some code
                    Log.d(TAG, "onDataChange: The room exists, proceed to the room.");
                }
                else{
                    Log.d(TAG, "onDataChange: The room is not found, creating...");
                    Message initMessage = new Message(
                        "TODO: GPS location here",/* TODO: GPS location as message (or maybe some other form of data?)*/
                        getUserName(),
                        getUserPhotoUrl(),
                        null /* TODO: we can include image URL(camera), and voice URL(audio)*/
                );
                    // Create a child reference and set the user's message at that location
                    finalMessagesRef.child(getUserName())
                            .push().setValue(initMessage);
                    Log.d(TAG, "onDataChange: room created");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Something wrong?");
            }
        });
        messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD+getUserName());
        // Configure the options required for FirebaseRecyclerAdapter with the above Query reference
        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(messagesRef, Message.class)
                // Listen to the changes in the Query and automatically update to the UI
                .setLifecycleOwner(this)
                .build();

        // Construct the FirebaseRecyclerAdapter with the options set
        FirebaseRecyclerAdapter<Message, MessageHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(options) {
            @NonNull
            @Override
            public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MessageHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message, parent, false)
                );
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message message) {
                mBinding.progressBar.setVisibility(ProgressBar.INVISIBLE);
                holder.bindMessage(message);
            }
        };

        // Initialize LinearLayoutManager and RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mBinding.messageRecyclerView.setLayoutManager(linearLayoutManager);
        mBinding.messageRecyclerView.setAdapter(firebaseRecyclerAdapter);
//        mBinding.messageRecyclerView.addItemDecoration(new VerticalListItemSpacingDecoration(
//                getResources().getDimensionPixelSize(R.dimen.main_item_list_spacing),
//                getResources().getDimensionPixelSize(R.dimen.main_item_parent_spacing)
//        ));

        // Register an observer for watching changes in the Adapter data in order to scroll
        // to the bottom of the list when the user is at the bottom of the list
        // in order to show newly added messages
        firebaseRecyclerAdapter.registerAdapterDataObserver(
                new ScrollToBottomObserver(
                        mBinding.messageRecyclerView,
                        firebaseRecyclerAdapter,
                        linearLayoutManager
                )
        );

        // Disable the send button when there is no text in this input message field
        mBinding.chatEdit.addTextChangedListener(new ButtonObserver(mBinding.send));

        // Register a click listener on the Send Button to send messages on click
        mBinding.send.setOnClickListener(view -> {
            Message friendlyMessage = new Message(
                    getMessageToSend(),
                    getUserName(),
                    getUserPhotoUrl(),
                    imageUri
            );

            // Create a child reference and set the user's message at that location
            FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                    .push().setValue(friendlyMessage);
            // Clear the input message field for the next message
            mBinding.chatEdit.setText("");
        });
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) result -> {
                    // Get the URI to the image file selected
                    final Uri imageUri = result;

                    // Construct a message with temporary loading image
                    final Message tempMessage = new Message(
                            getMessageToSend(),  // If user has entered some message, publish it as well
                            getUserName(),
                            getUserPhotoUrl(),
                            LOADING_IMAGE_URL  // Temporary image with loading indicator
                    );
                    Log.d(TAG, "onCreate: image uploading");
                    mBinding.chatEdit.setEnabled(false);
                    // Create a child reference and set the user's message at that location
                    FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD)
                            .push().setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                /**
                         * This method will be triggered when the operation has either succeeded or failed. If it has
                         * failed, an error will be given. If it has succeeded, the error will be null
                         *
                         * @param error A description of any errors that occurred or null on success
                         * @param ref A reference to the specified Firebase Database location
                         */
                        @Override
                        public void onComplete(
                                @androidx.annotation.Nullable DatabaseError error,
                                @NonNull DatabaseReference ref) {
                            // Check the error
                            if (error != null) {
                                // Log the error and return
                                Log.w(TAG,
                                        "Unable to write message to the database.",
                                        error.toException()
                                );
                                return;
                            }

                            // Get the key to this database reference
                            String databaseKey = ref.getKey();

                            // Create a StorageReference for the Image to be uploaded
                            // in the hierarchy of the database key reference
                            //noinspection ConstantConditions
                            StorageReference storageReference = FirebaseStorage.getInstance()
                                    // Create a child location for the current user
                                    .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    // Create a child location for the database key
                                    .child(databaseKey)
                                    // Create a child with the filename
                                    .child(imageUri.getLastPathSegment());

                            // Begin upload of selected image
                            putImageInStorage(storageReference, imageUri, databaseKey, tempMessage);

                            // Clear the input message field if any for the next message
                            mBinding.chatEdit.setEnabled(true);
                            Log.d(TAG, "onComplete: edit enabled");
                            mBinding.chatEdit.setText("");
                        }
                    });
                });
        // Register a click listener on the Add Image Button to send messages with Image on click
        mBinding.image.setOnClickListener(view -> {
            // Launch Gallery Intent for Image selection
            Log.d(TAG, "onCreate: Image button triggered");
            mGetContent.launch("image/*");

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private String getUserName(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getDisplayName()==null?"ANONYMOUS":user.getDisplayName();
    }
    /**
     * Extracts the user typed message from 'R.id.messageEditText' EditText and returns the same.
     * Can be an empty string when there is no message typed in.
     */
    private String getMessageToSend() {
        if (mBinding.chatEdit.getText() == null) {
            return "";
        } else {
            return mBinding.chatEdit.getText().toString();
        }
    }

    /**
     * Returns the URL to the User's profile picture as stored in Firebase Project's user database.
     * Can be {@code null} when not present or if user is not authenticated.
     */
    @Nullable
    private String getUserPhotoUrl() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }
        return null;
    }
    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it. The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            // If the request was for Image selection

            if (resultCode == RESULT_OK && data != null) {
                // If we have the result and its data

                // Get the URI to the image file selected
                final Uri imageUri = data.getData();

                // Construct a message with temporary loading image
                final Message tempMessage = new Message(
                        getMessageToSend(),  // If user has entered some message, publish it as well
                        getUserName(),
                        getUserPhotoUrl(),
                        ""  // Temporary image with loading indicator
                );

                // Create a child reference and set the user's message at that location
                FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                        .push().setValue(tempMessage, new DatabaseReference.CompletionListener() {
                    /**
                     * This method will be triggered when the operation has either succeeded or failed. If it has
                     * failed, an error will be given. If it has succeeded, the error will be null
                     *
                     * @param error A description of any errors that occurred or null on success
                     * @param ref A reference to the specified Firebase Database location
                     */
                    @Override
                    public void onComplete(
                            @Nullable DatabaseError error,
                            @NonNull DatabaseReference ref) {
                        // Check the error
                        if (error != null) {
                            // Log the error and return
                            Log.w(TAG,
                                    "Unable to write message to the database.",
                                    error.toException()
                            );
                            return;
                        }

                        // Get the key to this database reference
                        String databaseKey = ref.getKey();

                        // Create a StorageReference for the Image to be uploaded
                        // in the hierarchy of the database key reference
                        //noinspection ConstantConditions
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                // Create a child location for the current user
                                .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                // Create a child location for the database key
                                .child(databaseKey)
                                // Create a child with the filename
                                .child(imageUri.getLastPathSegment());

                        // Begin upload of selected image
                        putImageInStorage(storageReference, imageUri, databaseKey, tempMessage);

                        // Clear the input message field if any for the next message
                        mBinding.chatEdit.setText("");
                    }
                });

            }
        }
    }

    /**
     * Uploads the Image {@code imageUri} selected by the user to the {@code storageReference} pointed
     * to by the {@code databaseKey}, retrieves the URI to this uploaded file, and then
     * updates the same to the corresponding Firebase database reference identified by
     * the {@code databaseKey}, to reflect the URI of the uploaded image,
     * which then displays the uploaded image to the user.
     *
     * @param tempMessage Temporarily prepared {@link Message} instance
     *                    whose {@link Message#getImageUrl()} property will be
     *                    updated to the URI of the uploaded image.
     */
    private void putImageInStorage(final StorageReference storageReference,
                                   final Uri imageUri,
                                   final String databaseKey,
                                   final Message tempMessage) {
        // Upload the selected image
        UploadTask uploadTask = storageReference.putFile(imageUri);

        // Chain UploadTask to get the resulting URI Task of the uploaded image
        uploadTask.continueWithTask(task -> {
            // Return the resulting URI Task of the uploaded image
            //noinspection ConstantConditions
            return task.getResult().getStorage().getDownloadUrl();
        }).addOnSuccessListener(this, uri -> {
            // When all tasks have completed successfully, update the corresponding reference
            // in the database with the URI of the uploaded image
            tempMessage.setImageUrl(uri.toString());
            FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                    .child(databaseKey)
                    .setValue(tempMessage);
        }).addOnFailureListener(this, e -> {
            // Log the exception in case of failure
            Log.w(TAG, "Image upload task was not successful.", e);
        });
    }
}