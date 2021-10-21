package com.example.help.ui.chatRoom;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.help.R;
import com.example.help.databinding.MessageBinding;
import com.example.help.models.Message;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MessageHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;
    public TextView name;
    public TextView message;
    public ImageView image;
    ChatroomGlideModule glideApp;
    // Constant used for Logs
    public static final String TAG = MessageHolder.class.getSimpleName();

    private final MessageBinding mBinding;

    public MessageHolder(View itemView) {
        super(itemView);
        mBinding = MessageBinding.bind(itemView);
        this.glideApp = new ChatroomGlideModule();
    }

    public void setName(String string) {
        name.setText(string);
    }


    public void setMessage(String string) {
        message.setText(string);
    }
    public void bindMessage(Message friendlyMessage) {
        if (friendlyMessage.getText() != null && !TextUtils.isEmpty(friendlyMessage.getText())) {
            // If it contains a text message
            mBinding.messageTextView.setText(friendlyMessage.getText());
            mBinding.messageTextView.setVisibility(View.VISIBLE);
        } else {
            // If it does not contain any text message
            mBinding.messageTextView.setText("");
            mBinding.messageTextView.setVisibility(View.GONE);
        }

        if (friendlyMessage.getImageUrl() != null) {
            // If it contains an Image

            // Read the Image URL
            String imageUrl = friendlyMessage.getImageUrl();

            if (imageUrl.startsWith("gs://")) {
                // If the Image URL is pointing to an Image stored in Firebase Cloud Storage

                // Get the Storage Reference pointing to the Image URL
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                // Download the Image into View directly from StorageReference using Glide
                GlideApp.with(mBinding.messageImageView.getContext())
                        .load(storageReference)
                        .into(mBinding.messageImageView);

            } else {
                // If the Image URL is not from Firebase Cloud Storage,
                // then load the Image from URL into ImageView directly using Glide
                GlideApp.with(mBinding.messageImageView.getContext())
                        .load(friendlyMessage.getImageUrl())
                        .into(mBinding.messageImageView);
            }

            mBinding.messageImageView.setVisibility(View.VISIBLE);

        } else {
            // If it does not contain any Image
            mBinding.messageImageView.setVisibility(View.GONE);
        }

        // On both messages

        // Set the messenger's profile picture
        GlideApp.with(mBinding.getRoot().getContext())
                .load(friendlyMessage.getPhotoUrl())
                .fallback(R.drawable.ic_account_circle_black_36dp)
                .into(mBinding.messengerImageView);

        // Set the messenger's name
        mBinding.messengerTextView.setText(friendlyMessage.getName());
    }
}
