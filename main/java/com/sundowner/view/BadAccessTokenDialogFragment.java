package com.sundowner.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.sundowner.R;

public class BadAccessTokenDialogFragment extends DialogFragment {

    private static final String TAG = "BadAccessTokenDialogFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context ctx = getActivity();
        if (ctx == null) {
            Log.e(TAG, "Failed to get context");
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(R.string.bad_access_token)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                   }
               });
        return builder.create();
    }
}
