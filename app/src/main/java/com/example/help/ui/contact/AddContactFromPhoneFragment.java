package com.example.help.ui.contact;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.help.R;
import com.example.help.databinding.AddContactFromPhoneBinding;
import com.example.help.util.DatabaseHelper;

import java.util.ArrayList;


public class AddContactFromPhoneFragment extends Fragment {

    private static final String TAG = "AddContactFromPhoneF";
    private AddContactFromPhoneBinding binding;
    private DatabaseHelper databaseHelper;
    private ArrayList<Contact> phoneContacts = new ArrayList<>();
    private PhoneContactRecyclerAdapter rAdapter;
    RecyclerView recyclerView;
    android.widget.SearchView searchView;

    public static AddContactFromPhoneFragment newInstance() {
        return new AddContactFromPhoneFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AddContactFromPhoneBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.d(TAG, "Opened.");


        databaseHelper = new DatabaseHelper(this.getContext());
        recyclerView = root.findViewById(R.id.phone_contact_recycler);
        searchView = root.findViewById(R.id.phone_contacts_search);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // grab contacts from phone
        getPhoneContacts();
        // populate recyclerview with contacts
        populateRecyclerView(root);

        // set search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                rAdapter.getFilter().filter(s);
                return false;
            }
        });


        return root;
    }



    private void getPhoneContacts() {

        // Check permissions to read contacts
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[] {Manifest.permission.READ_CONTACTS}, 0);
            //TODO: Handle if user denies permission
        }

        ContentResolver contentResolver = this.getContext().getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);

        Log.d(TAG, "getPhoneContacts: total # of contacts retrieved from phone: " + Integer.toString(cursor.getCount()));
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()){
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                Log.d(TAG, "Retreived contact: " + cursor.getString(nameIdx) + ", " + cursor.getString(phoneIdx));
                phoneContacts.add(new Contact(cursor.getString(nameIdx), cursor.getString(phoneIdx)));
            }
        }


    }

    private void populateRecyclerView(View root) {
        Log.d(TAG, "populateRecyclerView: Displaying contacts in RecyclerView");

        rAdapter = new PhoneContactRecyclerAdapter(root.getContext(), phoneContacts);
        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        rAdapter.setOnItemClickListener(new PhoneContactRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onClickListener(int position) {
                // add contact
                Contact clickedContact = phoneContacts.get(position);
                Log.d(TAG, "Adding " + clickedContact.getName());
                boolean succeeded = addContact(clickedContact.getName(), clickedContact.getPhoneNumber());
                if (succeeded) {
                    Navigation.findNavController(root).navigate(R.id.action_add_contact_to_contacts);
                    closeKeyboard();
                }
            }

        });
    }

    public boolean addContact(String name, String phone) {
        boolean dataInserted = databaseHelper.addContact(name, phone);

        if (dataInserted) {
            toastMessage("Contact added!");
            return true;
        } else {
            toastMessage("Something went wrong");
            return false;
        }
    }

    public void toastMessage(String msg) {
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void closeKeyboard(){
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

}