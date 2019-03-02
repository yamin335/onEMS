package onair.onems.mainactivities;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import onair.onems.R;
import onair.onems.Services.RetrofitNetworkService;
import onair.onems.Services.StaticHelperClass;
import onair.onems.app.Config;
import onair.onems.attendance.AttendanceAdminDashboard;
import onair.onems.contacts.ContactsMainScreen;
import onair.onems.exam.SubjectWiseMarksEntryMain;
import onair.onems.homework.HomeworkMainScreenForAdmin;
import onair.onems.login.ChangePasswordDialog;
import onair.onems.login.LoginScreen;
import onair.onems.notice.NoticeMainScreen;
import onair.onems.notification.NotificationDetails;
import onair.onems.notification.NotificationMainScreen;
import onair.onems.result.ResultMainScreen;
import onair.onems.routine.RoutineMainScreen;
import onair.onems.attendance.TakeAttendance;
import onair.onems.icard.StudentiCardMain;
import onair.onems.syllabus.SyllabusMainScreenForAdmin;
import onair.onems.user.Profile;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private CompositeDisposable finalDisposer = new CompositeDisposable();
    public CommonProgressDialog dialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!finalDisposer.isDisposed())
            finalDisposer.dispose();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        MenuItem item = bottomNavigationView.getMenu().findItem(selectedBottomNavItem);
//        item.setChecked(true);
        int i = getSharedPreferences("UNSEEN_NOTIFICATIONS", Context.MODE_PRIVATE)
                .getInt("unseen", 0);
        if(i != 0) {
            notificationCounter.setVisibility(View.VISIBLE);
            notificationCounter.setText(Integer.toString(i));
        } else {
            notificationCounter.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
//        NotificationUtils.clearNotifications(getApplicationContext());
        int i = getSharedPreferences("UNSEEN_NOTIFICATIONS", Context.MODE_PRIVATE)
                .getInt("unseen", 0);
        if(i != 0) {
            notificationCounter.setVisibility(View.VISIBLE);
            notificationCounter.setText(Integer.toString(i));
        } else {
            notificationCounter.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_teacher);
        dialog = new CommonProgressDialog(this);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        InstituteID = prefs.getLong("InstituteID",0);
        LoggedUserID = prefs.getString("UserID", "0");
        UserName = prefs.getString("UserName", "0");
        dimView = findViewById(R.id.dim);
        dimView.setOnClickListener(v -> {
            if(isFabOpen){
                animateFAB();
            }
        });
        initializeFabAnimations();
        fabMenu = findViewById(R.id.floatingMenu);
//        fabMenu.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabMenu.setRippleColor(Color.parseColor("#341f97"));
        fabMenu.setOnClickListener(v -> animateFAB());

        fabLogout = findViewById(R.id.log_out);
//        fabLogout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabLogout.setRippleColor(Color.parseColor("#341f97"));
        fabLogout.setOnClickListener(v -> doLogOut());

        fabChangePassword = findViewById(R.id.change_password);
//        fabChangePassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabChangePassword.setRippleColor(Color.parseColor("#341f97"));
        fabChangePassword.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(TeacherMainScreen.this,
                    TeacherMainScreen.this, LoggedUserID, UserName, InstituteID);
            Objects.requireNonNull(changePasswordDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            changePasswordDialog.setCancelable(false);
            changePasswordDialog.show();
        });

        fabChangeUserType = findViewById(R.id.change_user_type);
//        fabChangeUserType.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#5f27cd")));
        fabChangeUserType.setRippleColor(Color.parseColor("#341f97"));
        fabChangeUserType.setOnClickListener(v -> {
            ChangeUserTypeDialog changeUserTypeDialog = new ChangeUserTypeDialog(TeacherMainScreen.this,
                    TeacherMainScreen.this);
            Objects.requireNonNull(changeUserTypeDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            changeUserTypeDialog.setCancelable(false);
            changeUserTypeDialog.show();
        });

        cardLogout = findViewById(R.id.card_log_out);
        cardLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences  = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("LogInState", false);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
            startActivity(intent);
            finish();
        });

        cardChangePassword = findViewById(R.id.card_change_password);
        cardChangePassword.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(TeacherMainScreen.this,
                    TeacherMainScreen.this, LoggedUserID, UserName, InstituteID);
            Objects.requireNonNull(changePasswordDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            changePasswordDialog.setCancelable(false);
            changePasswordDialog.show();
        });

        cardChangeUserType = findViewById(R.id.card_change_user_type);
        cardChangeUserType.setOnClickListener(v -> {
            ChangeUserTypeDialog changeUserTypeDialog = new ChangeUserTypeDialog(TeacherMainScreen.this,
                    TeacherMainScreen.this);
            Objects.requireNonNull(changeUserTypeDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            changeUserTypeDialog.setCancelable(false);
            changeUserTypeDialog.show();
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
            userType.setText(R.string.staff);
        } else if(user == 1){
            userType.setText(R.string.super_admin);
        } else if(user == 2){
            userType.setText(R.string.admin);
        }

        // Notice module start point
        notice.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this,NoticeMainScreen.class);
            startActivity(mainIntent);
            finish();
        });

        // Routine module start point
        routine.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, RoutineMainScreen.class);
            startActivity(mainIntent);
            finish();
        });

        // Attendance module start point
        attendance.setOnClickListener(v -> {
            if(user == 1||user == 2) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, AttendanceAdminDashboard.class);
                startActivity(mainIntent);
                finish();
            } else if (user == 4) {
                Intent mainIntent = new Intent(TeacherMainScreen.this, TakeAttendance.class);
                mainIntent.putExtra("fromDashBoard", false);
                mainIntent.putExtra("fromSideMenu", true);
                startActivity(mainIntent);
                finish();
            }
        });

        // Syllabus module start point
        syllabus.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, SyllabusMainScreenForAdmin.class);
            startActivity(mainIntent);
            finish();
        });

        homework.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, HomeworkMainScreenForAdmin.class);
            startActivity(mainIntent);
            finish();
        });

        // Result module start point
        result.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, ResultMainScreen.class);
            startActivity(mainIntent);
            finish();
        });

        // Contact module start point
        exam.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, SubjectWiseMarksEntryMain.class);
            startActivity(mainIntent);
            finish();
//
//                Intent mainIntent = new Intent(TeacherMainScreen.this, PrivacyPolicy.class);
//                startActivity(mainIntent);
//                finish();
        });

        iCard.setOnClickListener(v -> {
            Intent mainIntent = new Intent(TeacherMainScreen.this, StudentiCardMain.class);
            startActivity(mainIntent);
            finish();
        });

        notificationCounter = findViewById(R.id.notificationCounter);
        int i = getSharedPreferences("UNSEEN_NOTIFICATIONS", Context.MODE_PRIVATE)
                .getInt("unseen", 0);
        if(i != 0) {
            notificationCounter.setText(Integer.toString(i));
        } else {
            notificationCounter.setVisibility(View.INVISIBLE);
        }
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

        dashboard.setOnClickListener(v -> {
            refreshBottomNavBar();
            dashboard.setBackgroundColor(Color.parseColor("#f9f7f7"));
            iconDashboard.setColorFilter(Color.parseColor("#0550b7"));
            textDashboard.setTextColor(Color.parseColor("#0550b7"));
        });

        profile.setOnClickListener(v -> {
            refreshBottomNavBar();
            profile.setBackgroundColor(Color.parseColor("#f9f7f7"));
            iconProfile.setColorFilter(Color.parseColor("#0550b7"));
            textProfile.setTextColor(Color.parseColor("#0550b7"));
            Intent mainIntent = new Intent(TeacherMainScreen.this,Profile.class);
            startActivity(mainIntent);
            finish();
        });

        notification.setOnClickListener(v -> {
            refreshBottomNavBar();
            notification.setBackgroundColor(Color.parseColor("#f9f7f7"));
            iconNotification.setColorFilter(Color.parseColor("#0550b7"));
            textNotification.setTextColor(Color.parseColor("#0550b7"));
            Intent mainIntent = new Intent(TeacherMainScreen.this,NotificationMainScreen.class);
            startActivity(mainIntent);
            finish();
        });

        contacts.setOnClickListener(v -> {
            refreshBottomNavBar();
            contacts.setBackgroundColor(Color.parseColor("#f9f7f7"));
            iconContacts.setColorFilter(Color.parseColor("#0550b7"));
            textContacts.setTextColor(Color.parseColor("#0550b7"));
            Intent mainIntent = new Intent(TeacherMainScreen.this,ContactsMainScreen.class);
            startActivity(mainIntent);
            finish();
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (Objects.requireNonNull(intent.getAction()).equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
//                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TeacherMainScreen.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    int number = prefs.getInt("Notification",0);
//                    notificationCounter.setVisibility(View.VISIBLE);
//                    notificationCounter.setText(Integer.toString(++number));
                    editor.putInt("Notification", number);
                    editor.apply();
                    String message = intent.getStringExtra("notification");
                    try {
                        JSONObject messageJSONObject = new JSONObject(message);
                        //Show the notification when app is in foreground
                        // notification icon
                        final int icon = R.drawable.onair;
                        Intent resultIntent = new Intent(getApplicationContext(), NotificationDetails.class);
                        resultIntent.putExtra("notification", message);
                        int requestCode = new Random().nextInt(900000)+100000;
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, resultIntent, PendingIntent.FLAG_IMMUTABLE);
                        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), Config.NOTIFICATION_CHANNEL);
                        Notification notification = mBuilder.setSmallIcon(icon).setTicker(messageJSONObject.getString("title"))
                                .setAutoCancel(true)
                                .setContentTitle(messageJSONObject.getString("title"))
                                .setContentIntent(resultPendingIntent)
                                .setWhen(System.currentTimeMillis())
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(messageJSONObject.getString("body")))
                                .setContentText(messageJSONObject.getString("body"))
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .build();
                        int uniqueNotificationId = new Random().nextInt(900000)+100000;
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(uniqueNotificationId, notification);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int i = TeacherMainScreen.this.getSharedPreferences("UNSEEN_NOTIFICATIONS", Context.MODE_PRIVATE)
                            .getInt("unseen", 0);
                    if(i != 0) {
                        notificationCounter.setVisibility(View.VISIBLE);
                        notificationCounter.setText(Integer.toString(i));
                    } else {
                        notificationCounter.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };

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
                dimView.setClickable(true);
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
                dimView.setClickable(false);
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
                    .setPositiveButton("Yes", (dialog, id) -> finish())
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


//    private boolean logOut() {
//        if (StaticHelperClass.isNetworkAvailable(this)) {
//            dialog.show();
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl(getString(R.string.baseUrl))
//                    .addConverterFactory(ScalarsConverterFactory.create())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .build();
//
//            Observable<String> observable = retrofit
//                    .create(RetrofitNetworkService.class)
//                    .deleteFcmToken(LoggedUserID, "android", getSharedPreferences("UNIQUE_ID", Context.MODE_PRIVATE).getString("uuid", ""))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .unsubscribeOn(Schedulers.io());
//
//            finalDisposer.add( observable
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .unsubscribeOn(Schedulers.io())
//                    .subscribeWith(new DisposableObserver<String>() {
//
//                        @Override
//                        public void onNext(String response) {
//                            dialog.dismiss();
//                            doLogOut(response);
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            dialog.dismiss();
//                            returnValue = false;
//                            Toast.makeText(TeacherMainScreen.this,"Server error while logging out!!! ",
//                                    Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    }));
//        } else {
//            Toast.makeText(TeacherMainScreen.this,"Please check your internet connection and select again!!! ",
//                    Toast.LENGTH_LONG).show();
//        }
//        return returnValue;
//    }
//
//    private void doLogOut(String returnValueFromServer) {
//        try {
//            if (new JSONArray(returnValueFromServer).getJSONObject(0).getInt("ReturnValue") == 1) {
//                SharedPreferences sharedPreferences  = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean("LogInState", false);
//                editor.apply();
//
//                getSharedPreferences("PUSH_NOTIFICATIONS", Context.MODE_PRIVATE)
//                        .edit()
//                        .putString("notifications", "[]")
//                        .apply();
//
//                returnValue = true;
//                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
//                startActivity(intent);
//                finish();
//            } else {
//                returnValue = false;
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void doLogOut() {
        SharedPreferences sharedPreferences  = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("LogInState", false);
        editor.apply();

        getSharedPreferences("PUSH_NOTIFICATIONS", Context.MODE_PRIVATE)
                .edit()
                .putString("notifications", "[]")
                .apply();

        Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
        startActivity(intent);
        finish();
    }
}
