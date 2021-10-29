package com.example.help.ui.contact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help.R;
import com.example.help.databinding.ContactFragmentBinding;
import com.example.help.models.Contact;
import com.example.help.ui.PopUpDialog;
import com.example.help.ui.signIn.SignInActivity;
import com.example.help.util.FirestoreUserHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private ContactViewModel cViewModel;
    private ContactFragmentBinding binding;
    private FloatingActionButton addButton;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ContactRecyclerAdapter rAdapter;
    private FirestoreUserHelper userHelper;


    RecyclerView recyclerView;


    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        cViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        binding = ContactFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // If user isn't signed in, prompt to sign in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d(TAG, "onCreateView: user not signed in");
            startActivity(new Intent(this.getContext(), SignInActivity.class));
            this.getActivity().finish();
            return root;
        }

        userHelper = new FirestoreUserHelper(FirebaseAuth.getInstance().getCurrentUser().getUid());
        addButton = (FloatingActionButton) root.findViewById(R.id.addButton);
        recyclerView = root.findViewById(R.id.contact_recycler);

        userHelper.retrieveContacts(new FirestoreUserHelper.ContactListCallback() {
            @Override
            public void onCallback(ArrayList<Contact> contactList) {
                Log.d(TAG, "onCallback: contacts retrieved");
                contacts = contactList;
                populateRecyclerView(root);
            }
        });


        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check permissions to access contacts
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    navigateToAddContacts();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                }
            }
        });


        return root;
    }

    private void navigateToAddContacts(){
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_contacts_to_add_contact_from_phone);
    }


    private void populateRecyclerView(View root) {
        Log.d(TAG, "populateRecyclerView: Displaying contacts in RecyclerView");

        rAdapter = new ContactRecyclerAdapter(root.getContext(), contacts);
        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        rAdapter.setOnItemClickListener(new ContactRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClickListener(int position) {
                // triggered when any part of contact is clicked
            }

            @Override
            public void onDeleteClick(int position, String contactName, String phoneNumber) {
                userHelper.removeContact(contactName, phoneNumber, new FirestoreUserHelper.SuccessCallback() {
                    @Override
                    public void onCallback(boolean success) {
                        if (success) {
                            toastMessage("Contact removed");
                            contacts.remove(position);
                            rAdapter.notifyItemRemoved(position);
                        } else {
                            toastMessage("Something went wrong");
                        }
                    }
                });
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    navigateToAddContacts();
                } else {
                    openPermissionsRequiredDialog();
                }
            });

    private void openPermissionsRequiredDialog() {
        String title = "Permissions Required";
        String msg = "HELP! requires permission to access your contacts to add them as emergency contacts.";
        String buttonText = "ok";
        PopUpDialog dialog = new PopUpDialog(title, msg, buttonText);
        dialog.show(getActivity().getSupportFragmentManager(), "permissions required");
    }

    public void toastMessage(String msg) {
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        // TODO: Use the ViewModel
    }

}