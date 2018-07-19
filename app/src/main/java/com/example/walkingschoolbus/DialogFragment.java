package com.example.walkingschoolbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class DialogFragment extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.marker_clicked_dialog,null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Intent intentBack = new Intent(getContext(),MainMenuActivity.class);
                        //startActivity(intentBack);
                        break;
                }
            }
        };

        setCancelable(false);
        return new AlertDialog.Builder(getContext(),android.R.style.Theme_Material_Dialog_Alert)
                .setView(v)
                .setMessage(getString(R.string.prompt_join))
                .setPositiveButton(R.string.no,listener)
                .setNegativeButton(R.string.yes,listener)
                .create();
    }
    }

