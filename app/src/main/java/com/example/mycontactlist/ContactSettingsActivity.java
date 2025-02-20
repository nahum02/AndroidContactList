package com.example.mycontactlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationButton();
        settingButton();
        ListButton();
        settings();
        sortByButton();
        orderByButton();
    }

    private void settings() {
        String sortBy = getSharedPreferences("ContactListPreferences",
                Context.MODE_PRIVATE).getString("sortfield", "contactname");

        String orderBy = getSharedPreferences("ContactListPreferences",
                Context.MODE_PRIVATE).getString("sortorder", "ASC");

        RadioButton rbName = findViewById(R.id.radioName);
        RadioButton rbCity = findViewById(R.id.radioCity);
        RadioButton rbBirthday = findViewById(R.id.radioBirthday);

        if(sortBy.equalsIgnoreCase("contactname")){
            rbName.setChecked(true);
        }

        else if(sortBy.equalsIgnoreCase("city")){
            rbCity.setChecked(true);
        }

        else {
            rbBirthday.setChecked(true);
        }

        RadioButton rbAsc = findViewById(R.id.radioAscending);
        RadioButton rbDes = findViewById(R.id.radioDescending);

        if(orderBy.equalsIgnoreCase("ASC")){
            rbAsc.setChecked(true);
        }

        else {
            rbDes.setChecked(true);
        }


    }

    private void sortByButton() {
        RadioGroup sortBy = findViewById(R.id.radioGroupSortOrder);
        sortBy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbName = findViewById(R.id.radioName);
                RadioButton rbCity = findViewById(R.id.radioCity);

                if (rbName.isChecked()){
                    getSharedPreferences("ContactListPreferences",
                            Context.MODE_PRIVATE).edit().putString(
                                    "sortfield", "contactname").apply();
                }

                else if (rbCity.isChecked()){
                    getSharedPreferences("ContactListPreferences",
                            Context.MODE_PRIVATE).edit().putString(
                                    "sortfield", "city").apply();

                }

                else {
                    getSharedPreferences("ContactListPreferences",
                            Context.MODE_PRIVATE).edit().putString(
                                    "sortfield", "city").apply();
                }
            }
        });
    }


    public void orderByButton() {
        RadioGroup orderBy = findViewById(R.id.radioGroupSortBy);
        orderBy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbAscending = findViewById(R.id.radioAscending);

                if(rbAscending.isChecked()){
                    getSharedPreferences("ContactListPreferences",
                            Context.MODE_PRIVATE).edit().putString("sortorder", "ASC").apply();
                }

                else {
                    getSharedPreferences("ContactListPreferences",
                            Context.MODE_PRIVATE).edit().putString("sortorder", "DESC").apply();
                }

            }
        });
    }

    private void locationButton() {
        ImageButton ibList = findViewById(R.id.imageButtonLo);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactSettingsActivity.this, ContactMapActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void settingButton() {
        ImageButton ibList = findViewById(R.id.imageButtonSetting);
        ibList.setEnabled(false);
    }



    private void ListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactSettingsActivity.this, ContactListActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}