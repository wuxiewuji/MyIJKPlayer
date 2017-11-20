/*
 * *************************************************************************
 *  Permissions.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package com.tools.xxf.ijklib.media;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.tools.xxf.ijklib.R;


public class Permissions {

    public static final int PERMISSION_SYSTEM_RINGTONE = 42;
    public static final int PERMISSION_SYSTEM_BRIGHTNESS = 43;
    /*
     * Marshmallow permission system management
     */
    public static final boolean isNougatOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    public static final boolean isMarshMallowOrLater = isNougatOrLater || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean canWriteSettings(Context context) {
        return isMarshMallowOrLater || Settings.System.canWrite(context);
    }


    public static void checkWriteSettingsPermission(Activity activity, int mode) {
        if (!canWriteSettings(activity)) {
            showSettingsPermissionDialog(activity, mode);
        }
    }

    private static Dialog sAlertDialog;

    public static void showSettingsPermissionDialog(final Activity activity, int mode) {
        if (activity.isFinishing() || (sAlertDialog != null && sAlertDialog.isShowing()))
            return;
        sAlertDialog = createSettingsDialogCompat(activity, mode);
    }


    private static Dialog createSettingsDialogCompat(final Activity activity, int mode) {
        int titleId = 0, textId = 0;
        String action = Settings.ACTION_MANAGE_WRITE_SETTINGS;
        switch (mode) {
            case PERMISSION_SYSTEM_RINGTONE:
                titleId = R.string.allow_settings_access_ringtone_title;
                textId = R.string.allow_settings_access_ringtone_description;
                break;
            case PERMISSION_SYSTEM_BRIGHTNESS:
                titleId = R.string.allow_settings_access_brightness_title;
                textId = R.string.allow_settings_access_brightness_description;
                break;
        }
        final String finalAction = action;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(titleId))
                .setMessage(activity.getString(textId))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(activity.getString(R.string.permission_ask_again), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
                        Intent i = new Intent(finalAction);
                        i.setData(Uri.parse("package:" + activity.getPackageName()));
                        try {
                            activity.startActivity(i);
                        } catch (Exception ex) {}
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("user_declined_settings_access", true);
                        editor.apply();
                    }
                });
        return dialogBuilder.show();
    }

}
