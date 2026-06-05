package com.numberbook.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button importButton;
    private Button uploadButton;
    private Button lookupButton;
    private EditText searchField;
    private RecyclerView entriesListView;
    private PhoneListAdapter listAdapter;
    private final List<PhoneEntry> localEntries = new ArrayList<>();
    private RemoteEndpoints remoteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        configureRecyclerView();
        remoteService = HttpServiceFactory.obtainInstance().create(RemoteEndpoints.class);
        attachClickHandlers();
    }

    private void bindViews() {
        importButton = findViewById(R.id.btnImportEntries);
        uploadButton = findViewById(R.id.btnUploadEntries);
        lookupButton = findViewById(R.id.btnLookupEntry);
        searchField = findViewById(R.id.etSearchQuery);
        entriesListView = findViewById(R.id.rvPhoneEntries);
    }

    private void configureRecyclerView() {
        entriesListView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new PhoneListAdapter(localEntries);
        entriesListView.setAdapter(listAdapter);
    }

    private void attachClickHandlers() {
        importButton.setOnClickListener(v -> requestContactsAccess());
        uploadButton.setOnClickListener(v -> transmitEntriesToBackend());
        lookupButton.setOnClickListener(v -> performRemoteLookup());
    }

    private void requestContactsAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            importDeviceContacts();
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private final androidx.activity.result.ActivityResultLauncher<String> contactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    importDeviceContacts();
                } else {
                    Toast.makeText(this, R.string.msg_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    private void importDeviceContacts() {
        localEntries.clear();

        Cursor phoneCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (phoneCursor != null) {
            int nameColumn = phoneCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            );
            int numberColumn = phoneCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            );

            while (phoneCursor.moveToNext()) {
                String personName = phoneCursor.getString(nameColumn);
                String phoneDigits = phoneCursor.getString(numberColumn);
                localEntries.add(new PhoneEntry(personName, phoneDigits));
            }
            phoneCursor.close();
        }

        listAdapter.refreshEntries(localEntries);
        String countMessage = getString(R.string.msg_entries_imported, localEntries.size());
        Toast.makeText(this, countMessage, Toast.LENGTH_SHORT).show();
    }

    private void transmitEntriesToBackend() {
        if (localEntries.isEmpty()) {
            Toast.makeText(this, R.string.msg_no_entries_to_upload, Toast.LENGTH_SHORT).show();
            return;
        }

        for (PhoneEntry entry : localEntries) {
            remoteService.pushEntry(entry).enqueue(new Callback<ServerResult>() {
                @Override
                public void onResponse(@NonNull Call<ServerResult> call,
                                       @NonNull Response<ServerResult> response) {
                }

                @Override
                public void onFailure(@NonNull Call<ServerResult> call, @NonNull Throwable error) {
                    Toast.makeText(MainActivity.this,
                            R.string.msg_network_failure, Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(this, R.string.msg_upload_started, Toast.LENGTH_SHORT).show();
    }

    private void performRemoteLookup() {
        String queryText = searchField.getText().toString().trim();

        if (queryText.isEmpty()) {
            Toast.makeText(this, R.string.msg_empty_search, Toast.LENGTH_SHORT).show();
            return;
        }

        remoteService.lookupEntries(queryText).enqueue(new Callback<List<PhoneEntry>>() {
            @Override
            public void onResponse(@NonNull Call<List<PhoneEntry>> call,
                                   @NonNull Response<List<PhoneEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listAdapter.refreshEntries(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PhoneEntry>> call, @NonNull Throwable error) {
                Toast.makeText(MainActivity.this,
                        R.string.msg_search_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
