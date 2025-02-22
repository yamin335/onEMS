package onair.onems.fee;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import onair.onems.R;

public class FeesHistory extends AppCompatActivity {
    Spinner spinner,type_spinner;
    String[][] DATA_TO_SHOW;
    String YearlyFeesDetailsUrl;
    long sectionID,classID,shiftID,mediumID,instituteID,depertmentID;
    String RFID,UserID;
    SharedPreferences prefs;
    int monthselectindex;
    ProgressDialog dialog;
    double total_fees[],balance[],payment[],fees[],fine[],discount[],scholarship[],total_fees_amount,previous_balance[],total_payment,total_balance;
    String year;
    int monthID[], UserTypeID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fees_history);
//        tableView = (TableView) findViewById(R.id.tableView);
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        UserTypeID = prefs.getInt("UserTypeID",0);
//        RFID= prefs.getString("RFID","");
//        shiftID= prefs.getLong("ShiftID",0);
//        mediumID= prefs.getLong("MediumID",0);
//        classID= prefs.getLong("ClassID",0);
//        sectionID= prefs.getLong("SectionID",0);
//        instituteID=prefs.getLong("InstituteID",0);
//        monthselectindex= prefs.getInt("monthselectindex",0);
//        if(UserTypeID == 3) {
//            depertmentID=prefs.getLong("SDepartmentID",0);
//        } else {
//            depertmentID = prefs.getLong("DepartmentID",0);
//        }
//        year=prefs.getString("yearselectindex","");
//        UserID=prefs.getString("UserID","");
//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Loading....");
//        dialog.show();
//        simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(this, "Month","Total Fees", "Payment","Balance");
//        simpleTableHeaderAdapter.setTextColor(ContextCompat.getColor(this, R.color.table_header_text_total));
//        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
//        int colorEvenRows = getResources().getColor(R.color.table_data_row_even);
//        int colorOddRows = getResources().getColor(R.color.table_data_row_odd);
//        tableView.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(colorEvenRows, colorOddRows));
//        config = getResources().getConfiguration();
//        tableView.addDataClickListener(new TableDataClickListener()
//        {
//            @Override
//            public void onDataClicked(int rowIndex, Object clickedData)
//            {
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("fee",Double.toString(fees[rowIndex]));
//                editor.putString("fine",Double.toString(fine[rowIndex]));
//                editor.putString("scholarship",Double.toString(scholarship[rowIndex]));
//                editor.putString("discount",Double.toString(discount[rowIndex]));
//                editor.putString("due",Double.toString(previous_balance[rowIndex]));
//                editor.putString("payment",Double.toString(payment[rowIndex]));
//                editor.putString("balance",Double.toString(balance[rowIndex]));
//                editor.putString("total_amount",Double.toString(total_fees[rowIndex]));
//                editor.commit();
//                Intent mainIntent = new Intent(FeesHistory.this,MontlyHistory.class);
//                FeesHistory.this.startActivity(mainIntent);
//            }
//        });
//        type_spinner = (MaterialSpinner) findViewById(R.id.spinner_year);
//        ArrayList al = new ArrayList();
//        al.add("2018");
//        al.add("2019");
//        al.add("2020");
//        al.add("2021");
//        al.add("2022");
//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, al);
//        type_spinner.setAdapter(adapter);
//        YearlyFeesDetailsUrl = getString(R.string.baseUrl)+"/api/onEms/getStudentYearlyFeesSummeryInfo/"+instituteID+"/"+year+"/"+UserID;
//        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        StringRequest stringRequest = new StringRequest(Request.Method.GET,   YearlyFeesDetailsUrl,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        parseYearlyFeesDetailsJsonData(response);
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                dialog.dismiss();
//            }
//        });
//
//        queue.add(stringRequest);
//
//        type_spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener<String>()
//        {
//
//            @Override public void onItemSelected(Spinner view, int position, long id, String item)
//            {
//
//               year=item;
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("yearselectindex",year);
//                editor.commit();
//                YearlyFeesDetailsUrl = getString(R.string.baseUrl)+"/api/onEms/getStudentYearlyFeesSummeryInfo/"+instituteID+"/"+year+"/"+UserID;
//                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//                StringRequest stringRequest = new StringRequest(Request.Method.GET,   YearlyFeesDetailsUrl,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//
//                                parseYearlyFeesDetailsJsonData(response);
//
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                        dialog.dismiss();
//                    }
//                });
//
//                queue.add(stringRequest);
//
//
//
//            }
//        });
//
//        config = getResources().getConfiguration();
    }
    void  parseYearlyFeesDetailsJsonData(String jsonString)
    {
        total_fees_amount=0;total_payment=0;total_balance=0;
        try
        {
            JSONArray jsonArray = new JSONArray(jsonString);
            ArrayList al = new ArrayList();
            DATA_TO_SHOW = new String[jsonArray.length()][4];
            monthID=new int[jsonArray.length()];
            fees= new double[jsonArray.length()];
            payment=new double[jsonArray.length()];
            balance=new double[jsonArray.length()];
            previous_balance=new double[jsonArray.length()];
            fine=new double[jsonArray.length()];
            scholarship=new double[jsonArray.length()];
            discount=new double[jsonArray.length()];
            total_fees=new double[jsonArray.length()];
            for(int i = 0; i < jsonArray.length(); ++i)
            {
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                DATA_TO_SHOW[i][0]= (jsonObject.getString("Month")).substring(0,3);
                fees[i]=jsonObject.getDouble("fee");
                fine[i]=jsonObject.getDouble("Fine");
                discount[i]=jsonObject.getDouble("Discount");
                scholarship[i]=jsonObject.getDouble("Scholarship");
                previous_balance[i]=jsonObject.getDouble("PreviousBalance");
                total_fees[i]=fees[i]+fine[i]-scholarship[i]-discount[i]-previous_balance[i];
                total_fees_amount=total_fees_amount+total_fees[i];
                DATA_TO_SHOW [i][1]=total_fees[i]+" TK";
                DATA_TO_SHOW [i][2]=jsonObject.getString("TotalPayment")+" TK";
                payment[i]=jsonObject.getDouble("TotalPayment");
                total_payment= total_payment+payment[i];
                DATA_TO_SHOW [i][3]=jsonObject.getString("BalanceAmount")+" TK";
                total_balance=jsonObject.getDouble("BalanceAmount");
                balance[i]=total_balance;
                al.add(DATA_TO_SHOW[i][0]);
                al.add(DATA_TO_SHOW[i][1]);
                al.add(DATA_TO_SHOW[i][2]);
                al.add(DATA_TO_SHOW[i][3]);

            }




        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_LONG).show();
        }

        dialog.dismiss();
    }
    // Back parent Page code
    public boolean onOptionsItemSelected(MenuItem item){
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
        return true;

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();

    }

    // Back parent Page code end

}
