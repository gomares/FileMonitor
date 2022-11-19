package com.filemonitor.prueba.fileobserver;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.filemonitor.prueba.R;
import com.filemonitor.prueba.decoration.SpacesItemDecoration;

import java.util.ArrayList;

public class EventInfoActivity extends AppCompatActivity {

    DetailedListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_data);

        RecyclerView list = findViewById(R.id.detailed_list);


        ArrayList<eventData> data = (ArrayList<eventData>) getIntent().getSerializableExtra("dataList");



        adapter = new DetailedListAdapter(data);

        list.setAdapter(adapter);

        list.setLayoutManager(new GridLayoutManager(this,1));

        list.addItemDecoration(new SpacesItemDecoration(3));



    }

}
