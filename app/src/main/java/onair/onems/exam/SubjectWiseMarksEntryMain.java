package onair.onems.exam;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import onair.onems.R;
import onair.onems.Services.RetrofitNetworkService;
import onair.onems.Services.StaticHelperClass;
import onair.onems.attendance.TakeAttendance;
import onair.onems.mainactivities.SideNavigationMenuParentActivity;
import onair.onems.mainactivities.TeacherMainScreen;
import onair.onems.models.ClassModel;
import onair.onems.models.DepartmentModel;
import onair.onems.models.ExamModel;
import onair.onems.models.MediumModel;
import onair.onems.models.SectionModel;
import onair.onems.models.SessionModel;
import onair.onems.models.ShiftModel;
import onair.onems.models.SubjectModel;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SubjectWiseMarksEntryMain extends SideNavigationMenuParentActivity {

    private Spinner spinnerSession, spinnerShift, spinnerMedium, spinnerClass, spinnerDepartment, spinnerSection, spinnerSubject, spinnerExam;

    private ArrayList<SessionModel> allSessionArrayList;
    private ArrayList<ShiftModel> allShiftArrayList;
    private ArrayList<MediumModel> allMediumArrayList;
    private ArrayList<ClassModel> allClassArrayList;
    private ArrayList<DepartmentModel> allDepartmentArrayList;
    private ArrayList<SectionModel> allSectionArrayList;
    private ArrayList<SubjectModel> allSubjectArrayList;
    private ArrayList<ExamModel> allExamArrayList;

    private String[] tempSessionArray = {"Select Session"};
    private String[] tempShiftArray = {"Select Shift"};
    private String[] tempMediumArray = {"Select Medium"};
    private String[] tempClassArray = {"Select Class"};
    private String[] tempDepartmentArray = {"Select Department"};
    private String[] tempSectionArray = {"Select Section"};
    private String[] tempSubjectArray = {"Select Subject"};
    private String[] tempExamArray = {"Select Exam"};

    private SessionModel selectedSession = null;
    private ShiftModel selectedShift = null;
    private MediumModel selectedMedium = null;
    private ClassModel selectedClass = null;
    private DepartmentModel selectedDepartment = null;
    private SectionModel selectedSection = null;
    private SubjectModel selectedSubject = null;
    private ExamModel selectedExam = null;
    private int firstShift = 0, firstMedium = 0, firstClass = 0, firstDepartment = 0, firstSection = 0, firstSubject = 0, firstExam = 0;
    private CompositeDisposable finalDisposer = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!finalDisposer.isDisposed())
            finalDisposer.dispose();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityName = SubjectWiseMarksEntryMain.class.getName();

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View childActivityLayout = Objects.requireNonNull(inflater).inflate(R.layout.exam_subject_wise_marks_entry_main, null);
        LinearLayout parentActivityLayout = findViewById(R.id.contentMain);
        parentActivityLayout.addView(childActivityLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        selectedSession = new SessionModel();
        selectedShift = new ShiftModel();
        selectedMedium = new MediumModel();
        selectedClass = new ClassModel();
        selectedDepartment = new DepartmentModel();
        selectedSection = new SectionModel();

        spinnerSession = findViewById(R.id.spinnerSession);
        spinnerShift = findViewById(R.id.spinnerShift);
        spinnerMedium = findViewById(R.id.spinnerMedium);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerSection = findViewById(R.id.spinnerSection);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerExam = findViewById(R.id.spinnerExam);

        Button entryMarks = findViewById(R.id.entryMarks);
        entryMarks.setOnClickListener(v -> {
            if(selectedClass.getClassID()==0||selectedClass.getClassID()==-2) {
                Toast.makeText(SubjectWiseMarksEntryMain.this,"Select a class !!!",Toast.LENGTH_LONG).show();
            } else if(selectedSubject == null) {
                Toast.makeText(SubjectWiseMarksEntryMain.this,"Select a subject !!!",Toast.LENGTH_LONG).show();
            }else if(selectedExam == null){
                Toast.makeText(SubjectWiseMarksEntryMain.this,"Select an exam !!!",Toast.LENGTH_LONG).show();
            } else {
                CheckSelectedData();
                Intent intent = new Intent(SubjectWiseMarksEntryMain.this, SubjectWiseMarksEntryStudentList.class);
                intent.putExtra("InstituteID", InstituteID);
                intent.putExtra("ClassID", selectedClass.getClassID());
                intent.putExtra("SectionID", selectedSection.getSectionID());
                intent.putExtra("DepartmentID", selectedDepartment.getDepartmentID());
                intent.putExtra("MediumID", selectedMedium.getMediumID());
                intent.putExtra("ShiftID", selectedShift.getShiftID());
                intent.putExtra("SubjectID", selectedSubject.getSubjectID());
                intent.putExtra("ExamID", selectedExam.getExamID());
                intent.putExtra("SessionID", selectedSession.getSessionID());
                startActivity(intent);
            }
        });

        ArrayAdapter<String> session_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempSessionArray);
        session_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSession.setAdapter(session_spinner_adapter);

        ArrayAdapter<String> shift_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempShiftArray);
        shift_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShift.setAdapter(shift_spinner_adapter);

        ArrayAdapter<String> medium_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempMediumArray);
        medium_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedium.setAdapter(medium_spinner_adapter);

        ArrayAdapter<String> class_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempClassArray);
        class_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(class_spinner_adapter);

        ArrayAdapter<String> department_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempDepartmentArray);
        department_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(department_spinner_adapter);

        ArrayAdapter<String> section_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempSectionArray);
        section_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSection.setAdapter(section_spinner_adapter);

        ArrayAdapter<String> subject_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempSubjectArray);
        subject_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subject_spinner_adapter);

        ArrayAdapter<String> exam_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempExamArray);
        exam_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExam.setAdapter(exam_spinner_adapter);

        spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    try {
                        selectedSession = allSessionArrayList.get(position-1);
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No session found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    selectedSession = new SessionModel();
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
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No shift found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstShift++>1) {
                        selectedShift = new ShiftModel();
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
                        selectedSubject = null;
                        selectedExam = null;
                        ClassDataGetRequest();
                        SubjectDataGetRequest();
                        ExamDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No medium found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstMedium++>1) {
                        selectedMedium = new MediumModel();
                        selectedClass = new ClassModel();
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
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
                        selectedSubject = null;
                        selectedExam = null;
                        DepartmentDataGetRequest();
                        SubjectDataGetRequest();
                        ExamDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No class found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstClass++>1) {
                        selectedClass = new ClassModel();
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
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
                        selectedSubject = null;
                        SectionDataGetRequest();
                        SubjectDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No department found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstDepartment++>1) {
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
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
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No section found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstSection++>1) {
                        selectedSection = new SectionModel();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0)
                {
                    try {
                        selectedSubject = allSubjectArrayList.get(position-1);
                    }
                    catch (IndexOutOfBoundsException e)
                    {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No subject found !!!",Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    if(firstSubject++>1) {
                        selectedSubject = null;
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
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(SubjectWiseMarksEntryMain.this,"No Exam found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstExam++>1) {
                        selectedExam = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SessionDataGetRequest();
        ShiftDataGetRequest();
        MediumDataGetRequest();
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
                            Toast.makeText(SubjectWiseMarksEntryMain.this,"Session not found!!! ",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(this,"Please check your internet connection and select again!!! ",
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseShiftJsonData(String jsonString) {
        ArrayList<String> shiftArrayList = new ArrayList<>();
        try {
            allShiftArrayList = new ArrayList<>();
            JSONArray shiftJsonArray = new JSONArray(jsonString);
            shiftArrayList.add("Select Shift");
            for(int i = 0; i < shiftJsonArray.length(); ++i) {
                JSONObject shiftJsonObject = shiftJsonArray.getJSONObject(i);
                ShiftModel shiftModel = new ShiftModel(shiftJsonObject.getString("ShiftID"), shiftJsonObject.getString("ShiftName"));
                allShiftArrayList.add(shiftModel);
                shiftArrayList.add(shiftModel.getShiftName());
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
        try {
            String[] strings = new String[shiftArrayList.size()];
            strings = shiftArrayList.toArray(strings);
            ArrayAdapter<String> shift_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            shift_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerShift.setAdapter(shift_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,"No shift found !!!",Toast.LENGTH_LONG).show();
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseMediumJsonData(String jsonString) {
        ArrayList<String> mediumArrayList = new ArrayList<>();
        try {
            allMediumArrayList = new ArrayList<>();
            JSONArray mediumJsonArray = new JSONArray(jsonString);
            mediumArrayList.add("Select Medium");
            for(int i = 0; i < mediumJsonArray.length(); ++i) {
                JSONObject mediumJsonObject = mediumJsonArray.getJSONObject(i);
                MediumModel mediumModel = new MediumModel(mediumJsonObject.getString("MediumID"), mediumJsonObject.getString("MameName"),
                        mediumJsonObject.getString("IsDefault"));
                allMediumArrayList.add(mediumModel);
                mediumArrayList.add(mediumModel.getMameName());
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }

        try {
            String[] strings = new String[mediumArrayList.size()];
            strings = mediumArrayList.toArray(strings);
            ArrayAdapter<String> medium_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            medium_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMedium.setAdapter(medium_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,"No medium found !!!",Toast.LENGTH_LONG).show();
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseClassJsonData(String jsonString) {
        ArrayList<String> classArrayList = new ArrayList<>();
        try {
            allClassArrayList = new ArrayList<>();
            JSONArray classJsonArray = new JSONArray(jsonString);
            classArrayList.add("Select Class");
            for(int i = 0; i < classJsonArray.length(); ++i) {
                JSONObject classJsonObject = classJsonArray.getJSONObject(i);
                ClassModel classModel = new ClassModel(classJsonObject.getString("ClassID"), classJsonObject.getString("ClassName"));
                allClassArrayList.add(classModel);
                classArrayList.add(classModel.getClassName());
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }

        try {
            String[] strings = new String[classArrayList.size()];
            strings = classArrayList.toArray(strings);
            ArrayAdapter<String> class_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            class_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClass.setAdapter(class_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,"No class found !!!",Toast.LENGTH_LONG).show();
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseDepartmentJsonData(String jsonString) {
        ArrayList<String> departmentArrayList = new ArrayList<>();
        try {
            allDepartmentArrayList = new ArrayList<>();
            JSONArray departmentJsonArray = new JSONArray(jsonString);
            departmentArrayList.add("Select Department");
            for(int i = 0; i < departmentJsonArray.length(); ++i) {
                JSONObject departmentJsonObject = departmentJsonArray.getJSONObject(i);
                DepartmentModel departmentModel = new DepartmentModel(departmentJsonObject.getString("DepartmentID"), departmentJsonObject.getString("DepartmentName"));
                allDepartmentArrayList.add(departmentModel);
                departmentArrayList.add(departmentModel.getDepartmentName());
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseSectionJsonData(String jsonString) {
        ArrayList<String> sectionArrayList = new ArrayList<>();
        try {
            allSectionArrayList = new ArrayList<>();
            JSONArray sectionJsonArray = new JSONArray(jsonString);
            sectionArrayList.add("Select Section");
            for(int i = 0; i < sectionJsonArray.length(); ++i) {
                JSONObject sectionJsonObject = sectionJsonArray.getJSONObject(i);
                SectionModel sectionModel = new SectionModel(sectionJsonObject.getString("SectionID"), sectionJsonObject.getString("SectionName"));
                allSectionArrayList.add(sectionModel);
                sectionArrayList.add(sectionModel.getSectionName());
            }
        } catch (JSONException e) {
//            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
        try {
            String[] strings = new String[sectionArrayList.size()];
            strings = sectionArrayList.toArray(strings);
            ArrayAdapter<String> section_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            section_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSection.setAdapter(section_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,"No section found !!!",Toast.LENGTH_LONG).show();
        }
    }

    private void SubjectDataGetRequest() {
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
                    .getInsSubject(InstituteID, selectedDepartment.getDepartmentID(), selectedMedium.getMediumID(), selectedClass.getClassID())
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
                            parseSubjectJsonData(response);
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseSubjectJsonData(String jsonString) {
        ArrayList<String> subjectArrayList = new ArrayList<>();
        try {
            allSubjectArrayList = new ArrayList<>();
            JSONArray subjectJsonArray = new JSONArray(jsonString);
            subjectArrayList.add("Select Subject");
            for(int i = 0; i < subjectJsonArray.length(); ++i) {
                JSONObject subjectJsonObject = subjectJsonArray.getJSONObject(i);
                SubjectModel subjectModel = new SubjectModel(subjectJsonObject.getString("SubjectID"), subjectJsonObject.getString("SubjectNo"),
                        subjectJsonObject.getString("SubjectName"), subjectJsonObject.getString("InsSubjectID"), subjectJsonObject.getString("InstituteID"),
                        subjectJsonObject.getString("DepartmentID"), subjectJsonObject.getString("MediumID"), subjectJsonObject.getString("ClassID"),
                        subjectJsonObject.getString("IsActive"), subjectJsonObject.getString("IsCombined"), subjectJsonObject.getString("ParentID"));
                allSubjectArrayList.add(subjectModel);
                subjectArrayList.add(subjectModel.getSubjectName());
            }
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
        try {
            String[] strings = new String[subjectArrayList.size()];
            strings = subjectArrayList.toArray(strings);
            ArrayAdapter<String> subject_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            subject_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSubject.setAdapter(subject_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,"No subject found !!!",Toast.LENGTH_LONG).show();
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
            Toast.makeText(SubjectWiseMarksEntryMain.this,"Please check your internet connection and select again!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseExamJsonData(String jsonString) {
        ArrayList<String> examArrayList = new ArrayList<>();
        try {
            allExamArrayList = new ArrayList<>();
            JSONArray examJsonArray = new JSONArray(jsonString);
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
        } catch (JSONException e) {
            Toast.makeText(SubjectWiseMarksEntryMain.this,""+e,Toast.LENGTH_LONG).show();
        }

        try {
            String[] strings = new String[examArrayList.size()];
            strings = examArrayList.toArray(strings);
            ArrayAdapter<String> exam_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
            exam_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExam.setAdapter(exam_spinner_adapter);
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(SubjectWiseMarksEntryMain.this,"No section found !!!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent mainIntent = new Intent(SubjectWiseMarksEntryMain.this, TeacherMainScreen.class);
            startActivity(mainIntent);
            finish();
        }
    }

    private void CheckSelectedData(){
        if(selectedShift.getShiftID() == -2) {
            selectedShift.setShiftID("0");
        }
        if(selectedMedium.getMediumID() == -2) {
            selectedMedium.setMediumID("0");
        }
        if(selectedClass.getClassID() == -2) {
            selectedClass.setClassID("0");
        }
        if(selectedDepartment.getDepartmentID() == -2) {
            selectedDepartment.setDepartmentID("0");
        }
        if(selectedSection.getSectionID() == -2) {
            selectedSection.setSectionID("0");
        }
    }
}
