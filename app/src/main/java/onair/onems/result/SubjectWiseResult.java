package onair.onems.result;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import onair.onems.R;
import onair.onems.Services.StaticHelperClass;
import onair.onems.customised.MyDividerItemDecoration;
import onair.onems.mainactivities.CommonToolbarParentActivity;
import onair.onems.network.MySingleton;

public class SubjectWiseResult extends CommonToolbarParentActivity implements SubjectWiseResultAdapter.SubjectWiseResultsAdapterListener{

    private RecyclerView recyclerView;
    private List<JSONObject> subjectWiseResultList;
    private SubjectWiseResultAdapter mAdapter;
    private ProgressDialog mResultDialog;
    private String UserID, ShiftID, MediumID, ClassID, DepartmentID, SectionID, SessionID, ExamID, InstituteID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View childActivityLayout = inflater.inflate(R.layout.result_subject_wise, null);
        LinearLayout parentActivityLayout = findViewById(R.id.contentMain);
        parentActivityLayout.addView(childActivityLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        Intent intent = getIntent();
        UserID = intent.getStringExtra("UserID");
        ShiftID = intent.getStringExtra("ShiftID");
        MediumID = intent.getStringExtra("MediumID");
        ClassID = intent.getStringExtra("ClassID");
        DepartmentID = intent.getStringExtra("DepartmentID");
        SectionID = intent.getStringExtra("SectionID");
        SessionID = intent.getStringExtra("SessionID");
        ExamID = intent.getStringExtra("ExamID");
        InstituteID = intent.getStringExtra("InstituteID");

        recyclerView = findViewById(R.id.recycler_view);


        ResultDataGetRequest();

    }

    @Override
    public void onSubjectWiseResultSelected(JSONObject subjectWiseResul) {
        try {
            if(!subjectWiseResul.getString("SubjectName").equalsIgnoreCase("Total")) {
                ResultDetailsDialog customDialog = new ResultDetailsDialog(this, subjectWiseResul);
                customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customDialog.setCancelable(false);
                customDialog.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ResultDataGetRequest() {
        if (StaticHelperClass.isNetworkAvailable(this)) {

            String resultDataGetUrl = getString(R.string.baseUrl)+"/api/onEms/SubjectWiseMarksByStudent/"
                    +UserID+"/"+InstituteID+"/"+ClassID+"/"+SectionID+"/"+DepartmentID+"/"+MediumID
                    +"/"+ShiftID+"/"+SessionID+"/"+ExamID;

            mResultDialog = new ProgressDialog(this);
            mResultDialog.setTitle("Loading session...");
            mResultDialog.setMessage("Please Wait...");
            mResultDialog.setCancelable(false);
            mResultDialog.setIcon(R.drawable.onair);

            //Preparing Shift data from server
            StringRequest stringResultRequest = new StringRequest(Request.Method.GET, resultDataGetUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            prepareResult(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mResultDialog.dismiss();
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
            MySingleton.getInstance(this).addToRequestQueue(stringResultRequest);
        } else {
            Toast.makeText(SubjectWiseResult.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void prepareResult(String result) {
        try {
            subjectWiseResultList = new ArrayList<>();
            JSONArray resultJsonArray = new JSONArray(result);
            int totalMarks = 0;
            double totalGradePoint = 0.0;
            String totalGrade = "";
            int totalSubject = 0;

            for(int i = 0; i<resultJsonArray.length(); i++) {
                if(resultJsonArray.getJSONObject(i).getString("IsOptional").equalsIgnoreCase("true")
                        && (resultJsonArray.getJSONObject(i).getDouble("GradePoint")>2.0)) {

                    subjectWiseResultList.add(resultJsonArray.getJSONObject(i));
                    totalGradePoint += resultJsonArray.getJSONObject(i).getDouble("GradePoint")-2.0;
                    totalMarks+= resultJsonArray.getJSONObject(i).getInt("Total");

                } else if(resultJsonArray.getJSONObject(i).getString("IsOptional").equalsIgnoreCase("false")){

                    totalSubject++;
                    subjectWiseResultList.add(resultJsonArray.getJSONObject(i));
                    totalGradePoint += resultJsonArray.getJSONObject(i).getDouble("GradePoint");
                    totalMarks+= resultJsonArray.getJSONObject(i).getInt("Total");

                }
            }

            if(totalSubject!=0) {
                totalGradePoint/=totalSubject;
                totalGradePoint = new BigDecimal(totalGradePoint).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }

            if((totalGradePoint>5.0)||(totalGradePoint==5.0)) {
                totalGrade = "A+";
            } else if((totalGradePoint>=4.0)&&(totalGradePoint<5.0)) {
                totalGrade = "A";
            }else if((totalGradePoint>=3.5)&&(totalGradePoint<4.0)) {
                totalGrade = "A-";
            }else if((totalGradePoint>=3.0)&&(totalGradePoint<3.5)) {
                totalGrade = "B";
            }else if((totalGradePoint>=2.0)&&(totalGradePoint<3.0)) {
                totalGrade = "C";
            }else if((totalGradePoint>=1.0)&&(totalGradePoint<2.0)) {
                totalGrade = "D";
            }else {
                totalGrade = "F";
            }
            JSONObject totalResult = new JSONObject();
            totalResult.put("SubjectName", "Total");
            totalResult.put("Total",Integer.toString(totalMarks));
            totalResult.put("Grade", totalGrade);
            totalResult.put("GradePoint", totalGradePoint);
            subjectWiseResultList.add(totalResult);

            mAdapter = new SubjectWiseResultAdapter(this, subjectWiseResultList, this);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 10));
            recyclerView.setAdapter(mAdapter);
            mResultDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
            mResultDialog.dismiss();
        }
    }
}
