package onair.onems.mainactivities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import onair.onems.R;
import onair.onems.attendance.AttendanceAdminDashboard;
import onair.onems.contacts.ContactsMainScreen;
import onair.onems.homework.HomeworkMainScreen;
import onair.onems.login.LoginScreen;
import onair.onems.notice.NoticeMainScreen;
import onair.onems.notification.NotificationMainScreen;
import onair.onems.result.ResultMainScreen;
import onair.onems.routine.RoutineMainScreen;
import onair.onems.attendance.TakeAttendance;
import onair.onems.icard.StudentiCardMain;
import onair.onems.syllabus.SyllabusMainScreen;
import onair.onems.user.Profile;

import static onair.onems.login.LoginScreen.MyPREFERENCES;

public class TeacherMainScreen extends AppCompatActivity {

    private FloatingActionButton fabMenu, fabLogout, fabChangePassword, fabChangeUserType;
    private CardView cardLogout, cardChangePassword, cardChangeUserType;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private Boolean isFabOpen = false;
    private long InstituteID;
    private String UserName, LoggedUserID;
    private View dimView;
    private ConstraintLayout dashboard, profile, notification, contacts;
    private TextView textDashboard, textProfile, textNotification, textContacts, notificationCounter;
    private ImageView iconDashboard, iconProfile, iconNotification, iconContacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_teacher);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        InstituteID = prefs.getLong("InstituteID",0);
        LoggedUserID = prefs.getString("UserID", "0");
        UserName = prefs.getString("UserName", "0");
        dimView = findViewById(R.id.dim);
        initializeFabAnimations();
        fabMenu = findViewById(R.id.floatingMenu);
//        fabMenu.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabMenu.setRippleColor(Color.parseColor("#341f97"));
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

        fabLogout = findViewById(R.id.log_out);
//        fabLogout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabLogout.setRippleColor(Color.parseColor("#341f97"));
        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences  = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("LogInState", false);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        fabChangePassword = findViewById(R.id.change_password);
//        fabChangePassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabChangePassword.setRippleColor(Color.parseColor("#341f97"));
        fabChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(TeacherMainScreen.this,
                        TeacherMainScreen.this, LoggedUserID, UserName, InstituteID);
                changePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changePasswordDialog.setCancelable(false);
                changePasswordDialog.show();
            }
        });

        fabChangeUserType = findViewById(R.id.change_user_type);
//        fabChangeUserType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabChangeUserType.setRippleColor(Color.parseColor("#341f97"));
        fabChangeUserType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeUserTypeDialog changeUserTypeDialog = new ChangeUserTypeDialog(TeacherMainScreen.this,
                        TeacherMainScreen.this);
                changeUserTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changeUserTypeDialog.setCancelable(false);
                changeUserTypeDialog.show();
            }
        });

        cardLogout = findViewById(R.id.card_log_out);
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences  = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("LogInState", false);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        cardChangePassword = findViewById(R.id.card_change_password);
        cardChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(TeacherMainScreen.this,
                        TeacherMainScreen.this, LoggedUserID, UserName, InstituteID);
                changePasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changePasswordDialog.setCancelable(false);
                changePasswordDialog.show();
            }
        });

        cardChangeUserType = findViewById(R.id.card_change_user_type);
        cardChangeUserType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeUserTypeDialog changeUserTypeDialog = new ChangeUserTypeDialog(TeacherMainScreen.this,
                        TeacherMainScreen.this);
                changeUserTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changeUserTypeDialog.setCancelable(false);
                changeUserTypeDialog.show();
            }
        });

        CardView attendance = findViewById(R.id.attendance);
        CardView iCard = findViewById(R.id.iCard);
        CardView notice = findViewById(R.id.notice);
        TextView InstituteName = (TextView) findViewById(R.id.InstituteName);
        TextView userType = (TextView) findViewById(R.id.userType);
        CardView result = findViewById(R.id.result);
        CardView exam = findViewById(R.id.exam);
        CardView routine = findViewById(R.id.routine);
        CardView homework = findViewById(R.id.homework);
        CardView syllabus = findViewById(R.id.syllabus);

        SharedPreferences sharedPre = PreferenceManager.getDefaultSharedPreferences(this);
        String InstituteNameString = sharedPre.getString("InstituteName","");
        InstituteName.setText(InstituteNameString);
        final int user = sharedPre.getInt("UserTypeID",0);
        if(user == 4) {
            userType.setText("Teacher");
        } else if(user == 1){
            userType.setText("Super Admin");
        } else if(user == 2){
            userType.setText("Institute Admin");
        }

        // Notice module start point
        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this,NoticeMainScreen.class);
                TeacherMainScreen.this.startActivity(mainIntent);
                finish();
            }
        });

        // Routine module start point
        routine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, RoutineMainScreen.class);
                TeacherMainScreen.this.startActivity(mainIntent);
                finish();
            }
        });

        // Attendance module start point
        attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user == 1||user == 2) {
                    Intent mainIntent = new Intent(TeacherMainScreen.this,AttendanceAdminDashboard.class);
                    TeacherMainScreen.this.startActivity(mainIntent);
                    finish();
                } else {
                    Intent mainIntent = new Intent(TeacherMainScreen.this,TakeAttendance.class);
                    TeacherMainScreen.this.startActivity(mainIntent);
                    finish();
                }
            }
        });

        // Syllabus module start point
        syllabus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, SyllabusMainScreen.class);
                startActivity(mainIntent);
                finish();
            }
        });

        homework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this,HomeworkMainScreen.class);
                TeacherMainScreen.this.startActivity(mainIntent);
                finish();
            }
        });

        // Result module start point
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, ResultMainScreen.class);
                TeacherMainScreen.this.startActivity(mainIntent);
                finish();
            }
        });

        // Contact module start point
        exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent mainIntent = new Intent(TeacherMainScreen.this, SyllabusMainScreen.class);
//                startActivity(mainIntent);
//                finish();
//                Intent mainIntent = new Intent(TeacherMainScreen.this,ContactsMainScreen.class);
//                TeacherMainScreen.this.startActivity(mainIntent);
//                finish();
            }
        });

        iCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, StudentiCardMain.class);
                TeacherMainScreen.this.startActivity(mainIntent);
                finish();
            }
        });

        notificationCounter = findViewById(R.id.notificationCounter);
        dashboard = findViewById(R.id.dashboard);
        profile = findViewById(R.id.profile);
        notification = findViewById(R.id.notification);
        contacts = findViewById(R.id.contacts);
        iconDashboard = findViewById(R.id.iconDashboard);
        iconProfile = findViewById(R.id.iconProfile);
        iconNotification = findViewById(R.id.iconNotification);
        iconContacts = findViewById(R.id.iconContact);
        textDashboard = findViewById(R.id.textDashboard);
        textProfile = findViewById(R.id.textProfile);
        textNotification = findViewById(R.id.textNotification);
        textContacts = findViewById(R.id.textContact);

        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBottomNavBar();
                dashboard.setBackgroundColor(Color.parseColor("#f9f7f7"));
                iconDashboard.setColorFilter(Color.parseColor("#0550b7"));
                textDashboard.setTextColor(Color.parseColor("#0550b7"));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBottomNavBar();
                profile.setBackgroundColor(Color.parseColor("#f9f7f7"));
                iconProfile.setColorFilter(Color.parseColor("#0550b7"));
                textProfile.setTextColor(Color.parseColor("#0550b7"));
                Intent mainIntent = new Intent(TeacherMainScreen.this,Profile.class);
                startActivity(mainIntent);
                finish();
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBottomNavBar();
                notification.setBackgroundColor(Color.parseColor("#f9f7f7"));
                iconNotification.setColorFilter(Color.parseColor("#0550b7"));
                textNotification.setTextColor(Color.parseColor("#0550b7"));
                notificationCounter.setVisibility(View.INVISIBLE);
                Intent mainIntent = new Intent(TeacherMainScreen.this,NotificationMainScreen.class);
                startActivity(mainIntent);
                finish();
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBottomNavBar();
                contacts.setBackgroundColor(Color.parseColor("#f9f7f7"));
                iconContacts.setColorFilter(Color.parseColor("#0550b7"));
                textContacts.setTextColor(Color.parseColor("#0550b7"));
                Intent mainIntent = new Intent(TeacherMainScreen.this,ContactsMainScreen.class);
                startActivity(mainIntent);
                finish();
            }
        });

    }

    private void refreshBottomNavBar(){
        dashboard.setBackgroundColor(Color.WHITE);
        iconDashboard.setColorFilter(Color.parseColor("#757575"));
        textDashboard.setTextColor(Color.parseColor("#757575"));

        profile.setBackgroundColor(Color.WHITE);
        iconProfile.setColorFilter(Color.parseColor("#757575"));
        textProfile.setTextColor(Color.parseColor("#757575"));

        notification.setBackgroundColor(Color.WHITE);
        iconNotification.setColorFilter(Color.parseColor("#757575"));
        textNotification.setTextColor(Color.parseColor("#757575"));

        contacts.setBackgroundColor(Color.WHITE);
        iconContacts.setColorFilter(Color.parseColor("#757575"));
        textContacts.setTextColor(Color.parseColor("#757575"));

    }

    private void initializeFabAnimations() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_open.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                dimView.setVisibility(View.VISIBLE);
                fabChangeUserType.setVisibility(View.VISIBLE);
                cardChangeUserType.setVisibility(View.VISIBLE);
                fabChangePassword.setVisibility(View.VISIBLE);
                cardChangePassword.setVisibility(View.VISIBLE);
                fabLogout.setVisibility(View.VISIBLE);
                cardLogout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fab_close.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardLogout.setVisibility(View.GONE);
                fabLogout.setVisibility(View.GONE);
                cardChangePassword.setVisibility(View.GONE);
                fabChangePassword.setVisibility(View.GONE);
                cardChangeUserType.setVisibility(View.GONE);
                fabChangeUserType.setVisibility(View.GONE);
                dimView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
    }

    public void animateFAB(){

        if(isFabOpen){
            dimView.startAnimation(fab_close);
            fabMenu.startAnimation(rotate_backward);
            cardLogout.startAnimation(fab_close);
            fabLogout.startAnimation(fab_close);
            cardChangePassword.startAnimation(fab_close);
            fabChangePassword.startAnimation(fab_close);
            cardChangeUserType.startAnimation(fab_close);
            fabChangeUserType.startAnimation(fab_close);
            cardLogout.setClickable(false);
            fabLogout.setClickable(false);
            cardChangePassword.setClickable(false);
            fabChangePassword.setClickable(false);
            cardChangeUserType.setClickable(false);
            fabChangeUserType.setClickable(false);
            isFabOpen = false;

        } else {
            dimView.startAnimation(fab_open);
            fabMenu.startAnimation(rotate_forward);
            fabChangeUserType.startAnimation(fab_open);
            cardChangeUserType.startAnimation(fab_open);
            fabChangePassword.startAnimation(fab_open);
            cardChangePassword.startAnimation(fab_open);
            fabLogout.startAnimation(fab_open);
            cardLogout.startAnimation(fab_open);
            fabChangeUserType.setClickable(true);
            cardChangeUserType.setClickable(true);
            fabChangePassword.setClickable(true);
            cardChangePassword.setClickable(true);
            fabLogout.setClickable(true);
            cardLogout.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onBackPressed() {
        if(isFabOpen){
            animateFAB();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.drawable.onair);
            builder.setMessage("Do you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
