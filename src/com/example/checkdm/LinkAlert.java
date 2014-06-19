package com.example.checkdm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class LinkAlert {

    private Context mContext;
    
    public LinkAlert(Context context) {
        mContext = context;
    }
    
    public void showAlert() {
        new MyThread().start();
    }
    
    void showAlertWithLink(String title, String message,
            String positive, DialogInterface.OnClickListener poslistener,
            String neutral, DialogInterface.OnClickListener neulistner,
            String negative, DialogInterface.OnClickListener neglistener) {
        final SpannableString s = new SpannableString(message);
        Linkify.addLinks(s, Linkify.WEB_URLS);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
//        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(
//                LinkMovementMethod.getInstance());
        TextView textView = (TextView)alertDialog.findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    private class MyThread extends Thread {
        @Override
        public void run() {
//            Looper.prepare();
//            // TODO Auto-generated method stub
//            super.run();
//            showAlertWithLink("Cancel Download", "Cancel download id: www.baidu.com ",
//                    "Yes",
//                    null,
//                    null, null,
//                    "Cancel",
//                    null);
//            
//            Looper.loop();
            Log.d("LinkAlert", "run LinkAlert");
            Context context = mContext;
            Intent intent = new Intent(context, BackActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
