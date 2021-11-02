package com.example.help.ui.chatRoom;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.help.MainActivity;
import com.example.help.R;
import com.example.help.databinding.ActivityChatBinding;
import com.example.help.models.Alert;
import com.example.help.models.Message;
import com.example.help.ui.home.GatherInfo;
import com.example.help.ui.signIn.SignInActivity;
import com.example.help.util.FirestoreUserHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

/**
 * @Title: Caleb's notes!! please put URLs in the initial message!
 *
 * @IMPORTANT: The initial message (or say automated message) is in line 100
 * or search messagesRef.addListenerForSingleValueEvent by ctrl+f
 *
 * @When: The initial message is sent when the room is created. It will become the first message in the room
 *
 * @Purpose:
 * 1. This can be the short description in the alarm tab (the tab that we can see available emergency events)
 * by reading the first message in every room in the realtime db.
 *
 * 2. This can be a part of the notification if we send SMSs in the client side or server side.
 * As both side can read the first message easily.
 *
 * @Usage: Simply get the initial message by query and use it as an object
 * The object definition is at models.Message
 * All data are String format.
 *
 * @Note: photoURL is not used currently, this is the one to set users avatar, a trivial data.
 * use null for photoURL, or call getUserPhotoUrl() here
 * */
public class ChatActivity extends AppCompatActivity{
    private static final String TAG = "ChatActivity";
    private static String MESSAGES_CHILD = "/emergency_event/"; //it is the 'topic' that we subscribe in the real-time db
    private ActivityChatBinding mBinding;
    private String imageUri = null;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private Boolean isVisitor = false;
    private FirestoreUserHelper userHelper;


    private  LinearLayout chatTop, chatBot;


    // Firebase instance variables
    public DatabaseReference mFirebaseDatabaseReference;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userHelper = FirestoreUserHelper.getInstance();

        /*
        * step 1: create binding, the ActivityChatBinding is auto generated by Android studio.
        *         Here, the binding comes from the defined activity, e.g., when Android studio see ChatActivity class, it will generate an identical
        *         binding called ActivityChatBinding. You can try to type different activity class name and see the prompt in the IDE.
        * */
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        /*
        * step 1.5: get intent and check current user is creator or visitor
        *           is is visitor then make some component invisible
        **/

        Intent i = getIntent();
        String from = i.getStringExtra("from");

        this.isVisitor = (from != null);

        if (isVisitor) {
            chatTop =  (LinearLayout) findViewById(R.id.chat_top);
            chatTop.setVisibility(View.GONE);
            chatBot =  (LinearLayout) findViewById(R.id.chat_bot);
            final float scale = this.getResources().getDisplayMetrics().density;
            int pixels = (int) (60 * scale + 0.5f);
            chatBot.getLayoutParams().height = pixels;
            chatBot.requestLayout();
        }
        /*
        * step 2: get firebase database reference (note this is realtime database)
        *         a. get a ref by accessing a child (as mentioned previously it works like a tree structure)
        *         b. addListenerForSingleValueEvent: add an optional listener as below to see if the "topic" exists (is the child node in the data available for me to use?)
        *         c. then, CURD can be triggered by the buttons/context change. See mBinding.<xml ids>.<listeners>() lines.
        *            putImageInStorage() is a method if you want to upload objects to the Firestore.
        *         d. Optional: below lines are more on the real-time updating (responsiveness) so that I have to add extra listeners in the layout managers.
        * */
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();



        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
        if (!isVisitor) {
            DatabaseReference finalMessagesRef = messagesRef;
            messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(getUserName())) {
                        Log.d(TAG, "onDataChange: The room exists, proceed to the room.");
                    }
                    else{
                        Log.d(TAG, "onDataChange: The room is not found, creating...");
                        Message initMessage = new Message(
                                "TODO: GPS location here",/* TODO: GPS location as text message (or maybe some other form of data?)*/
                                getPhoneNumber(),
                                getUserPhotoUrl(),
                                null,/*TODO: camera image file URL*/
                                /* TODO: voice file URL*/audioUrl
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

        }

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
        // Trivial decoration
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
                    imageUri,
                    null
            );

            // Create a child reference and set the user's message at that location
            FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                    .push().setValue(friendlyMessage);
            // Clear the input message field for the next message
            mBinding.chatEdit.setText("");
        });


        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    // Get the URI to the image file selected
                    ContentResolver cr = this.getContentResolver();
                    String mime = cr.getType(result);
                    Log.d(TAG, "onCreate: Received mime type "+mime);
                    if(mime.matches("image(.*)")){
                        Log.d(TAG, "onCreate: In image upload function");
                        final Uri imageUri = result;

                        // Construct a message with temporary loading image
                        final Message tempMessage = new Message(
                                getMessageToSend(),  // If user has entered some message, publish it as well
                                getUserName(),
                                getUserPhotoUrl(),
                                LOADING_IMAGE_URL, // Temporary image with loading indicator
                                null
                        );
                        Log.d(TAG, "onCreate: image uploading");
                        mBinding.chatEdit.setEnabled(false);
                        // Create a child reference and set the user's message at that location
                        FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                                .push().setValue(tempMessage, (error, ref) -> {
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
                                    StorageReference storageReference = FirebaseStorage.getInstance()
                                            // Create a child location for the current user
                                            .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            // Create a child location for the database key
                                            .child(databaseKey)
                                            // Create a child with the filename
                                            .child(imageUri.getLastPathSegment());

                                    // IMPORTANT: UPLOAD FUNCTION IS HERE.
                                    // Begin upload of selected image
                                    putImageInStorage(storageReference, imageUri, databaseKey, tempMessage);

                                    // Clear the input message field if any for the next message
                                    mBinding.chatEdit.setText("");
                                });
                    }
                    else if(mime.matches("audio(.*)")){


                        Log.d(TAG, "onCreate: In audio upload function");
                        final Uri audioUri = result;
                        // Construct a message with temporary loading image
                        final Message tempMessage = new Message(
                                getMessageToSend(),  // If user has entered some message, publish it as well
                                getUserName(),
                                getUserPhotoUrl(),
                                null,
                                ""// Temp voice, empty string
                        );
                        Log.d(TAG, "onCreate: voice uploading");
                        mBinding.chatEdit.setEnabled(false);
                        // Create a child reference and set the user's message at that location
                        FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                                .push().setValue(tempMessage, (error, ref) -> {
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
                            StorageReference storageReference = FirebaseStorage.getInstance()
                                    // Create a child location for the current user
                                    .getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    // Create a child location for the database key
                                    .child(databaseKey)
                                    // Create a child with the filename
                                    .child(audioUri.getLastPathSegment());

                            // IMPORTANT: UPLOAD FUNCTION IS HERE.
                            // Begin upload of selected audio
                            putVoiceInStorage(storageReference, audioUri, databaseKey, tempMessage);

                        });
                    }

                });
        // Register a click listener on the Add Image Button to send messages with Image on click
        mBinding.image.setOnClickListener(view -> {
            // Launch Gallery Intent for Image selection
            Log.d(TAG, "onCreate: Image button triggered");
            mGetContent.launch("image/*");

        });
        mBinding.voiceUpload.setOnClickListener(view -> {
            // Launch Gallery Intent for Image selection
            Log.d(TAG, "onCreate: Audio button triggered");
            mGetContent.launch("audio/*");
        });

        // TODO: cancel the event
        mBinding.alertCancel.setOnClickListener(view -> {
            deleteChild();
            userHelper.sendSMSToContacts("The above emergency alert has been cancelled.");
            Toast.makeText(getBaseContext(), "Alert cancelled.", Toast.LENGTH_SHORT);
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

    private String getPhoneNumber(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getPhoneNumber()==null?"ANONYMOUS":user.getPhoneNumber();
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
            mBinding.chatEdit.setEnabled(true);
            Log.d(TAG, "onComplete: edit enabled");
        }).addOnFailureListener(this, e -> {
            // Log the exception in case of failure
            Log.w(TAG, "Image upload task was not successful.", e);
        });
    }
    private void putVoiceInStorage(final StorageReference storageReference,
                                   final Uri voiceUri,
                                   final String databaseKey,
                                   final Message tempMessage) {
        // Upload the selected image
        UploadTask uploadTask = storageReference.putFile(voiceUri);

        // Chain UploadTask to get the resulting URI Task of the uploaded image
        uploadTask.continueWithTask(task -> {
            // Return the resulting URI Task of the uploaded image
            return task.getResult().getStorage().getDownloadUrl();
        }).addOnSuccessListener(this, uri -> {
            // When all tasks have completed successfully, update the corresponding reference
            // in the database with the URI of the uploaded image
            tempMessage.setVoiceUrl(uri.toString());
            FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName())
                    .child(databaseKey)
                    .setValue(tempMessage);
            Log.d(TAG, "putVoiceInStorage: Voice upload complete");
            // Clear the input message field if any for the next message
            mBinding.chatEdit.setEnabled(true);
            Log.d(TAG, "onComplete: edit enabled");
            mBinding.chatEdit.setText("");
        }).addOnFailureListener(this, e -> {
            // Log the exception in case of failure
            Log.w(TAG, "Voice upload task was not successful.", e);
        });
    }
    private void deleteChild(){
        // delete all messages in realtime db
        FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD+getUserName()).removeValue().addOnCompleteListener(
        this, val->{
                    Log.d(TAG, "deleteChild: The room content for user "+getUserName()+" has been deleted");
                    startActivity(new Intent(this, MainActivity.class));
                    finish(); // end of this activity
        }
        ).addOnFailureListener(this, val->{
            Log.w(TAG, "deleteChild: The room content for user "+getUserName()+" has NOT been deleted");
            startActivity(new Intent(this, MainActivity.class));
            finish(); // end of this activity
        });
        // Note: the Firestore does NOT support delete dir, I will do this on the server side by cloud function code.
        // why: I want to delete all audio and videos in this room when user cancel the alert
    }

//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        LatLng curPosition = new LatLng(latitude, longitude);
//        googleMap.addMarker(new MarkerOptions()
//                .position(curPosition)
//                .title("Current Position"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(curPosition));
//    }
}