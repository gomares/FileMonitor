package com.filemonitor.prueba.apkList;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.filemonitor.prueba.R;
import com.filemonitor.prueba.decoration.SpacesItemDecoration;

public class Activity_apklist extends Activity  {

    PackageManager packageManager;
    RecyclerView apkList;
    RecyclerView apkListNuevas;
    ApkAdapter adapter;
    ApkAdapter adapter2;
    static int cnt = 0;
    static List<PackageInfo> inicialAppList =  null;
    List<PackageInfo> nuevaList = new ArrayList<PackageInfo>();

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.diff_apklist);

        packageManager = getPackageManager();

        List<PackageInfo> packageList = packageManager
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);

        List<PackageInfo> fullAppList = new ArrayList<PackageInfo>();

        List<PackageInfo> userAppList = new ArrayList<PackageInfo>();

        /*To filter out System apps*/
        for(PackageInfo pi : packageList) {

            fullAppList.add(pi);

            if(!isSystemPackage(pi)){
                userAppList.add(pi);
            }

        }

        if (cnt == 0){
            inicialAppList = userAppList;
        }

        cnt ++;

        for (int i = 0; i < userAppList.size(); i++){

            if (esNuevaApp(inicialAppList,userAppList.get(i)))
                nuevaList.add(userAppList.get(i));

        }


        adapter = new ApkAdapter(this, inicialAppList, packageManager);
        adapter.setPackageList(inicialAppList);

        adapter2 = new ApkAdapter(this, nuevaList, packageManager);
        adapter2.setPackageList(nuevaList);

        apkList = findViewById(R.id.apk_list);
        apkList.setAdapter(adapter);
        apkList.setLayoutManager(new GridLayoutManager(this,1));
        apkList.addItemDecoration(new SpacesItemDecoration(3));

        apkListNuevas = findViewById(R.id.apps_nuevas);
        apkListNuevas.setAdapter(adapter2);
        apkListNuevas.setLayoutManager(new GridLayoutManager(this,1));
        apkListNuevas.addItemDecoration(new SpacesItemDecoration(3));

    }

    /**
     * Return whether the given PackgeInfo represents a system package or not.
     * User-installed packages (Market or otherwise) should not be denoted as
     * system packages.
     *
     * @param pkgInfo
     * @return boolean
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true
                : false;
    }

    @SuppressLint("LongLogTag")
    private boolean haveDangerousPerms(PackageInfo pkginfo)  {

        Boolean cond = false;

        if (pkginfo.requestedPermissions != null) {
            for (String s : pkginfo.requestedPermissions) {
                try {
                    if (packageManager.getPermissionInfo(s, 0).protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) {
                        cond = true;
                        break;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.i("PM.NameNotFoundException", s);
                }

            }
        }
        return cond;
    }

    private boolean esNuevaApp(List<PackageInfo> lista, PackageInfo app){

        for (int i = 0; i < lista.size(); i++){

            if (lista.get(i).packageName.equals(app.packageName))
                return false;

        }

        return true;

    }

    public List<PackageInfo> getInicialList(){

        return inicialAppList;

    }

    public List<PackageInfo> getBlackList(){

        return nuevaList;

    }


}



