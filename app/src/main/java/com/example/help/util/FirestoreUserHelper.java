package com.example.help.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.help.models.Contact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreUserHelper {
    private static final String TAG = "FirestoreUserHelper";

    private static volatile FirestoreUserHelper instance;
    private static final String COLLECTION_USERS = "users";
    private static final String FIELD_CONTACT_LIST = "contactList";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phoneNumber";
    private HandlerThread handlerThread = new HandlerThread("FirestoreThread");
    private Handler threadHandler;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;



    private FirestoreUserHelper() {
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "FirestoreUserHelper: uid is " + userId);
    }

    public static FirestoreUserHelper getInstance() {
        FirestoreUserHelper result = instance;
        if (result != null) {
            return result;
        }
        synchronized(FirestoreUserHelper.class) {
            if (instance == null) {
                instance = new FirestoreUserHelper();
            }
            return instance;
        }
    }


    public void retrieveContacts(ContactListCallback callback) {
        DocumentReference docRef = db.collection(COLLECTION_USERS).document(userId);
        getJSONStr(docRef, new StringCallback() {
            @Override
            public void onCallback(String json) {
                try {
                    // Create list of contacts from JSON
                    ArrayList<Contact> contacts = new ArrayList<>();
                    JSONArray contactListArr = new JSONObject(json).getJSONArray("contactList");
                    for (int i = 0; i < contactListArr.length(); i++) {
                        JSONObject contactObj = contactListArr.getJSONObject(i);
                        Contact ctct = new Contact(contactObj.getString("name"), contactObj.getString("phoneNumber"));
                        contacts.add(ctct);
                    }
                    callback.onCallback(contacts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addContact(String name, String phoneNumber, SuccessCallback callback) {
        DocumentReference docRef = db.collection(COLLECTION_USERS).document(userId);
        Map<String, Object> contact = buildContact(name, phoneNumber);

        docRef.update(FIELD_CONTACT_LIST, FieldValue.arrayUnion(contact))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: contact added");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                        callback.onCallback(false);
                    }
                });
    }


    public void getJSONStr(DocumentReference docRef, StringCallback callback) {
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        Object obj = document.getData();
                        String json = new Gson().toJson(obj);
                        callback.onCallback(json);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void removeContact(String name, String phoneNumber, SuccessCallback callback) {
        DocumentReference contactRef = db.collection(COLLECTION_USERS).document(userId);
        Object contact = buildContact(name, phoneNumber);

        contactRef.update(FIELD_CONTACT_LIST, FieldValue.arrayRemove(contact))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: " + contactRef.toString());
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                        callback.onCallback(false);
                    }
                });

    }

    private Map<String, Object> buildContact(String name, String phoneNumber) {
        Map<String, Object> contact = new HashMap<>();
        contact.put(KEY_NAME, name);
        contact.put(KEY_PHONE, phoneNumber);
        return contact;
    }

    public void sendSMSToContacts(String msg){
        SmsManager smsManager = SmsManager.getDefault();
        retrieveContacts(new FirestoreUserHelper.ContactListCallback() {
            @Override
            public void onCallback(ArrayList<Contact> contacts) {
                // for every contact in contacts, send sms
                Log.d(TAG, "onCallback: sending SMS to contacts -> " + msg);
                for (Contact contact : contacts) {
                    smsManager.sendTextMessage(contact.getPhoneNumber(), null, msg, null, null);
                }
            }
        });
    }

    public interface SuccessCallback {
        void onCallback(boolean success);
    }

    public interface ContactListCallback {
        void onCallback(ArrayList<Contact> contacts);
    }

    public interface StringCallback {
        void onCallback(String str);
    }


}
