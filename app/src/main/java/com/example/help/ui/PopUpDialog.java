package com.example.help.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class PopUpDialog extends AppCompatDialogFragment {

    private String title;
    private String msg;
    private String buttonText;

    public PopUpDialog(String title, String msg, String buttonText) {
        super();

        this.title = title;
        this.msg = msg;
        this.buttonText = buttonText;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }

//    public Dialog onCreateDialog(String title, String msg, String buttonText){
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(title)
//                .setMessage(msg)
//                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
//        return builder.create();
//    }
}
