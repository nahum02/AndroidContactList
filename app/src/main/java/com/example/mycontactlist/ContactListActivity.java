package com.example.mycontactlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {



    private View.OnClickListener onItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
            int position = viewHolder.getAdapterPosition();
            Intent intent = new Intent(ContactListActivity.this, MainActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationButton();
        settingButton();
        ListButton();

        ContactDataSource ds = new ContactDataSource(this);
        ArrayList<String> names;


        try {
            ds.open();
            names = ds.getContactName();
            Log.d("DB_DEBUG", "Names retrieved: " + names.size());

            ds.close();

            RecyclerView contactList = findViewById(R.id.rvC);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            contactList.setLayoutManager(layoutManager);
            ContactAdapter contactAdapter = new ContactAdapter(names);
            contactAdapter.setOnItemClickListener(onItemClickListener);
            //Log.d("DB_DEBUG", "display name: " + layoutManager);
            contactList.setAdapter(contactAdapter);
        }

        catch (Exception e){
            Toast.makeText(this,"Error retrieving contacts", Toast.LENGTH_LONG).show();
        }


    }

    private void locationButton() {
        ImageButton ibList = findViewById(R.id.imageButtonLo);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactListActivity.this, ContactMapActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void settingButton() {
        ImageButton ibList = findViewById(R.id.imageButtonSetting);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactListActivity.this, ContactSettingsActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }



    private void ListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactListActivity.this, ContactListActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}

