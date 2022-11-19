package com.filemonitor.prueba.apkList

import android.os.Parcelable
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.FeatureInfo
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.*

class AppDato : Parcelable {
    var packageInfo: PackageInfo? = null
    var packageManager: PackageManager? = null

    //GETTERS
    var appLabel: String? = null
    var packageName: String? = null
    var versionName: String? = null
    var targetVersion: Int? = null
    var path: String? = null
    var installedTime: Long = 0
    var lastUpdateTime: Long = 0
    var features: Array<FeatureInfo>?
    var permissions: Array<String>?

    //BUILDERS
    constructor(features: Array<FeatureInfo>?, permissions: Array<String>?) {
        this.features = features
        this.permissions = permissions
    }
    protected constructor(`in`: Parcel, features: Array<FeatureInfo>?, permissions: Array<String>?) {
        appLabel = `in`.readString()
        packageName = `in`.readString()
        versionName = `in`.readString()
        targetVersion = `in`.readInt()
        path = `in`.readString()
        installedTime = `in`.readLong()
        lastUpdateTime = `in`.readLong()
        this.features = `in`.createTypedArray(FeatureInfo.CREATOR)
        this.permissions = `in`.createStringArray()
        this.features = features
        this.permissions = permissions
    }

    constructor(pi: PackageInfo, pm: PackageManager, features: Array<FeatureInfo>?, permissions: Array<String>?) {
        appLabel = pm.getApplicationLabel(pi.applicationInfo) as String
        packageName = pi.packageName
        versionName = pi.versionName
        targetVersion = pi.applicationInfo.targetSdkVersion
        path = pi.applicationInfo.sourceDir
        installedTime = pi.firstInstallTime
        lastUpdateTime = pi.lastUpdateTime
        this.features = pi.reqFeatures
        this.permissions = pi.requestedPermissions
        this.features = features
        this.permissions = permissions
    }

    //OTHER FUNCTIONS
    override fun describeContents(): Int {
        return 0
    }

    //WRITER OF PARCEL
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(appLabel)
        dest.writeString(packageName)
        dest.writeString(versionName)
        dest.writeInt(targetVersion!!)
        dest.writeString(path)
        dest.writeLong(installedTime)
        dest.writeLong(lastUpdateTime)
        dest.writeTypedArray(features, flags)
        dest.writeStringArray(permissions)
    }

    override fun toString(): String {
        return "AppDato{" +
                ", appLabel='" + appLabel + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", targetVersion=" + targetVersion +
                ", path='" + path + '\'' +
                ", installedTime=" + installedTime +
                ", lastModify=" + lastUpdateTime +
                ", features=" + Arrays.toString(features) +
                ", permissions=" + Arrays.toString(permissions) +
                '}'
    }

    companion object {
        @JvmField
        val CREATOR: Creator<AppDato?> = object : Creator<AppDato?> {
            override fun createFromParcel(`in`: Parcel): AppDato? {
                return AppDato(`in`, null, null)
            }

            override fun newArray(size: Int): Array<AppDato?> {
                return arrayOfNulls(size)
            }
        }
    }
}