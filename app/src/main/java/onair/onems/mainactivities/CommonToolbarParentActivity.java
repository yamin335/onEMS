package onair.onems.mainactivities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.util.Objects;

import onair.onems.R;

public class CommonToolbarParentActivity extends AppCompatActivity {
    public int UserTypeID;
    public CommonProgressDialog dialog;
    public long InstituteID, LoggedUserShiftID, LoggedUserMediumID, LoggedUserClassID,
            LoggedUserDepartmentID, LoggedUserSectionID, LoggedUserSessionID;
    public String activityName = "", LoggedUserID, UserName, UserFullName, ImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onems_common_toolbar_activity);

        dialog = new CommonProgressDialog(this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        UserTypeID = prefs.getInt("UserTypeID",0);
        InstituteID = prefs.getLong("InstituteID",0);
        LoggedUserID = prefs.getString("UserID", "0");
        UserName = prefs.getString("UserName", "");
        UserFullName = prefs.getString("UserFullName", "");
        ImageUrl = prefs.getString("ImageUrl", "0");
        LoggedUserShiftID = prefs.getLong("ShiftID",0);
        LoggedUserMediumID = prefs.getLong("MediumID",0);
        LoggedUserClassID = prefs.getLong("ClassID",0);
        if(UserTypeID == 3) {
            LoggedUserDepartmentID = prefs.getLong("SDepartmentID",0);
        } else {
            LoggedUserDepartmentID = prefs.getLong("DepartmentID",0);
        }
        LoggedUserSectionID = prefs.getLong("SectionID",0);
        LoggedUserSessionID = prefs.getLong("SessionID",0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
