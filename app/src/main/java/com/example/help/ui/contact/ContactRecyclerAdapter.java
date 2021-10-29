package com.example.help.ui.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.help.R;
import com.example.help.models.Contact;

import java.util.ArrayList;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.CViewHolder>{
    //TODO: Add search bar

    private ArrayList<Contact> contacts;
    Context context;
    private OnItemClickListener cListener;

    public interface OnItemClickListener{
        void onClickListener(int position);
        void onDeleteClick(int position, String contactName, String phoneNumber);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        cListener = listener;
    }

    public ContactRecyclerAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public CViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(context); // TODO: check context is correct
//        View view = inflater.inflate(R.layout.contact_row, parent, false);
//        return new CViewHolder(view);
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, parent, false);
        return new CViewHolder(v, cListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CViewHolder holder, int position) {
        String name = contacts.get(position).getName();
        String phoneNumber = contacts.get(position).getPhoneNumber();
        holder.name.setText(name);
        holder.phoneNumber.setText(phoneNumber);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class CViewHolder extends RecyclerView.ViewHolder{

        TextView name, phoneNumber;
        ImageButton deleteButton;

        public CViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            phoneNumber = itemView.findViewById(R.id.contact_phone);
            deleteButton = itemView.findViewById(R.id.deleteContactBtn);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // When any part of item is clicked, delete (for test)
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onClickListener(position);
                        }
                    }
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position, name.getText().toString(), phoneNumber.getText().toString());
                        }
                    }
                }
            });
        }


    }
}
