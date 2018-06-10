package onair.onems.homework;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import onair.onems.R;
import onair.onems.Services.StaticHelperClass;
import onair.onems.attendance.TakeAttendance;
import onair.onems.mainactivities.SideNavigationMenuParentActivity;
import onair.onems.mainactivities.StudentMainScreen;
import onair.onems.mainactivities.TeacherMainScreen;
import onair.onems.network.MySingleton;
import onair.onems.syllabus.DigitalContentAdapter;
import onair.onems.syllabus.ExamSelectionDialog;
import onair.onems.syllabus.SyllabusMainScreen;

import static onair.onems.Services.StaticHelperClass.isNetworkAvailable;

public class HomeworkMainScreen extends SideNavigationMenuParentActivity implements HomeworkAdapter.HomeworkAdapterListener,
        DigitalContentAdapter.AddFileToDownloader{

    private ArrayList<JSONObject> homeworkList;
    private ArrayList<JSONObject> digitalContentList;
    private HomeworkAdapter mAdapter;
    private ProgressDialog mHomeworkDialog;
    private TextView error, errorDigital, homeworkDate, topic, details;
    private DatePickerDialog datePickerDialog;
    private Button datePicker;
    private DigitalContentAdapter mDigitalAdapter;
    private ArrayList<Long> refIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityName = HomeworkMainScreen.class.getName();

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.homework_main_screen, null);
        LinearLayout parent = (LinearLayout) findViewById(R.id.contentMain);
        parent.addView(rowView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        error = findViewById(R.id.homeworkEmpty);
        errorDigital = findViewById(R.id.empty);
        homeworkDate = findViewById(R.id.date);
        topic = findViewById(R.id.topicValue);
        details = findViewById(R.id.detailsValue);

        homeworkList = new ArrayList<>();
        digitalContentList = new ArrayList<>();

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String today = df.format(date);

        mAdapter = new HomeworkAdapter(this, homeworkList, this);
        RecyclerView recyclerView = findViewById(R.id.homeworkRecycler);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mDigitalAdapter = new DigitalContentAdapter(this, this, digitalContentList, DigitalContentAdapter.ContentType.HOMEWORK);
        RecyclerView recycler = findViewById(R.id.recycler);
        RecyclerView.LayoutManager mDigitalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setLayoutManager(mDigitalLayoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(mDigitalAdapter);

        datePicker = (Button)findViewById(R.id.pickDate);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(HomeworkMainScreen.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
//                                selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
//                                selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                String date = (monthOfYear + 1)+"-"+dayOfMonth+"-"+year;
                                datePicker.setText(date);
                                homeworkDate.setText(date);
                                homeworkGetRequest(date);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        homeworkGetRequest(today);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(UserTypeID == 1) {
            Intent mainIntent = new Intent(HomeworkMainScreen.this, TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 2) {
            Intent mainIntent = new Intent(HomeworkMainScreen.this, TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 3) {
            Intent mainIntent = new Intent(HomeworkMainScreen.this, StudentMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 4) {
            Intent mainIntent = new Intent(HomeworkMainScreen.this, TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 5) {
            Intent mainIntent = new Intent(HomeworkMainScreen.this, StudentMainScreen.class);
            startActivity(mainIntent);
            finish();
        }
    }

    @Override
    public void onHomeworkSelected(JSONObject homework) {
        try {
            topic.setText(homework.getString("Topic"));
            details.setText(homework.getString("TopicDetails"));
            homeworkDigitalContentGetRequest(homework.getString("HomeWorkID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void homeworkGetRequest(String date) {
        if (StaticHelperClass.isNetworkAvailable(this)) {

            String homeworkUrl = "";
            if(UserTypeID == 3) {
                homeworkUrl = getString(R.string.baseUrl)+"/api/onEms/getMyInsHomeWork/"+InstituteID+
                        "/"+LoggedUserMediumID+"/"+LoggedUserClassID+"/"+LoggedUserDepartmentID+"/"+
                        LoggedUserSectionID+"/"+LoggedUserShiftID+"/"+date;
            } else if(UserTypeID == 5) {
                try {
                    JSONObject selectedStudent = new JSONObject(getSharedPreferences("CURRENT_STUDENT", Context.MODE_PRIVATE)
                            .getString("guardianSelectedStudent", "{}"));
                    homeworkUrl = getString(R.string.baseUrl)+"/api/onEms/getMyInsHomeWork/"+InstituteID+
                            "/"+selectedStudent.getString("MediumID")+"/"+selectedStudent.getString("ClassID")
                            +"/"+selectedStudent.getString("DepartmentID")+"/"+selectedStudent.getString("SectionID")
                            +"/"+selectedStudent.getString("ShiftID")+"/"+date;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mHomeworkDialog = new ProgressDialog(this);
            mHomeworkDialog.setTitle("Loading homework...");
            mHomeworkDialog.setMessage("Please Wait...");
            mHomeworkDialog.setCancelable(false);
            mHomeworkDialog.setIcon(R.drawable.onair);
            mHomeworkDialog.show();

            //Preparing homework
            StringRequest request = new StringRequest(Request.Method.GET, homeworkUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            parseHomeworkData(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mHomeworkDialog.dismiss();
                    Toast.makeText(HomeworkMainScreen.this,"Homework data not found!!! ",
                            Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Authorization", "Request_From_onEMS_Android_app");
                    return params;
                }
            };
            MySingleton.getInstance(this).addToRequestQueue(request);
        } else {
            Toast.makeText(this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void parseHomeworkData(String homeworkData) {
        if(!homeworkData.equalsIgnoreCase("[]")) {
            error.setVisibility(View.GONE);
            try {
                JSONArray jsonArray = new JSONArray(homeworkData);
                homeworkList.clear();
                for (int i = 0; i<jsonArray.length(); i++) {
                    homeworkList.add(jsonArray.getJSONObject(i));
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            homeworkList.clear();
            mAdapter.notifyDataSetChanged();
            error.setVisibility(View.VISIBLE);
            Toast.makeText(this,"Homework not found!!! ",
                    Toast.LENGTH_LONG).show();
        }
        mHomeworkDialog.dismiss();
    }

    private void homeworkDigitalContentGetRequest(String HomeWorkID) {
        if (StaticHelperClass.isNetworkAvailable(this)) {

            String digitalContentUrl = getString(R.string.baseUrl)+"/api/onEms/getMyInsHomeWorkDetail/"+HomeWorkID;

            mHomeworkDialog = new ProgressDialog(this);
            mHomeworkDialog.setTitle("Loading digital content...");
            mHomeworkDialog.setMessage("Please Wait...");
            mHomeworkDialog.setCancelable(false);
            mHomeworkDialog.setIcon(R.drawable.onair);
            mHomeworkDialog.show();

            //Preparing digital content
            StringRequest request = new StringRequest(Request.Method.GET, digitalContentUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            parseDigitalContent(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mHomeworkDialog.dismiss();
                    Toast.makeText(HomeworkMainScreen.this,"Digital content not found!!! ",
                            Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Authorization", "Request_From_onEMS_Android_app");
                    return params;
                }
            };
            MySingleton.getInstance(this).addToRequestQueue(request);
        } else {
            Toast.makeText(this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void parseDigitalContent(String digitalContent) {
        if(!digitalContent.equalsIgnoreCase("[]")) {
            errorDigital.setVisibility(View.GONE);
            try {
                JSONArray jsonArray = new JSONArray(digitalContent);
                digitalContentList.clear();
                for (int i = 0; i<jsonArray.length(); i++) {
                    digitalContentList.add(jsonArray.getJSONObject(i));
                }
                mDigitalAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            digitalContentList.clear();
            mDigitalAdapter.notifyDataSetChanged();
            errorDigital.setVisibility(View.VISIBLE);
            Toast.makeText(this,"Digital content not found!!! ",
                    Toast.LENGTH_LONG).show();
        }
        mHomeworkDialog.dismiss();
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            // get the refId from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // Open downloaded file
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(referenceId);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
                if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
//                    try {
//                        downloadManager.openDownloadedFile(referenceId);
//                        openFile(downloadLocalUri);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
            cursor.close();

            // remove it from our list
            refIdList.remove(referenceId);

            // if list is empty means all downloads completed
            if (refIdList.isEmpty()) {
                // show a notification
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(HomeworkMainScreen.this)
                                .setSmallIcon(R.drawable.onair)
                                .setContentTitle("Download Completed")
                                .setContentText("All Downloads are completed");


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());
            }
        }
    };


    private boolean isPermissionAllowed() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void downloadFile(String url) {
        if(isNetworkAvailable(this)){
            if(isPermissionAllowed()){
                try {
                    String s[] = url.split("/");
                    String file = s[s.length-1];
                    String fileName = file.split("\\.")[0]+getFileExtension(url);

                    // get download service
                    DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(getString(R.string.baseUrl)+"/"+url));
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    request.setVisibleInDownloadsUi(true);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    request.setDescription("Some descrition");
                    request.setTitle(fileName);
                    // in order for this if to run, you must use the android 3.2 to compile your app
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }
                    long refId = manager.enqueue(request);
                    refIdList.add(refId);

                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "You denied to download!!!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please check your INTERNET connection!!!", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private String getFileExtension(String string) {
        if(string.contains(".png")||string.contains(".PNG")){
            return ".png";
        } else if(string.contains(".jpg")||string.contains(".JPG")){
            return ".jpg";
        } else if(string.contains(".jpeg")||string.contains(".JPEG")){
            return ".jpeg";
        } else if(string.contains(".gif")||string.contains(".GIF")){
            return ".gif";
        } else if(string.contains(".bmp")||string.contains(".BMP")){
            return ".bmp";
        } else if(string.contains(".mp3")||string.contains(".MP3")){
            return ".mp3";
        } else if(string.contains(".amr")||string.contains(".AMR")){
            return ".amr";
        } else if(string.contains(".wav")||string.contains(".WAV")){
            return ".wav";
        } else if(string.contains(".aac")||string.contains(".AAC")){
            return ".aac";
        } else if(string.contains(".mp4")||string.contains(".MP4")){
            return ".mp4";
        } else if(string.contains(".wmv")||string.contains(".WMV")){
            return ".wmv";
        } else if(string.contains(".avi")||string.contains(".AVI")){
            return ".avi";
        } else if(string.contains(".flv")||string.contains(".FLV")){
            return ".flv";
        } else if(string.contains(".mov")||string.contains(".MOV")){
            return ".mov";
        } else if(string.contains(".vob")||string.contains(".VOB")){
            return ".vob";
        } else if(string.contains(".mpeg")||string.contains(".MPEG")){
            return ".mpeg";
        } else if(string.contains(".3gp")||string.contains(".3GP")){
            return ".3gp";
        } else if(string.contains(".mpg")||string.contains(".MPG")){
            return ".mpg";
        } else if(string.contains(".wmv")||string.contains(".WMV")){
            return ".wmv";
        } else if(string.contains(".octet-stream")){
            return ".rar";
        } else if(string.contains(".vnd.openxmlformats-officedocument.wo")){
            return ".doc";
        } else if(string.contains(".plain")){
            return ".txt";
        } else if(string.contains(".vnd.openxmlformats-officedocument.sp")){
            return ".xls";
        } else if(string.contains(".x-zip-compressed")){
            return ".zip";
        } else if(string.contains(".vnd.openxmlformats-officedocument.presentationml.p")){
            return ".ppt";
        } else if(string.contains(".pdf")||string.contains(".PDF")){
            return ".pdf";
        } else {
            return "UnknownFileType";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }
}
