package com.example.speakinghand;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {
    private static final String TAG = "MainActivity4";
    private VideoAdapter adapter;
    private ArrayList<Video> videoList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dictionary");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseApp.initializeApp(this);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VideoAdapter(MainActivity4.this, videoList);
        adapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onClick(Video video) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getUrl()));
                intent.setDataAndType(Uri.parse(video.getUrl()), "video/*");
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        loadVideos();
    }

    private void loadVideos() {
        Log.d(TAG, "Loading videos...");
        FirebaseStorage.getInstance().getReference().child("Videos").listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.d(TAG, "Videos retrieved: " + listResult.getItems().size());
                        videoList.clear();
                        for (StorageReference storageReference : listResult.getItems()) {
                            final Video video = new Video();
                            video.setTitle(storageReference.getName());
                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        video.setUrl(task.getResult().toString());
                                        videoList.add(video);
                                        Log.d(TAG, "Video added: " + video.getTitle());
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, "Failed to retrieve video URL", task.getException());
                                        Toast.makeText(MainActivity4.this, "Failed to retrieve video URL", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to retrieve videos", e);
                        Toast.makeText(MainActivity4.this, "Failed to retrieve videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filter(String query) {
        ArrayList<Video> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(videoList);
        } else {
            query = query.toLowerCase();
            for (Video video : videoList) {
                if (video.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(video);
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(MainActivity4.this, "Video not found", Toast.LENGTH_SHORT).show();
            }
        }
        adapter.filterList(filteredList);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
