
package com.filemonitor.prueba.fileobserver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.filemonitor.prueba.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DetailedListAdapter extends
        RecyclerView.Adapter<DetailedListAdapter.ViewHolder> {


    ArrayList<eventData> data;

    public DetailedListAdapter(ArrayList<eventData> d){
        super();
        this.data = d;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.full_row,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  DetailedListAdapter.ViewHolder holder, int position) {
       holder.fillList(data.get(data.size() - position - 1));              // Muestra primero las ultimas posiciones


    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView event_type;
        TextView event_path;
        TextView date;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            event_type = itemView.findViewById(R.id.tv_eventtype);
            event_path = itemView.findViewById(R.id.tv_path2);
            date = itemView.findViewById(R.id.tv_date);
        }

        public void fillList(eventData datos){

            event_type.setText(datos.event_type.toString());
            event_path.setText(datos.event_path);
            date.setText(new SimpleDateFormat("HH:mm:ss:SS").format(datos.event_time));             // Dar formato a la fecha
        }


    }
}




