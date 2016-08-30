package com.example.permission;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import library.BaseActivity;
import library.RequestWithPermission;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestWithPermission.ITransactionListener listener = new RequestWithPermission.ITransactionListener() {
            @Override
            public void todo() {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    startActivityForResult(intent, 123);
                } catch (Exception e) {
                    Log.d("tag", "请打开您的通讯录权限，否则无法读取通讯录");
                }
            }
        };
        startRequest(new RequestWithPermission(listener,new String[]{Manifest.permission.READ_CONTACTS}));
    }
}
