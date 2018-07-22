package onair.onems.attendance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;
import onair.onems.R;
import onair.onems.Services.GlideApp;
import onair.onems.Services.StaticHelperClass;
import onair.onems.models.MonthModel;
import onair.onems.models.DailyAttendanceModel;
import onair.onems.network.MySingleton;

public class SelfAttendanceReport extends Fragment {
    private TableView tableView;
    private Spinner spinnerMonth;
    private String UserID="";
    private ProgressDialog dialog;
    private Configuration config;
    private long SectionID, ClassID, ShiftID, MediumID, DepartmentID, InstituteID;
    private SimpleTableHeaderAdapter simpleTableHeaderAdapter;
    private ArrayList<MonthModel> allMonthArrayList;
    private String[] tempMonthArray = {"Select Month"};
    private MonthModel selectedMonth = null;
    private ArrayList<DailyAttendanceModel> dailyAttendanceList;
    private DailyAttendanceModel selectedDay;
    private int UserTypeID;
    private TextView totalClass, totalPresent;
    private String UserFullName, RollNo, RFID, ImageUrl = "";
    public SelfAttendanceReport()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.attendance_report_self_attendance, container, false);
        tableView = (TableView)rootView.findViewById(R.id.tableView);
        totalClass = rootView.findViewById(R.id.totalClass);
        totalPresent = rootView.findViewById(R.id.totalPresent);
        ImageView studentImage = rootView.findViewById(R.id.studentImage);
        TextView name = rootView.findViewById(R.id.name);
        TextView roll = rootView.findViewById(R.id.roll);
        TextView id = rootView.findViewById(R.id.Id);
        tableView.setColumnCount(4);

        SharedPreferences sharedPre = PreferenceManager.getDefaultSharedPreferences(getActivity());
        UserTypeID = sharedPre.getInt("UserTypeID",0);
        InstituteID=sharedPre.getLong("InstituteID",0);
        if(UserTypeID == 3) {
            DepartmentID=sharedPre.getLong("SDepartmentID",0);
            ShiftID=sharedPre.getLong("ShiftID",0);
            MediumID=sharedPre.getLong("MediumID",0);
            ClassID=sharedPre.getLong("ClassID",0);
            SectionID=sharedPre.getLong("SectionID",0);
            UserFullName = sharedPre.getString("UserFullName","");
            RollNo = sharedPre.getString("RollNo","");
            RFID = sharedPre.getString("RFID","");
            ImageUrl = sharedPre.getString("ImageUrl","");
            name.setText(UserFullName);
            roll.setText("Roll: "+RollNo);
            id.setText("ID: "+RFID);
        } else if(UserTypeID == 5) {
//            DepartmentID=sharedPre.getLong("DepartmentID",0);
            try {
                JSONObject selectedStudent = new JSONObject(getActivity().getSharedPreferences("CURRENT_STUDENT", Context.MODE_PRIVATE)
                        .getString("guardianSelectedStudent", "{}"));
                if(selectedStudent.getString("ShiftID").equalsIgnoreCase("null")||selectedStudent.getString("ShiftID").equalsIgnoreCase("")) {
                    ShiftID = 0;
                } else {
                    ShiftID = Long.parseLong(selectedStudent.getString("ShiftID"));
                }
                if(selectedStudent.getString("MediumID").equalsIgnoreCase("null")||selectedStudent.getString("MediumID").equalsIgnoreCase("")) {
                    MediumID = 0;
                } else {
                    MediumID = Long.parseLong(selectedStudent.getString("MediumID"));
                }
                if(selectedStudent.getString("ClassID").equalsIgnoreCase("null")||selectedStudent.getString("ClassID").equalsIgnoreCase("")) {
                    ClassID = 0;
                } else {
                    ClassID = Long.parseLong(selectedStudent.getString("ClassID"));
                }
                if(selectedStudent.getString("SectionID").equalsIgnoreCase("null")||selectedStudent.getString("SectionID").equalsIgnoreCase("")) {
                    SectionID = 0;
                } else {
                    SectionID = Long.parseLong(selectedStudent.getString("SectionID"));
                }
                if(selectedStudent.getString("DepartmentID").equalsIgnoreCase("null")||selectedStudent.getString("DepartmentID").equalsIgnoreCase("")) {
                    DepartmentID = 0;
                } else {
                    DepartmentID = Long.parseLong(selectedStudent.getString("DepartmentID"));
                }
                UserID = selectedStudent.getString("UserID");
                UserFullName = selectedStudent.getString("UserFullName");
                RollNo = selectedStudent.getString("RollNo");
                RFID = selectedStudent.getString("RFID");
                ImageUrl = selectedStudent.getString("ImageUrl");
                name.setText(UserFullName);
                roll.setText("Roll: "+RollNo);
                id.setText("ID: "+RFID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        GlideApp.with(this)
                .load(getString(R.string.baseUrl)+"/"+ImageUrl.replace("\\","/")).apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(studentImage);

        selectedMonth = new MonthModel();

        spinnerMonth = (Spinner)rootView.findViewById(R.id.spinnerMonth);
        ArrayAdapter<String> month_spinner_adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, tempMonthArray);
        month_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(month_spinner_adapter);

        dialog = new ProgressDialog(getActivity());
        dialog.setTitle("Loading...");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setIcon(R.drawable.onair);
        dialog.show();

        simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(getActivity(),"SI","Date","Present", "Late(min)");

        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(getActivity(), R.color.table_header_text ));
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
        config = getResources().getConfiguration();

        if (config.smallestScreenWidthDp >320) {
            simpleTableHeaderAdapter.setTextSize(14);
        } else {
            simpleTableHeaderAdapter.setTextSize(10);
        }
        int colorEvenRows = getResources().getColor(R.color.table_data_row_even);
        int colorOddRows = getResources().getColor(R.color.table_data_row_odd);
        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(colorEvenRows, colorOddRows));
        TableColumnWeightModel columnModel = new TableColumnWeightModel(4);
        columnModel.setColumnWeight(1, 2);
        tableView.setColumnModel(columnModel);

        tableView.addDataClickListener(new TableDataClickListener() {
            @Override
            public void onDataClicked(int rowIndex, Object clickedData)
            {
                selectedDay = dailyAttendanceList.get(rowIndex);
                Intent intent = new Intent(getActivity(), StudentSubjectWiseAttendance.class);
                intent.putExtra("UserID", UserID);
                intent.putExtra("ShiftID", ShiftID);
                intent.putExtra("MediumID", MediumID);
                intent.putExtra("ClassID", ClassID);
                intent.putExtra("DepartmentID", DepartmentID);
                intent.putExtra("SectionID", SectionID);
                intent.putExtra("Date", selectedDay.getDate());
                startActivity(intent);
            }
        });

        MonthDataGetRequest();

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                try {
                    selectedMonth = allMonthArrayList.get(position);
                    MonthlyAttendanceDataGetRequest(selectedMonth.getMonthID());
                } catch (Exception e) {
                    Toast.makeText(getActivity(),"No section found !!!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }

    void parseMonthJsonData(String jsonString) {

        try {
            allMonthArrayList = new ArrayList<>();
            JSONArray MonthJsonArray = new JSONArray(jsonString);
            ArrayList<String> monthArrayList = new ArrayList<>();
            //monthArrayList.add("Select Month");
            for(int i = 0; i < MonthJsonArray.length(); ++i)
            {
                JSONObject monthJsonObject = MonthJsonArray.getJSONObject(i);
                MonthModel monthModel = new MonthModel(monthJsonObject.getString("MonthID"), monthJsonObject.getString("MonthName"));
                allMonthArrayList.add(monthModel);
                monthArrayList.add(monthModel.getMonthName());
            }

            try {
                String[] strings = new String[monthArrayList.size()];
                strings = monthArrayList.toArray(strings);
                ArrayAdapter<String> month_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.spinner_item, strings);
                month_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMonth.setAdapter(month_spinner_adapter);
                Calendar c = Calendar.getInstance();
                spinnerMonth.setSelection(c.get(Calendar.MONTH));
                dialog.dismiss();
            }
            catch (IndexOutOfBoundsException e)
            {
                dialog.dismiss();
                Toast.makeText(getActivity(),"No class found !!!",Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Toast.makeText(getActivity(),""+e,Toast.LENGTH_LONG).show();
            dialog.dismiss();

        }

    }

    void parseMonthlyAttendanceJsonData(String jsonString) {
        try {
            dailyAttendanceList = new ArrayList<>();
            JSONArray dailyAttendanceJsonArray = new JSONArray(jsonString);
            String[][] DATA_TO_SHOW = new String[dailyAttendanceJsonArray.length()][4];
            for(int i = 0; i < dailyAttendanceJsonArray.length(); ++i) {
                JSONObject dailyAttendanceJsonObject = dailyAttendanceJsonArray.getJSONObject(i);
                if(i == 0) {
                    totalClass.setText("Total class: "+dailyAttendanceJsonObject.getString("TotalClassDay"));
                    totalPresent.setText("Total present: "+dailyAttendanceJsonObject.getString("TotalPresent"));
                }
                DailyAttendanceModel perDayAttendance = new DailyAttendanceModel();
                perDayAttendance.setDate(dailyAttendanceJsonObject.getString("Date"));
                perDayAttendance.setPresent(dailyAttendanceJsonObject.getString("Present"));
                perDayAttendance.setLate(dailyAttendanceJsonObject.getString("Late"));
                perDayAttendance.setTotalClassDay(dailyAttendanceJsonObject.getString("TotalClassDay"));
                perDayAttendance.setTotalPresent(dailyAttendanceJsonObject.getString("TotalPresent"));
                DATA_TO_SHOW[i][0] = String.valueOf((i+1));
                DATA_TO_SHOW [i][1] = perDayAttendance.getDate();
                DATA_TO_SHOW[i][2] = perDayAttendance.getPresent() == 1 ? "YES" : "NO";
                DATA_TO_SHOW [i][3] = Integer.toString(perDayAttendance.getLate());
                dailyAttendanceList.add(perDayAttendance);
            }

            SimpleTableDataAdapter simpleTabledataAdapter = new SimpleTableDataAdapter(getActivity(),DATA_TO_SHOW);
            tableView.setDataAdapter(simpleTabledataAdapter);
            if (config.smallestScreenWidthDp >320) {
                simpleTableHeaderAdapter.setTextSize(14);
                simpleTabledataAdapter.setTextSize(12);
            } else {
                simpleTableHeaderAdapter.setTextSize(10);
                simpleTabledataAdapter.setTextSize(10);
            }
            dialog.dismiss();
        } catch (JSONException e) {
            dialog.dismiss();
            Toast.makeText(getActivity(),""+e,Toast.LENGTH_LONG).show();
        }
    }

    void MonthDataGetRequest(){
        if(StaticHelperClass.isNetworkAvailable(getActivity())) {
            dialog.show();
            String monthUrl=getString(R.string.baseUrl)+"/api/onEms/getMonth";
            StringRequest stringMonthRequest = new StringRequest(Request.Method.GET, monthUrl,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {

                            parseMonthJsonData(response);

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
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
            MySingleton.getInstance(getActivity()).addToRequestQueue(stringMonthRequest);
        } else {
            Toast.makeText(getActivity(),"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void MonthlyAttendanceDataGetRequest(int MonthID){
        if(StaticHelperClass.isNetworkAvailable(getActivity())) {
            dialog.show();
            String monthAttendanceUrl = getString(R.string.baseUrl)+"/api/onEms/getStudentMonthlyDeviceAttendance/"+
                    ShiftID+"/"+MediumID+"/"+ClassID+"/"+SectionID+"/"+DepartmentID+"/"+ MonthID+"/"+UserID+"/"+InstituteID;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, monthAttendanceUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            parseMonthlyAttendanceJsonData(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
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
            MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
        } else {
            Toast.makeText(getActivity(),"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }
}