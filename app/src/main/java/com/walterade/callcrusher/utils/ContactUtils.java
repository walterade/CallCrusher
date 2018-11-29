package com.walterade.callcrusher.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactUtils {

    public static class ContactInfo {
        public String name;
        public String imageUri;
        public boolean isContact;
    }

    public static boolean isNumberInContacts(Context c, String phoneNumber) {
        boolean inContacts = false;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                inContacts = cursor.moveToFirst();
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inContacts;
    }

    public static ContactInfo getContactInfo(Context c, String phoneNumber) {
        ContactInfo contactInfo = null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = new String[]{
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
            };
            Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int i;
                    contactInfo = new ContactInfo();
                    i = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    contactInfo.name = cursor.getString(i);
                    i = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI);
                    contactInfo.imageUri = cursor.getString(i);
                    contactInfo.isContact = true;
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactInfo;
    }

}
