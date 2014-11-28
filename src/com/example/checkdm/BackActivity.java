package com.example.checkdm;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


public class BackActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
//        new LinkAlert(this).showAlert();
        showAlertWithLink("Cancel Download", "Cancel download id: www.baidu.com ",
                "Yes",
                null,
                null, null,
                "Cancel",
                null);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    void showAlertWithLink(String title, String message,
            String positive, DialogInterface.OnClickListener poslistener,
            String neutral, DialogInterface.OnClickListener neulistner,
            String negative, DialogInterface.OnClickListener neglistener) {
        Log.d("Background", "show Alert with Message: " + message);
        final SpannableString s = new SpannableString(message);
        Linkify.addLinks(s, Linkify.WEB_URLS);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(s);
        builder.setPositiveButton(positive, poslistener);
        builder.setNegativeButton(negative, neglistener);
        builder.setNeutralButton(neutral, neulistner);
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        // for show alert in service.
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        alertDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }
}
