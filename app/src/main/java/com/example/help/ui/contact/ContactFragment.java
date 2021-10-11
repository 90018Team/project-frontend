package com.example.help.ui.contact;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help.R;
import com.example.help.databinding.ContactFragmentBinding;
import com.example.help.util.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private ContactViewModel cViewModel;
    private ContactFragmentBinding binding;
    private FloatingActionButton addButton;
    private DatabaseHelper databaseHelper;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ContactRecyclerAdapter rAdapter;


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

        databaseHelper = new DatabaseHelper(this.getContext());
        addButton = (FloatingActionButton) root.findViewById(R.id.addButton);
        recyclerView = root.findViewById(R.id.contact_recycler);

        createContactsFromDb();
        populateRecyclerView(root);




        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_add_contact);
            }
        });

        return root;
    }

    private void createContactsFromDb() {
        Log.d(TAG, "createContactsFromDb: getting contacts from db");

        Cursor data = databaseHelper.getContacts();
        while(data.moveToNext()){
            contacts.add(new Contact(data.getString(1), data.getString(2)));
        }
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
            public void onDeleteClick(int position, String contactName) {
                removeContact(position);
                databaseHelper.deleteContactByName(contactName);
            }
        });
    }

    public void removeContact(int position) {
        contacts.remove(position);
        rAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        // TODO: Use the ViewModel
    }

}