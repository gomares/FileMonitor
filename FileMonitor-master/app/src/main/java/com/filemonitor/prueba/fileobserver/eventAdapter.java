
package com.filemonitor.prueba.fileobserver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.filemonitor.prueba.R;

import java.util.ArrayList;

public class eventAdapter extends
        RecyclerView.Adapter<eventAdapter.ViewHolder> {

    private ArrayList<eventData> fullList = new ArrayList<>();         // Lista con todos los eventos
    private ArrayList<Pair<typeofevents, Boolean>> filter = new ArrayList<>();   // Filtro
    private ArrayList<Pair<String,ArrayList<eventData>>> oList= new ArrayList<>();  // Lista de eventos que se muestran (los que pasan el filtro)
    private Context context;


    public eventAdapter(Context context){
        super();

        this.context = context;

        //Establece filtro por defecto
        for (typeofevents e: typeofevents.values()){
            if(e == typeofevents.UNKNOWN){
                filter.add(new Pair<>(e, false));
            }
            else {
                filter.add(new Pair<>(e, true));
            }
        }
    }

    public ArrayList<Pair<typeofevents, Boolean>> getFilter() {
        return filter;
    }



    public ArrayList<eventData> getList(){
        return fullList;
        //return oList; vamos a probar
    }




    /// FILTER RELATED FUNCTIONS

    public void setFilter(Pair<String,Boolean> f){

        for(int i=0; i<filter.size();i++){

            if( filter.get(i).first.toString().equals(f.first)){
                filter.set(i,new Pair<>(filter.get(i).first,f.second));

            }
        }

        applyFilter();
    }


    private Boolean satisfiesfilter(eventData e) {             // Returns true if event satisfies filter
        Boolean pasa = true;

        for (Pair<typeofevents, Boolean> f : filter) {
            if (e.event_type == f.first && !f.second){
                pasa = false;
            }

        }
        return pasa;
    }


    private void applyFilter(){
        oList = new ArrayList<>();

        for(eventData e : fullList){
            if(satisfiesfilter(e)){
                addInOrganizedList(e);
            }
        }

        notifyDataSetChanged();
    }




    ////////////////////////////


    private void addInOrganizedList(eventData e){

        int pos = -1;
        for (int i=0; i<oList.size() && pos==-1;i++){
            if(oList.get(i).first.equals(e.event_path)){
                pos = i;
                oList.get(pos).second.add(e);
            }
        }

        if(pos == -1) {

            ArrayList<eventData> l = new ArrayList<>();
            l.add(e);
            oList.add(new Pair<String, ArrayList<eventData>>(e.event_path, l));
        }


    }

    public void deleteList(){
        fullList = new ArrayList<>();
        oList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addEvent(eventData e){

        if(!fullList.isEmpty()){
            if(e.num != fullList.get(fullList.size()-1).num){ // Si el numero de evento no es igual al numero de evento anterior (evitar que se dupliquen)
                fullList.add(e);
                if(satisfiesfilter(e)) {
                    addInOrganizedList(e);
                    notifyDataSetChanged();
                }

            }
        }
        else {
            fullList.add(e);
            if(satisfiesfilter(e)) {
                addInOrganizedList(e);
                notifyDataSetChanged();
            }
        }
    }

    public void removeItem(int position){
        int pos;

        // Calcular la posicion real en el array teniendo en cuenta que la lista esta invertida
        if(position == oList.size()){                 // Evitar que se salga del array
            pos = position;
        }
        else {
            pos = oList.size() - position - 1;   // Recordar que la lista esta invertida
        }


        // Delete all event data relative to the path (item) in eventlist
        String path = oList.get(pos).first;
        for( int i=0; i<fullList.size();i++){
            if(path.equals(fullList.get(i).event_path)){
                fullList.remove(i);
                i--;  // Cuando se elimina un objeto hay que actualizar el indice para que funcione correctamente
            }
        }

        oList.remove(pos);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.org_row,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  eventAdapter.ViewHolder holder, int position) {
        holder.fillList(oList.get(oList.size() - position - 1));              // Muestra primero las ultimas posiciones


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                bundle.putSerializable("dataList",oList.get(oList.size() - position - 1).second);
                //Pasa el contexto en el primer parametro
                Intent intent = new Intent(holder.itemView.getContext(),EventInfoActivity.class);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }

        });
    }

    @Override
    public int getItemCount() {
        return oList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView numofevents;
        TextView event_path;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            event_path = itemView.findViewById(R.id.tv_path);
            numofevents = itemView.findViewById(R.id.tv_date);

        }

        public void fillList(Pair<String,ArrayList<eventData>> datos){
            event_path.setText(datos.first);
            numofevents.setText(Integer.toString(datos.second.size()));

        }




    }
}




