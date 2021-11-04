package com.example.help.ui.chatRoom;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        GlideApp.with(mBinding.messageImageView.getContext()).clear(mBinding.messageImageView);
        if (friendlyMessage.getText() != null && !TextUtils.isEmpty(friendlyMessage.getText())) {
            // If it contains a text message
            mBinding.messageTextView.setText(friendlyMessage.getText());
            mBinding.messageTextView.setVisibility(View.VISIBLE);
        } else {
            // If it does not contain any text message
            mBinding.messageTextView.setText("");
            mBinding.messageTextView.setVisibility(View.GONE);
        }

        if (friendlyMessage.getVoiceUrl() != null) {
            // If it contains an Image

            // Read the Image URL
            String voiceUrl = friendlyMessage.getVoiceUrl();
            mBinding.messageVoiceView.setAudio(voiceUrl);
//            mBinding.messageVoiceView.setText(voiceUrl);
            mBinding.messageVoiceView.setVisibility(View.VISIBLE);

        } else {
            // If it does not contain any Voice
            mBinding.messageVoiceView.setVisibility(View.GONE);
        }
        if (friendlyMessage.getImageUrl() != null) {
            // If it contains an Image

            // Read the Image URL
            String imageUrl = friendlyMessage.getImageUrl();
                // Get the Storage Reference pointing to the Image URL
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                // Download the Image into View directly from StorageReference using Glide
                GlideApp.with(mBinding.messageImageView.getContext())
                        .load(storageReference)
                        .into(mBinding.messageImageView);


            // On both messages
            // Set the messenger's profile picture
            GlideApp.with(mBinding.getRoot().getContext())
                    .load(friendlyMessage.getPhotoUrl())
                    .fallback(R.drawable.ic_account_circle_black_36dp)
                    .into(mBinding.messengerImageView);


        }
        if (friendlyMessage.getName() != null) {
            // Set the messenger's name
            mBinding.messengerTextView.setText(friendlyMessage.getName());
        }
        if (friendlyMessage.getTimeStampStr() != null) {
            // set the timestamp
            mBinding.timeStampTextView.setText(friendlyMessage.getTimeStampStr());
        }
    }
}
