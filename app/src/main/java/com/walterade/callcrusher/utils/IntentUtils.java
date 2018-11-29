package com.walterade.callcrusher.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;


public class IntentUtils {

    public static void addContact(FragmentActivity activity, String name, String phone) {
        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, name)
                .putExtra(ContactsContract.Intents.Insert.PHONE, phone);

        activity.startActivityForResult(contactIntent, 1);
    }

    public static void showWebBrowser(FragmentActivity activity, String url) {
        Intent i = getWebBrowser(url);
        activity.startActivityForResult(i, 2);
    }

    /**
     * Builds an email intent with attachments
     *
     * @param subject         the message subject
     * @param body            the message body
     * @param attachments     the attachments
     * @return email intent
     */
    @NonNull
    static Intent email(@NonNull String email, @NonNull String subject, @NonNull String body, @NonNull ArrayList<Uri> attachments) {
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.setType("message/rfc822");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    public static void phoneCall(Context c, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        c.startActivity(intent);
    }

    public static Intent getWebBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        return i;
    }
}
