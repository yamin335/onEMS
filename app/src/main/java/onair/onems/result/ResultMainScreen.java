package onair.onems.result;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import onair.onems.R;
import onair.onems.Services.RetrofitNetworkService;
import onair.onems.Services.StaticHelperClass;
import onair.onems.mainactivities.SideNavigationMenuParentActivity;
import onair.onems.mainactivities.StudentMainScreen;
import onair.onems.mainactivities.TeacherMainScreen;
import onair.onems.models.ClassModel;
import onair.onems.models.DepartmentModel;
import onair.onems.models.ExamModel;
import onair.onems.models.MediumModel;
import onair.onems.models.SectionModel;
import onair.onems.models.SessionModel;
import onair.onems.models.ShiftModel;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ResultMainScreen extends SideNavigationMenuParentActivity{

    private Spinner spinnerClass, spinnerShift, spinnerSection,
            spinnerMedium, spinnerDepartment, spinnerStudent,
            spinnerSession, spinnerExam;

    private ArrayList<ClassModel> allClassArrayList;
    private ArrayList<ShiftModel> allShiftArrayList;
    private ArrayList<SectionModel> allSectionArrayList;
    private ArrayList<MediumModel> allMediumArrayList;
    private ArrayList<DepartmentModel> allDepartmentArrayList;
    private ArrayList<SessionModel> allSessionArrayList;
    private ArrayList<ExamModel> allExamArrayList;

    private String[] tempClassArray = {"Select Class"};
    private String[] tempShiftArray = {"Select Shift"};
    private String[] tempSectionArray = {"Select Section"};
    private String[] tempDepartmentArray = {"Select Department"};
    private String[] tempMediumArray = {"Select Medium"};
    private String[] tempStudentArray = {"Select Student"};
    private String[] tempSessionArray = {"Select Session"};
    private String[] tempExamArray = {"Select Exam"};

    private ClassModel selectedClass;
    private ShiftModel selectedShift;
    private SectionModel selectedSection;
    private MediumModel selectedMedium;
    private DepartmentModel selectedDepartment;
    private SessionModel selectedSession;
    private ExamModel selectedExam;
    private JSONObject selectedStudent;

    private int firstClass = 0, firstShift = 0, firstSection = 0, firstMedium = 0,
            firstDepartment = 0, firstSession = 0, firstExam = 0;
    private JSONArray allStudentJsonArray;
    private CompositeDisposable finalDisposer = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!finalDisposer.isDisposed())
            finalDisposer.dispose();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityName = ResultMainScreen.class.getName();

        if(UserTypeID == 3||UserTypeID==5) {
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View childActivityLayout = Objects.requireNonNull(inflater).inflate(R.layout.result_student_content_main, null);
            LinearLayout parentActivityLayout = findViewById(R.id.contentMain);
            parentActivityLayout.addView(childActivityLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            ViewPager viewPager = findViewById(R.id.pager);
            setupViewPager(viewPager);
            TabLayout tabLayout = findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(viewPager);
        } else if(UserTypeID == 1||UserTypeID == 2||UserTypeID == 4) {
            LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View childActivityLayout = Objects.requireNonNull(inflater).inflate(R.layout.result_content_main, null);
            LinearLayout parentActivityLayout = findViewById(R.id.contentMain);
            parentActivityLayout.addView(childActivityLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            allSessionArrayList = new ArrayList<>();
            allShiftArrayList = new ArrayList<>();
            allMediumArrayList = new ArrayList<>();
            allClassArrayList = new ArrayList<>();
            allDepartmentArrayList = new ArrayList<>();
            allSectionArrayList = new ArrayList<>();
            allExamArrayList = new ArrayList<>();

            selectedClass = new ClassModel();
            selectedShift = new ShiftModel();
            selectedSection = new SectionModel();
            selectedMedium = new MediumModel();
            selectedDepartment = new DepartmentModel();
            selectedSession = new SessionModel();
            selectedExam = new ExamModel();
            selectedStudent = null;

            spinnerClass = findViewById(R.id.spinnerClass);
            spinnerShift = findViewById(R.id.spinnerShift);
            spinnerSection = findViewById(R.id.spinnerSection);
            spinnerMedium = findViewById(R.id.spinnerMedium);
            spinnerDepartment = findViewById(R.id.spinnerDepartment);
            spinnerStudent = findViewById(R.id.spinnerStudent);
            spinnerSession = findViewById(R.id.spinnerSession);
            spinnerExam = findViewById(R.id.spinnerExam);

            Button showResult = findViewById(R.id.showResult);

            ArrayAdapter<String> class_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempClassArray);
            class_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(class_spinner_adapter);

            ArrayAdapter<String> shift_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempShiftArray);
            shift_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerShift.setAdapter(shift_spinner_adapter);

            ArrayAdapter<String> section_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempSectionArray);
            section_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSection.setAdapter(section_spinner_adapter);

            ArrayAdapter<String> department_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempDepartmentArray);
            department_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDepartment.setAdapter(department_spinner_adapter);

            ArrayAdapter<String> medium_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempMediumArray);
            medium_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMedium.setAdapter(medium_spinner_adapter);

            ArrayAdapter<String> student_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempStudentArray);
            student_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStudent.setAdapter(student_spinner_adapter);

            ArrayAdapter<String> session_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempSessionArray);
            session_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSession.setAdapter(session_spinner_adapter);

            ArrayAdapter<String> exam_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempExamArray);
            exam_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExam.setAdapter(exam_spinner_adapter);

            spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedSession = allSessionArrayList.get(position-1);
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No session found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstSession++>1) {
                            selectedSession = new SessionModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedShift = allShiftArrayList.get(position-1);
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No shift found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstShift++>1) {
                            selectedShift = new ShiftModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerMedium.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedMedium = allMediumArrayList.get(position-1);
                            selectedClass = new ClassModel();
                            selectedDepartment = new DepartmentModel();
                            selectedSection = new SectionModel();
                            ClassDataGetRequest();
                            selectedExam = new ExamModel();
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No medium found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstMedium++>1) {
                            selectedMedium = new MediumModel();
                            selectedClass = new ClassModel();
                            selectedDepartment = new DepartmentModel();
                            selectedSection = new SectionModel();
                            selectedExam = new ExamModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedClass = allClassArrayList.get(position-1);
                            selectedDepartment = new DepartmentModel();
                            selectedSection = new SectionModel();
                            DepartmentDataGetRequest();
                            ExamDataGetRequest();
                            selectedExam = new ExamModel();
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No class found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstClass++>1) {
                            selectedClass = new ClassModel();
                            selectedDepartment = new DepartmentModel();
                            selectedSection = new SectionModel();
                            selectedExam = new ExamModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedDepartment = allDepartmentArrayList.get(position-1);
                            selectedSection = new SectionModel();
                            SectionDataGetRequest();
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No shift found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstDepartment++>1) {
                            selectedDepartment = new DepartmentModel();
                            selectedSection = new SectionModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            spinnerSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedSection = allSectionArrayList.get(position-1);
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No section found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstSection++>1) {
                            selectedSection = new SectionModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerExam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            selectedExam = allExamArrayList.get(position-1);
                            GetStudentListPostRequest();
                            selectedStudent = null;
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No Exam found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if(firstExam++>1) {
                            selectedExam = new ExamModel();
                            selectedStudent = null;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(position != 0) {
                        try {
                            try {
                                selectedStudent = allStudentJsonArray.getJSONObject(position-1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(ResultMainScreen.this,"No student found !!!",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        selectedStudent = null;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            showResult.setOnClickListener(v -> {
                if(StaticHelperClass.isNetworkAvailable(ResultMainScreen.this)) {
                    if(allSessionArrayList.size()>0 && selectedSession.getSessionID() == 0){
                        Toast.makeText(ResultMainScreen.this,"Please select session!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(allMediumArrayList.size()>0 && (selectedMedium.getMediumID() == -2 || selectedMedium.getMediumID() == 0)) {
                        Toast.makeText(ResultMainScreen.this,"Please select medium!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(allClassArrayList.size()>0 && (selectedClass.getClassID() == -2 || selectedClass.getClassID() == 0)) {
                        Toast.makeText(ResultMainScreen.this,"Please select class!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(allDepartmentArrayList.size()>0 && (selectedDepartment.getDepartmentID() == -2 || selectedDepartment.getDepartmentID() == 0)) {
                        Toast.makeText(ResultMainScreen.this,"Please select department!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(allSectionArrayList.size()>0 && (selectedSection.getSectionID() == -2 || selectedSection.getSectionID() == 0)) {
                        Toast.makeText(ResultMainScreen.this,"Please select section!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(allExamArrayList.size()>0 && selectedExam.getExamID() == 0) {
                        Toast.makeText(ResultMainScreen.this,"Please select exam!!! ",
                                Toast.LENGTH_LONG).show();
                    } else if(selectedStudent == null){
                        Toast.makeText(ResultMainScreen.this,"Please select a student!!! ",
                                Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            Intent intent = new Intent(ResultMainScreen.this, SubjectWiseResult.class);
                            intent.putExtra("UserID", selectedStudent.getString("UserID").equalsIgnoreCase("null")? "0":selectedStudent.getString("UserID"));
                            intent.putExtra("ShiftID", selectedStudent.getString("ShiftID").equalsIgnoreCase("null")? "0":selectedStudent.getString("ShiftID"));
                            intent.putExtra("MediumID", selectedStudent.getString("MediumID").equalsIgnoreCase("null")? "0":selectedStudent.getString("MediumID"));
                            intent.putExtra("ClassID", selectedStudent.getString("ClassID").equalsIgnoreCase("null")? "0":selectedStudent.getString("ClassID"));
                            intent.putExtra("DepartmentID", selectedStudent.getString("DepartmentID").equalsIgnoreCase("null")? "0":selectedStudent.getString("DepartmentID"));
                            intent.putExtra("SectionID", selectedStudent.getString("SectionID").equalsIgnoreCase("null")? "0":selectedStudent.getString("SectionID"));
                            intent.putExtra("SessionID", selectedStudent.getString("SessionID").equalsIgnoreCase("null")? "0":selectedStudent.getString("SessionID"));
                            intent.putExtra("ExamID", Long.toString(selectedExam.getExamID()));
                            intent.putExtra("InstituteID", selectedStudent.getString("InstituteID").equalsIgnoreCase("null")? "0":selectedStudent.getString("InstituteID"));
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                            Toast.LENGTH_LONG).show();
                }
            });

            SessionDataGetRequest();
            ShiftDataGetRequest();
            MediumDataGetRequest();
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SelfResult(), "SELF RESULT");
        Bundle bundle = new Bundle();
        bundle.putLong("InstituteID", InstituteID);
        bundle.putString("LoggedUserID", LoggedUserID);
        OtherResult otherResult = new OtherResult();
        otherResult.setArguments(bundle);
        adapter.addFragment(otherResult, "OTHERS RESULT");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(UserTypeID == 1) {
            Intent mainIntent = new Intent(ResultMainScreen.this,TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 2) {
            Intent mainIntent = new Intent(ResultMainScreen.this,TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 3) {
            Intent mainIntent = new Intent(ResultMainScreen.this,StudentMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 4) {
            Intent mainIntent = new Intent(ResultMainScreen.this,TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        } else if(UserTypeID == 5) {
            Intent mainIntent = new Intent(ResultMainScreen.this,StudentMainScreen.class);
            startActivity(mainIntent);
            finish();
        }
    }

    private void SessionDataGetRequest() {
        if (StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getallsession()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseSessionJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            Toast.makeText(ResultMainScreen.this,"Session not found!!! ",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseSessionJsonData(String jsonString) {
        try {
            String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
            int yearIndex = 0;
            allSessionArrayList = new ArrayList<>();
            JSONArray sessionJsonArray = new JSONArray(jsonString);
            ArrayList<String> sessionArrayList = new ArrayList<>();
            sessionArrayList.add("Select Session");
            for(int i = 0; i < sessionJsonArray.length(); ++i) {
                JSONObject sessionJsonObject = sessionJsonArray.getJSONObject(i);
                SessionModel sessionModel = new SessionModel(sessionJsonObject.getString("SessionID"), sessionJsonObject.getString("SessionName"));
                allSessionArrayList.add(sessionModel);
                sessionArrayList.add(sessionModel.getSessionName());
                if (year.equalsIgnoreCase(sessionModel.getSessionName())) {
                    yearIndex = i;
                }
            }
            try {
                String[] strings = new String[sessionArrayList.size()];
                strings = sessionArrayList.toArray(strings);
                ArrayAdapter<String> session_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                session_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSession.setAdapter(session_spinner_adapter);
                spinnerSession.setSelection(yearIndex+1);
                selectedSession = allSessionArrayList.get(yearIndex);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No session found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void ShiftDataGetRequest() {
        if (StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getInsShift(InstituteID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseShiftJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseShiftJsonData(String jsonString) {
        try {
            allShiftArrayList = new ArrayList<>();
            JSONArray shiftJsonArray = new JSONArray(jsonString);
            ArrayList<String> shiftArrayList = new ArrayList<>();
            shiftArrayList.add("Select Shift");
            for(int i = 0; i < shiftJsonArray.length(); ++i) {
                JSONObject shiftJsonObject = shiftJsonArray.getJSONObject(i);
                ShiftModel shiftModel = new ShiftModel(shiftJsonObject.getString("ShiftID"), shiftJsonObject.getString("ShiftName"));
                allShiftArrayList.add(shiftModel);
                shiftArrayList.add(shiftModel.getShiftName());
            }
//            if(allShiftArrayList.size() == 1){
//                selectedShift = allShiftArrayList.get(0);
//            }
            try {
                String[] strings = new String[shiftArrayList.size()];
                strings = shiftArrayList.toArray(strings);
                ArrayAdapter<String> shift_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                shift_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerShift.setAdapter(shift_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No shift found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void MediumDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getInstituteMediumDdl(InstituteID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseMediumJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseMediumJsonData(String jsonString) {
        try {
            allMediumArrayList = new ArrayList<>();
            JSONArray mediumJsonArray = new JSONArray(jsonString);
            ArrayList<String> mediumnArrayList = new ArrayList<>();
            mediumnArrayList.add("Select Medium");
            for(int i = 0; i < mediumJsonArray.length(); ++i) {
                JSONObject mediumJsonObject = mediumJsonArray.getJSONObject(i);
                MediumModel mediumModel = new MediumModel(mediumJsonObject.getString("MediumID"), mediumJsonObject.getString("MameName"),
                        mediumJsonObject.getString("IsDefault"));
                allMediumArrayList.add(mediumModel);
                mediumnArrayList.add(mediumModel.getMameName());
            }
//            if(allMediumArrayList.size() == 1){
//                selectedMedium = allMediumArrayList.get(0);
//                ClassDataGetRequest();
//            }
            try {
                String[] strings = new String[mediumnArrayList.size()];
                strings = mediumnArrayList.toArray(strings);
                ArrayAdapter<String> medium_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                medium_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMedium.setAdapter(medium_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No medium found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void ClassDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            CheckSelectedData();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .MediumWiseClassDDL(InstituteID, selectedMedium.getMediumID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseClassJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseClassJsonData(String jsonString) {
        try {
            allClassArrayList = new ArrayList<>();
            JSONArray classJsonArray = new JSONArray(jsonString);
            ArrayList<String> classArrayList = new ArrayList<>();
            classArrayList.add("Select Class");
            for(int i = 0; i < classJsonArray.length(); ++i) {
                JSONObject classJsonObject = classJsonArray.getJSONObject(i);
                ClassModel classModel = new ClassModel(classJsonObject.getString("ClassID"), classJsonObject.getString("ClassName"));
                allClassArrayList.add(classModel);
                classArrayList.add(classModel.getClassName());
            }
//            if(allClassArrayList.size() == 1) {
//                selectedClass = allClassArrayList.get(0);
//                DepartmentDataGetRequest();
//            }
            try {
                String[] strings = new String[classArrayList.size()];
                strings = classArrayList.toArray(strings);
                ArrayAdapter<String> class_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                class_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerClass.setAdapter(class_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No class found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void DepartmentDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            CheckSelectedData();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .ClassWiseDepartmentDDL(InstituteID, selectedClass.getClassID(), selectedMedium.getMediumID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseDepartmentJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseDepartmentJsonData(String jsonString) {
        try {
            allDepartmentArrayList = new ArrayList<>();
            JSONArray departmentJsonArray = new JSONArray(jsonString);
            ArrayList<String> departmentArrayList = new ArrayList<>();
            departmentArrayList.add("Select Department");
            for(int i = 0; i < departmentJsonArray.length(); ++i) {
                JSONObject departmentJsonObject = departmentJsonArray.getJSONObject(i);
                DepartmentModel departmentModel = new DepartmentModel(departmentJsonObject.getString("DepartmentID"), departmentJsonObject.getString("DepartmentName"));
                allDepartmentArrayList.add(departmentModel);
                departmentArrayList.add(departmentModel.getDepartmentName());
            }
//            if(allDepartmentArrayList.size() == 1){
//                selectedDepartment = allDepartmentArrayList.get(0);
//                SectionDataGetRequest();
//            }
            if(allDepartmentArrayList.size() == 0){
                SectionDataGetRequest();
            }
            try {
                String[] strings = new String[departmentArrayList.size()];
                strings = departmentArrayList.toArray(strings);
                ArrayAdapter<String> department_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                department_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDepartment.setAdapter(department_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No department found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void SectionDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            CheckSelectedData();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getInsSection(InstituteID, selectedClass.getClassID(), selectedDepartment.getDepartmentID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseSectionJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseSectionJsonData(String jsonString) {
        try {
            allSectionArrayList = new ArrayList<>();
            JSONArray sectionJsonArray = new JSONArray(jsonString);
            ArrayList<String> sectionArrayList = new ArrayList<>();
            sectionArrayList.add("Select Section");
            for(int i = 0; i < sectionJsonArray.length(); ++i) {
                JSONObject sectionJsonObject = sectionJsonArray.getJSONObject(i);
                SectionModel sectionModel = new SectionModel(sectionJsonObject.getString("SectionID"), sectionJsonObject.getString("SectionName"));
                allSectionArrayList.add(sectionModel);
                sectionArrayList.add(sectionModel.getSectionName());
            }
//            if(allSectionArrayList.size() == 1){
//                selectedSection = allSectionArrayList.get(0);
//            }
            try {
                String[] strings = new String[sectionArrayList.size()];
                strings = sectionArrayList.toArray(strings);
                ArrayAdapter<String> section_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                section_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSection.setAdapter(section_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No section found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void ExamDataGetRequest() {
        if (StaticHelperClass.isNetworkAvailable(this)) {
            dialog.show();
            CheckSelectedData();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getClassWiseInsExame(InstituteID, selectedMedium.getMediumID(), selectedClass.getClassID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseExamJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseExamJsonData(String jsonString) {
        try {
            allExamArrayList = new ArrayList<>();
            JSONArray examJsonArray = new JSONArray(jsonString);
            ArrayList<String> examArrayList = new ArrayList<>();
            examArrayList.add("Select Exam");
            for(int i = 0; i < examJsonArray.length(); ++i) {
                JSONObject examJsonObject = examJsonArray.getJSONObject(i);
                if(Integer.parseInt(examJsonObject.getString("IsActive")) == 1) {
                    ExamModel examModel = new ExamModel(examJsonObject.getString("ExamID"), examJsonObject.getString("ExamName"),
                            examJsonObject.getString("InsExamID"), examJsonObject.getString("Sequence")
                            , examJsonObject.getString("IsActive"));
                    allExamArrayList.add(examModel);
                    examArrayList.add(examModel.getExamName());
                }
            }
//            if(allExamArrayList.size() == 1){
//                selectedExam = allExamArrayList.get(0);
//            }
            try {
                String[] strings = new String[examArrayList.size()];
                strings = examArrayList.toArray(strings);
                ArrayAdapter<String> exam_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                exam_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerExam.setAdapter(exam_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No section found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void GetStudentListPostRequest() {
        if(StaticHelperClass.isNetworkAvailable(this)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("pageNumber", "0");
            jsonObject.addProperty("pageSize", "100000");
            jsonObject.addProperty("IsPaging", "0");
            jsonObject.addProperty("InstituteID", Long.toString(InstituteID));
            jsonObject.addProperty("LoggedUserID", LoggedUserID);
            jsonObject.addProperty("ExamID", String.valueOf(selectedExam.getExamID()));
            jsonObject.addProperty("UserID", "0");
            jsonObject.addProperty("ClassID", Long.toString(selectedClass.getClassID()));
            jsonObject.addProperty("SectionID", Long.toString(selectedSection.getSectionID()));
            jsonObject.addProperty("DepartmentID", Long.toString(selectedDepartment.getDepartmentID()));
            jsonObject.addProperty("MediumID", Long.toString(selectedMedium.getMediumID()));
            jsonObject.addProperty("ShiftID", Long.toString(selectedShift.getShiftID()));
            jsonObject.addProperty("SessionID", Long.toString(selectedSession.getSessionID()));

            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getStudentList(jsonObject)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

            finalDisposer.add( observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<String>() {

                        @Override
                        public void onNext(String response) {
                            dialog.dismiss();
                            parseStudentJsonData(response);
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            Toast.makeText(ResultMainScreen.this,"Student not found!!! ",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(ResultMainScreen.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseStudentJsonData(String jsonString) {
        try {
            allStudentJsonArray = new JSONArray();
            JSONArray studentJsonArray = new JSONArray(jsonString);
            ArrayList<String> studentArrayList = new ArrayList<>();
            studentArrayList.add("Select Student");
            for(int i = 0; i < studentJsonArray.length(); ++i) {
                JSONObject studentJsonObject = studentJsonArray.getJSONObject(i);
                studentArrayList.add(studentJsonObject.getString("RollNo")+" - "+studentJsonObject.getString("UserName"));
                allStudentJsonArray.put(studentJsonObject);
            }
            try {
                String[] strings = new String[studentArrayList.size()];
                strings = studentArrayList.toArray(strings);
                ArrayAdapter<String> student_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                student_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerStudent.setAdapter(student_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No student found !!!",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void CheckSelectedData(){
        if(selectedClass.getClassID() == -2) {
            selectedClass.setClassID("0");
        }
        if(selectedShift.getShiftID() == -2) {
            selectedShift.setShiftID("0");
        }
        if(selectedSection.getSectionID() == -2) {
            selectedSection.setSectionID("0");
        }
        if(selectedMedium.getMediumID() == -2) {
            selectedMedium.setMediumID("0");
        }
        if(selectedDepartment.getDepartmentID() == -2) {
            selectedDepartment.setDepartmentID("0");
        }
    }


}
