package com.example.help.ui.contact;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help.R;
import com.example.help.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class PhoneContactRecyclerAdapter extends RecyclerView.Adapter<PhoneContactRecyclerAdapter.CViewHolder> implements Filterable {
    private static final String TAG = "PhoneContactRecyclerAdapter";
    private ArrayList<Contact> allContacts;
    private ArrayList<Contact> filteredContacts;
    Context context;
    private PhoneContactRecyclerAdapter.OnItemClickListener cListener;

    public interface OnItemClickListener{
        void onClickListener(int position);
    }

    public void setOnItemClickListener(PhoneContactRecyclerAdapter.OnItemClickListener listener) {
        cListener = listener;
    }

    public PhoneContactRecyclerAdapter(Context context, ArrayList<Contact> contacts) {
        Log.d(TAG, "Created");
        this.context = context;
        filteredContacts = contacts;
        allContacts = new ArrayList<>(contacts);
    }

    @NonNull
    @Override
    public PhoneContactRecyclerAdapter.CViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.phone_contact_row, parent, false);
        return new CViewHolder(v, cListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneContactRecyclerAdapter.CViewHolder holder, int position) {
        Log.d(TAG, "OnBindViewHolder: Binding holders");
        String name = filteredContacts.get(position).getName();
        String phoneNumber = filteredContacts.get(position).getPhoneNumber();
        holder.name.setText(name);
        holder.phoneNumber.setText(phoneNumber);
    }

    public class CViewHolder extends RecyclerView.ViewHolder{

        TextView name, phoneNumber;

        public CViewHolder(@NonNull View itemView, final PhoneContactRecyclerAdapter.OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            phoneNumber = itemView.findViewById(R.id.contact_phone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onClickListener(position);
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredContacts.size();
    }

    public Filter getFilter(){
       return fltr;
    };

    private Filter fltr = new Filter(){

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Contact> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(allContacts);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Contact contact : allContacts) {
                    if (contact.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(contact);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredContacts.clear();
            filteredContacts.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };
}
