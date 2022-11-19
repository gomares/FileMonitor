package com.filemonitor.prueba.apkList;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.filemonitor.prueba.R;

public class ApkAdapter extends  RecyclerView.Adapter<ApkAdapter.ViewHolder>{

    List<PackageInfo> packageList;
    Activity context;
    PackageManager packageManager;


    public ApkAdapter(Activity context, List<PackageInfo> packageList,
                      PackageManager packageManager) {
        super();
        this.context = context;
        this.packageList = packageList;
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apk_row,null,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.fillList(packageList.get(position));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                AppDato appDato = new AppDato(null, null) ;

                //.setPackageInfo(packageList.get(position),packageManager);


                appDato = new AppDato(packageList.get(position),packageManager, null, null);
                //bundle.putSerializable("appData",appData);
                bundle.putParcelable("appDato",appDato);

                //Pasa el contexto en el primer parametro
                Intent intent = new Intent(holder.itemView.getContext(), ApkInfoActivity.class);
                //intent.putExtras(bundle);
                intent.putExtra("appDato",appDato);
                context.startActivity(intent);

            }
        });
    }

    public void setPackageList(List<PackageInfo> p){
        this.packageList = p;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView name;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.apkIcon);
            name = (TextView) itemView.findViewById(R.id.apkname);

        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void fillList(PackageInfo p) {
            Drawable appIcon = packageManager
                    .getApplicationIcon(p.applicationInfo);

            appIcon.setBounds(0, 0, 40, 40);

            icon.setImageDrawable(appIcon);
            String appName = packageManager.getApplicationLabel(
                    p.applicationInfo).toString();

            name.setText(appName);

            if(haveDangerousPerms(p)){
                name.setTextColor(Color.RED);
            }
            else{
                name.setTextColor(Color.BLACK);
            }


        }


    }

    private boolean haveDangerousPerms(PackageInfo pkginfo)  {

        Boolean cond = false;

        if (pkginfo.requestedPermissions != null) {
            for (String s : pkginfo.requestedPermissions) {

                try{
                if (packageManager.getPermissionInfo(s, 0).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                    cond = true;
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            }
        }


        return cond;
    }

}