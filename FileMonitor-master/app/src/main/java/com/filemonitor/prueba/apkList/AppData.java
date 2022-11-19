package com.filemonitor.prueba.apkList;



import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.Serializable;

public class AppData implements Serializable {

    PackageInfo packageInfo;
    PackageManager packageManager;
    String appLabel;
    String packageName;
    String versionName;
    Integer targetVersion;
    String path;
    long installedTime;
    long lastModify;
    FeatureInfo[] features;
    String[] permissions;

    public void setPackageInfo(PackageInfo pi, PackageManager pm) throws PackageManager.NameNotFoundException {

        // APP name
        appLabel = (String) pm.getApplicationLabel(pi.applicationInfo);

        // package name
        packageName = pi.packageName;

        // version name
        versionName = pi.versionName;

        // target version
        targetVersion = pi.applicationInfo.targetSdkVersion;

        // path
        path = pi.applicationInfo.sourceDir;

        // first installation
        installedTime = pi.firstInstallTime;

        // last modified
        lastModify = pi.lastUpdateTime;

        // features
        features = pi.reqFeatures;

        // uses-permission

        permissions = pi.requestedPermissions;

       // packageManager = pm;


        //this.packageInfo = pi;
    }
}
