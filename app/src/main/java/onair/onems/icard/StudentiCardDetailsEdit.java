package onair.onems.icard;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import onair.onems.R;
import onair.onems.Services.GlideApp;
import onair.onems.Services.RetrofitNetworkService;
import onair.onems.Services.StaticHelperClass;
import onair.onems.mainactivities.CommonToolbarParentActivity;
import onair.onems.models.BloodGroupModel;
import onair.onems.models.ClassModel;
import onair.onems.models.DepartmentModel;
import onair.onems.models.GenderModel;
import onair.onems.models.MediumModel;
import onair.onems.models.ReligionModel;
import onair.onems.models.SectionModel;
import onair.onems.models.ShiftModel;
import onair.onems.models.StudentInformationEntry;
import onair.onems.utils.ImageUtils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class StudentiCardDetailsEdit extends CommonToolbarParentActivity {

    private EditText t_name, t_email, t_address, t_parent, t_parentsPhone, t_roll,
            t_stuEmail, t_stuPhone, t_remarks;
    private TextView t_board, t_session, t_class, t_section, t_birthDay, t_department,
            t_sex, t_religion, t_medium, t_shift, t_branch, t_studentNo, t_rfid, t_bloodGroup;

    private CropImageView mCropImageView;
    private CheckBox checkBox;
    private Uri mCropImageUri;
    private StudentInformationEntry studentInformationEntry;
    private Spinner spinnerClass, spinnerShift, spinnerSection, spinnerMedium, spinnerDepartment, spinnerGender, spinnerReligion, spinnerBloodGroup;

    private ArrayList<ClassModel> allClassArrayList;
    private ArrayList<ShiftModel> allShiftArrayList;
    private ArrayList<SectionModel> allSectionArrayList;
    private ArrayList<MediumModel> allMediumArrayList;
    private ArrayList<DepartmentModel> allDepartmentArrayList;
    private ArrayList<GenderModel> allGenderArrayList;
    private ArrayList<ReligionModel> allReligionArrayList;
    private ArrayList<BloodGroupModel> allBloodGroupArrayList;

    private String[] tempClassArray = {"Select Class"};
    private String[] tempShiftArray = {"Select Shift"};
    private String[] tempSectionArray = {"Select Section"};
    private String[] tempDepartmentArray = {"Select Department"};
    private String[] tempMediumArray = {"Select Medium"};
    private String[] tempGenderArray = {"Select Gender"};
    private String[] tempReligionArray = {"Select Religion"};
    private String[] tempBloodGroupArray = {"Select Blood Group"};

    private ClassModel selectedClass = null;
    private ShiftModel selectedShift = null;
    private SectionModel selectedSection = null;
    private MediumModel selectedMedium = null;
    private DepartmentModel selectedDepartment = null;
    private GenderModel selectedGender = null;
    private ReligionModel selectedReligion = null;
    private BloodGroupModel selectedBloodGroup = null;

    private JsonObject jsonObjectStudentData;

    private File file;

    private Bitmap originalBitmap = null;
    private Bitmap tempBitmap = null;

    private FileFromBitmap fileFromBitmap = null;

    private String selected_Class, selected_Shift,
            selected_Section, selected_Medium, selected_Department, UserID, selected_Session;
    private int PICK_IMAGE_REQUEST = 1;

    private long InstituteID;
    private static int brightnessValue;
    private BrightnessProcessTask mBrightnessProcessTask = null;
    private RotateProcessTask mRotateProcessTask = null;
    private String selectedDate = "";
    private DatePickerDialog datePickerDialog;
    private boolean imageChanged = false;
    private int firstClass = 0, firstShift = 0, firstSection = 0, firstMedium = 0,
            firstDepartment = 0, firstGender = 0, firstReligion = 0, firstBloodGroup = 0;
    private ProgressBar progressBar;
    private Button rotateLeft, rotateRight;
    private SeekBar brightImageSeekBar;
    private CompositeDisposable finalDisposer = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!finalDisposer.isDisposed())
            finalDisposer.dispose();
    }

    @Override
    public void onResume() {
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    @Override
    public void onStart() {
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View childActivityLayout = Objects.requireNonNull(inflater).inflate(R.layout.icard_student_details_edit, null);
        LinearLayout parentActivityLayout = findViewById(R.id.contentMain);
        parentActivityLayout.addView(childActivityLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        InstituteID = prefs.getLong("InstituteID",0);

        selectedClass = new ClassModel();
        selectedShift = new ShiftModel();
        selectedSection = new SectionModel();
        selectedMedium = new MediumModel();
        selectedDepartment = new DepartmentModel();
        selectedGender = new GenderModel();
        selectedReligion = new ReligionModel();
        selectedBloodGroup = new BloodGroupModel();
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        selected_Class = Objects.requireNonNull(bundle).getString("ClassID");
        selected_Session = bundle.getString("SessionID");
        selected_Shift = bundle.getString("ShiftID");
        selected_Section = bundle.getString("SectionID");
        selected_Medium = bundle.getString("MediumID");
        selected_Department = bundle.getString("DepartmentID");
        UserID = bundle.getString("UserID");

        rotateLeft = findViewById(R.id.rotateLeft);
        rotateRight = findViewById(R.id.rotateRight);
        brightImageSeekBar = findViewById(R.id.brightness);
        brightImageSeekBar.setProgress(100);
        progressBar = (ProgressBar)findViewById(R.id.spin_kit);

        studentInformationEntry = new StudentInformationEntry();

//        originalBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.image1);

        mCropImageView = findViewById(R.id.CropImageView);
        mCropImageView.setAspectRatio(1,1);
        mCropImageView.setAutoZoomEnabled(true);
        mCropImageView.setFixedAspectRatio(true);

        Button updateStudentPhoto = findViewById(R.id.updatephoto);

        checkBox = findViewById(R.id.checkbox);

        Button cameraButton= findViewById(R.id.camera);
        Button searchButton= findViewById(R.id.browse);

        t_roll = findViewById(R.id.stuRoll);
        t_name = findViewById(R.id.stuName);
        t_class = findViewById(R.id.classs);
        t_section = findViewById(R.id.section);
        t_birthDay = findViewById(R.id.birthday);
        t_email = findViewById(R.id.guardianEmail);
        t_address = findViewById(R.id.presentAddress);
        t_parent = findViewById(R.id.guardianName);
        t_parentsPhone = findViewById(R.id.guardianPhoneNo);
        t_department = findViewById(R.id.department);
        t_sex = findViewById(R.id.sex);
        t_religion = findViewById(R.id.religion);
        t_medium = findViewById(R.id.medium);
        t_session = findViewById(R.id.session);
        t_board = findViewById(R.id.board);
        t_shift = findViewById(R.id.shift);
        t_stuEmail = findViewById(R.id.stuEmail);
        t_stuPhone = findViewById(R.id.stuPhone);
        t_branch = findViewById(R.id.branch);
        t_remarks = findViewById(R.id.remarks);
        t_studentNo = findViewById(R.id.studentNo);
        t_rfid = findViewById(R.id.rfid);
        t_bloodGroup = findViewById(R.id.bloodGroup);
        Button datePicker = findViewById(R.id.pickDate);

        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerShift = findViewById(R.id.spinnerShift);
        spinnerSection = findViewById(R.id.spinnerSection);
        spinnerMedium = findViewById(R.id.spinnerMedium);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerReligion = findViewById(R.id.spinnerReligion);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);

        checkBox.setEnabled(false);
        rotateLeft.setEnabled(false);
        rotateRight.setEnabled(false);
        brightImageSeekBar.setEnabled(false);

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

        ArrayAdapter<String> gender_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempGenderArray);
        gender_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(gender_spinner_adapter);

        ArrayAdapter<String> religion_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempReligionArray);
        religion_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReligion.setAdapter(religion_spinner_adapter);

        ArrayAdapter<String> bloodGroup_spinner_adapter = new ArrayAdapter<>(this, R.layout.spinner_item, tempBloodGroupArray);
        bloodGroup_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodGroup_spinner_adapter);

        StudentDataGetRequest();

        datePicker.setOnClickListener(v -> {
            // calender class's instance and get current date , month and year from calender
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR); // current year
            int mMonth = c.get(Calendar.MONTH); // current month
            int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
            // date picker dialog
            datePickerDialog = new DatePickerDialog(StudentiCardDetailsEdit.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        // set day of month , month and year value in the edit text
//                                selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        studentInformationEntry.setDOB(selectedDate);
                        t_birthDay.setText(selectedDate);
//                                datePicker.setText(selectedDate);

                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        cameraButton.setOnClickListener(v -> startActivityForResult(getPickImageChooserIntent(), 200));

        searchButton.setOnClickListener(v -> {
            Intent intent1 = new Intent();
            // Show only images, no videos or anything else
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (originalBitmap != null)
            {
                imageChanged = true;
            }

        });

        rotateLeft.setOnClickListener(v -> {
            imageChanged = true;
            mRotateProcessTask = new RotateProcessTask(originalBitmap, -90);
            mRotateProcessTask.execute((Void) null);
        });

        rotateRight.setOnClickListener(v -> {
            imageChanged = true;
            mRotateProcessTask = new RotateProcessTask(originalBitmap, 90);
            mRotateProcessTask.execute((Void) null);
        });

        brightImageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValue = progress - 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageChanged = true;
                mBrightnessProcessTask = new BrightnessProcessTask(originalBitmap, brightnessValue);
                mBrightnessProcessTask.execute((Void) null);
            }
        });

        updateStudentPhoto.setOnClickListener(view -> {
            if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
                studentInformationEntry.setUserName(t_name.getText().toString());
                studentInformationEntry.setRollNo(t_roll.getText().toString());
                studentInformationEntry.setRFID(t_rfid.getText().toString());
                studentInformationEntry.setStudentNo(t_studentNo.getText().toString());
                studentInformationEntry.setEmailID(t_stuEmail.getText().toString());
                studentInformationEntry.setPhoneNo(t_stuPhone.getText().toString());
                studentInformationEntry.setGuardian(t_parent.getText().toString());
                studentInformationEntry.setGuardianPhone(t_parentsPhone.getText().toString());
                studentInformationEntry.setGuardianEmailID(t_email.getText().toString());
                studentInformationEntry.setPreAddress(t_address.getText().toString());
                studentInformationEntry.setRemarks(t_remarks.getText().toString());
                if(imageChanged) {
                    if(originalBitmap != null) {
                        if(checkBox.isChecked()) {
                            tempBitmap = mCropImageView.getCroppedImage(500, 500);
                            mCropImageView.setImageBitmap(tempBitmap);
                            fileFromBitmap = new FileFromBitmap(tempBitmap, StudentiCardDetailsEdit.this);
                            fileFromBitmap.execute();
                        } else {
                            fileFromBitmap = new FileFromBitmap(tempBitmap, StudentiCardDetailsEdit.this);
                            fileFromBitmap.execute();
                        }
                    } else {
                        Toast.makeText(StudentiCardDetailsEdit.this,"Take or choose a photo to update!!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(studentInformationEntry);
                    postUsingVolley(json);
                }
            } else {
                Toast.makeText(StudentiCardDetailsEdit.this,"Please check your INTERNET connection!!!",Toast.LENGTH_LONG).show();
            }
        });

        ShiftDataGetRequest();
        MediumDataGetRequest();
        ReligionDataGetRequest();
        GenderDataGetRequest();
        BloodGroupDataGetRequest();

        spinnerShift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    try {
                        selectedShift = allShiftArrayList.get(position-1);
                        studentInformationEntry.setShiftID(Long.toString(selectedShift.getShiftID()));
                        t_shift.setText(selectedShift.getShiftName());

                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No shift found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstShift++>1) {
                        selectedShift = new ShiftModel();
                        studentInformationEntry.setShiftID(Long.toString(selectedShift.getShiftID()));
                        t_shift.setText(R.string.none);
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
                        studentInformationEntry.setMediumID(Long.toString(selectedMedium.getMediumID()));
                        studentInformationEntry.setClassID(Long.toString(selectedClass.getClassID()));
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_medium.setText(selectedMedium.getMameName());
                        t_class.setText(R.string.none);
                        t_department.setText(R.string.none);
                        t_section.setText(R.string.none);
                        ClassDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No medium found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstMedium++>1) {
                        selectedMedium = new MediumModel();
                        selectedClass = new ClassModel();
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
                        studentInformationEntry.setMediumID(Long.toString(selectedMedium.getMediumID()));
                        studentInformationEntry.setClassID(Long.toString(selectedClass.getClassID()));
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_medium.setText(R.string.none);
                        t_class.setText(R.string.none);
                        t_department.setText(R.string.none);
                        t_section.setText(R.string.none);
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
                        studentInformationEntry.setClassID(Long.toString(selectedClass.getClassID()));
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_class.setText(selectedClass.getClassName());
                        t_department.setText(R.string.none);
                        t_section.setText(R.string.none);
                        DepartmentDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No class found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstClass++>1) {
                        selectedClass = new ClassModel();
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
                        studentInformationEntry.setClassID(Long.toString(selectedClass.getClassID()));
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_class.setText(R.string.none);
                        t_department.setText(R.string.none);
                        t_section.setText(R.string.none);
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
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_department.setText(selectedDepartment.getDepartmentName());
                        t_section.setText(R.string.none);
                        SectionDataGetRequest();
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No department found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstDepartment++>1) {
                        selectedDepartment = new DepartmentModel();
                        selectedSection = new SectionModel();
                        studentInformationEntry.setDepartmentID(Long.toString(selectedDepartment.getDepartmentID()));
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_department.setText(R.string.none);
                        t_section.setText(R.string.none);
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
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_section.setText(selectedSection.getSectionName());
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No section found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstSection++>1) {
                        selectedSection = new SectionModel();
                        studentInformationEntry.setSectionID(Long.toString(selectedSection.getSectionID()));
                        t_section.setText(R.string.none);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    try {
                        selectedGender = allGenderArrayList.get(position-1);
                        studentInformationEntry.setGenderID(Long.toString(selectedGender.getGenderID()));
                        t_sex.setText(selectedGender.getGenderName());
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No gender found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstGender++>1) {
                        selectedGender = new GenderModel();
                        studentInformationEntry.setGenderID(Long.toString(selectedGender.getGenderID()));
                        t_sex.setText(R.string.none);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerReligion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    try {
                        selectedReligion = allReligionArrayList.get(position-1);
                        studentInformationEntry.setReligionID(Long.toString(selectedReligion.getReligionID()));
                        t_religion.setText(selectedReligion.getReligionName());
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No religion found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstReligion++>1) {
                        selectedReligion = new ReligionModel();
                        studentInformationEntry.setReligionID(Long.toString(selectedReligion.getReligionID()));
                        t_religion.setText(R.string.none);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerBloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    try {
                        selectedBloodGroup = allBloodGroupArrayList.get(position-1);
                        studentInformationEntry.setBloodGroupID(Long.toString(selectedBloodGroup.getBloodGroupID()));
                        t_bloodGroup.setText(selectedBloodGroup.getBloodGroupName());
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(StudentiCardDetailsEdit.this,"No religion found !!!",Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(firstBloodGroup++>1) {
                        selectedBloodGroup = new BloodGroupModel();
                        studentInformationEntry.setBloodGroupID(Long.toString(selectedBloodGroup.getBloodGroupID()));
                        t_bloodGroup.setText(R.string.none);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void StudentDataGetRequest(){
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getStudent(InstituteID,
                            selected_Class, selected_Section,
                            selected_Department, selected_Medium, selected_Shift, UserID, selected_Session)
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
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));
        } else {
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void parseStudentJsonData(String jsonString) {
        try {
            JSONArray studentJsonArray = new JSONArray(jsonString);
            JSONObject studentJsonObject = studentJsonArray.getJSONObject(0);
            if(studentJsonObject.getString("ImageUrl").equals("null")){
                Toast.makeText(StudentiCardDetailsEdit.this,"No image found!!!",Toast.LENGTH_LONG).show();
            }
            GlideApp.with(this)
                    .asBitmap()
                    .load(getString(R.string.baseUrl)+"/"+studentJsonObject.getString("ImageUrl").replace("\\","/"))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            mCropImageView.setImageBitmap(resource);
                            originalBitmap = resource;
                            tempBitmap = resource;
                            progressBar.setVisibility(View.GONE);
                            if(resource != null) {
                                checkBox.setEnabled(true);
                                rotateLeft.setEnabled(true);
                                rotateRight.setEnabled(true);
                                brightImageSeekBar.setEnabled(true);
                            }
                        }
                        @Override
                        public void onLoadFailed(Drawable errorDrawable){
                            super.onLoadFailed(errorDrawable);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(StudentiCardDetailsEdit.this,"No image found!!!",Toast.LENGTH_LONG).show();
                        }
                    });

            studentInformationEntry = new StudentInformationEntry();
            studentInformationEntry.setRollNo(studentJsonObject.getString("RollNo"));
            studentInformationEntry.setUserName(studentJsonObject.getString("UserName"));
            studentInformationEntry.setGuardian(studentJsonObject.getString("Guardian"));
            studentInformationEntry.setGuardianPhone(studentJsonObject.getString("GuardianPhone"));
            studentInformationEntry.setGuardianEmailID(studentJsonObject.getString("GuardianEmailID"));
            studentInformationEntry.setDOB(studentJsonObject.getString("DOB"));
            studentInformationEntry.setPreAddress(studentJsonObject.getString("PreAddress"));
            studentInformationEntry.setUserClassID(studentJsonObject.getString("UserClassID"));
            studentInformationEntry.setUserID(studentJsonObject.getString("UserID"));
            studentInformationEntry.setRFID(studentJsonObject.getString("RFID"));
            studentInformationEntry.setStudentNo(studentJsonObject.getString("StudentNo"));
            studentInformationEntry.setSectionID(studentJsonObject.getString("SectionID"));
            studentInformationEntry.setClassID(studentJsonObject.getString("ClassID"));
            studentInformationEntry.setBrunchID(studentJsonObject.getString("BrunchID"));
            studentInformationEntry.setShiftID(studentJsonObject.getString("ShiftID"));
            studentInformationEntry.setRemarks(studentJsonObject.getString("Remarks"));
            studentInformationEntry.setInstituteID(studentJsonObject.getString("InstituteID"));
            studentInformationEntry.setUserTypeID(studentJsonObject.getString("UserTypeID"));
            studentInformationEntry.setGenderID(studentJsonObject.getString("GenderID"));
            studentInformationEntry.setBloodGroupID(studentJsonObject.getString("BloodGroupID"));
            studentInformationEntry.setReligionID(studentJsonObject.getString("ReligionID"));
            studentInformationEntry.setSessionID(studentJsonObject.getString("SessionID"));
            studentInformationEntry.setBoardID(studentJsonObject.getString("BoardID"));
            studentInformationEntry.setPhoneNo(studentJsonObject.getString("PhoneNo"));
            studentInformationEntry.setEmailID(studentJsonObject.getString("EmailID"));
            studentInformationEntry.setFingerUrl(studentJsonObject.getString("FingerUrl"));
            studentInformationEntry.setSignatureUrl(studentJsonObject.getString("SignatureUrl"));
            studentInformationEntry.setMediumID(studentJsonObject.getString("MediumID"));
            studentInformationEntry.setDepartmentID(studentJsonObject.getString("DepartmentID"));
            studentInformationEntry.setIsImageCaptured(studentJsonObject.getBoolean("IsImageCaptured"));
            studentInformationEntry.setImageUrl(studentJsonObject.getString("ImageUrl"));
            studentInformationEntry.setUserNo(studentJsonObject.getString("UserNo"));
            studentInformationEntry.setUserTitleID(studentJsonObject.getString("UserTitleID"));
            studentInformationEntry.setUserFirstName(studentJsonObject.getString("UserFirstName"));
            studentInformationEntry.setUserMiddleName(studentJsonObject.getString("UserMiddleName"));
            studentInformationEntry.setUserLastName(studentJsonObject.getString("UserLastName"));
            studentInformationEntry.setNickName(studentJsonObject.getString("NickName"));
            studentInformationEntry.setSkypeID(studentJsonObject.getString("SkypeID"));
            studentInformationEntry.setFacebookID(studentJsonObject.getString("FacebookID"));
            studentInformationEntry.setWhatsApp(studentJsonObject.getString("WhatsApp"));
            studentInformationEntry.setViber(studentJsonObject.getString("Viber"));
            studentInformationEntry.setLinkedIN(studentJsonObject.getString("LinkedIN"));
            studentInformationEntry.setParAddress(studentJsonObject.getString("ParAddress"));
            studentInformationEntry.setParThana(studentJsonObject.getString("ParThana"));
            studentInformationEntry.setParPostCode(studentJsonObject.getString("ParPostCode"));
            studentInformationEntry.setParCountryID(studentJsonObject.getString("ParCountryID"));
            studentInformationEntry.setParStateID(studentJsonObject.getString("ParStateID"));
            studentInformationEntry.setParCityID(studentJsonObject.getString("ParCityID"));
            studentInformationEntry.setPreThana(studentJsonObject.getString("PreThana"));
            studentInformationEntry.setPrePostCode(studentJsonObject.getString("PrePostCode"));
            studentInformationEntry.setPreCountryID(studentJsonObject.getString("PreCountryID"));
            studentInformationEntry.setPreStateID(studentJsonObject.getString("PreStateID"));
            studentInformationEntry.setPreCityID(studentJsonObject.getString("PreCityID"));
            studentInformationEntry.setMobileNo(studentJsonObject.getString("MobileNo"));
            studentInformationEntry.setUniqueIdentity(studentJsonObject.getString("UniqueIdentity"));
            studentInformationEntry.setBloodGroupID(studentJsonObject.getString("BloodGroupID"));
            studentInformationEntry.setWeigth(studentJsonObject.getString("Weigth"));
            studentInformationEntry.setHeight(studentJsonObject.getString("Height"));
            studentInformationEntry.setBirthCertificate(studentJsonObject.getString("BirthCertificate"));
            studentInformationEntry.setPassportNO(studentJsonObject.getString("PassportNO"));
            studentInformationEntry.setNID(studentJsonObject.getString("NID"));
            studentInformationEntry.setIsActive(studentJsonObject.getString("IsActive"));
            studentInformationEntry.setStatusID(studentJsonObject.getString("StatusID"));
            studentInformationEntry.setGuardianMobileNo(studentJsonObject.getString("GuardianMobileNo"));
            studentInformationEntry.setGuardianUserFirstName(studentJsonObject.getString("GuardianUserFirstName"));
            studentInformationEntry.setGuardianUserMiddleName(studentJsonObject.getString("GuardianUserMiddleName"));
            studentInformationEntry.setGuardianUserLastName(studentJsonObject.getString("GuardianUserLastName"));
            studentInformationEntry.setGuardianNickName(studentJsonObject.getString("GuardianNickName"));
            studentInformationEntry.setGuardianUniqueIdentity(studentJsonObject.getString("GuardianUniqueIdentity"));
            studentInformationEntry.setGuardianBloodGroupID(studentJsonObject.getString("GuardianBloodGroupID"));
            studentInformationEntry.setGuardianPassportNO(studentJsonObject.getString("GuardianPassportNO"));
            studentInformationEntry.setGuardianNID(studentJsonObject.getString("GuardianNID"));
            studentInformationEntry.setRelationID(studentJsonObject.getString("RelationID"));
            studentInformationEntry.setIsLocalGuardian(studentJsonObject.getString("IsLocalGuardian"));
            studentInformationEntry.setIsActiveFamily(studentJsonObject.getString("IsActiveFamily"));
            studentInformationEntry.setIsActiveStudent(studentJsonObject.getString("IsActiveStudent"));



            t_roll.setText(studentJsonObject.getString("RollNo"));
            t_name.setText(studentJsonObject.getString("UserName"));
            t_section.setText(studentJsonObject.getString("SectionName"));
            t_birthDay.setText(studentJsonObject.getString("DOB"));
            t_email.setText(studentJsonObject.getString("GuardianEmailID"));
            t_address.setText(studentJsonObject.getString("PreAddress"));
            t_parent.setText(studentJsonObject.getString("Guardian"));
            t_parentsPhone.setText(studentJsonObject.getString("GuardianPhone"));
            t_department.setText(studentJsonObject.getString("DepartmentName"));
            t_sex.setText(studentJsonObject.getString("Gender"));
            t_religion.setText(studentJsonObject.getString("Religion"));
            t_medium.setText(studentJsonObject.getString("MameName"));
            t_session.setText(studentJsonObject.getString("SessionName"));
            t_board.setText(studentJsonObject.getString("BoardName"));
            t_class.setText(studentJsonObject.getString("ClassName"));
            t_shift.setText(studentJsonObject.getString("ShiftName"));
            t_rfid.setText(studentJsonObject.getString("RFID"));
            t_studentNo.setText(studentJsonObject.getString("StudentNo"));
            t_branch.setText(studentJsonObject.getString("BrunchName"));
            t_stuEmail.setText(studentJsonObject.getString("EmailID"));
            t_stuPhone.setText(studentJsonObject.getString("PhoneNo"));
            t_remarks.setText(studentJsonObject.getString("Remarks"));
            t_bloodGroup.setText(studentJsonObject.getString("BloodGroup"));

        } catch (JSONException e) {
            Toast.makeText(this,"WARNING!!! "+e,Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(File file) {
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/jpeg"),
                        file
                );
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData("file", file.getName(), requestFile);
    }

    public class FileFromBitmap extends AsyncTask<Void, Integer, String> {

        Context context;
        Bitmap bitmap;
//        String path_external = Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg";

        FileFromBitmap(Bitmap bitmap, Context context) {
            this.bitmap = bitmap;
            this.context= context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before executing doInBackground
            // update your UI
            // exp; make progressbar visible
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                file = ImageUtils.getFileFromBitmap(bitmap, context);
            } catch (IOException e) {
                dialog.dismiss();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // back to main thread after finishing doInBackground
            // update your UI or take action after
            // exp; make progressbar gone
            dialog.dismiss();
            if (StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
                dialog.show();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(getString(R.string.baseUrl))
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();

                Observable<String> photoObservable = retrofit
                        .create(RetrofitNetworkService.class)
                        .uploadSingleImage(prepareFilePart(file))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io());

                finalDisposer.add(photoObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<String>() {
                            @Override
                            public void onNext(String response) {
                                dialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    studentInformationEntry.setImageUrl(jsonObject.getString("path"));
                                    studentInformationEntry.setIsImageCaptured(true);
                                    Gson gson = new Gson();
                                    String json = gson.toJson(studentInformationEntry);
                                    postUsingVolley(json);
                                    Log.d( "ImageUrl", jsonObject.getString("path"));
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                dialog.dismiss();
                                Log.e("RXANDROID", "onError: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.e("COMPLETE", "Complete: ");
                            }
                        }));
            } else {
                Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
                        Toast.LENGTH_LONG).show();
            }

//            Ion.with(getApplicationContext())
//                    .load(getString(R.string.baseUrl)+"/api/onEms/Mobile/uploads")
//                    .progressDialog(mStudentDataPostDialog)
//                    .setMultipartParameter("name", "source")
//                    .setMultipartFile("file", "image/jpeg", file)
//                    .asString()
//                    .setCallback(new FutureCallback<String>() {
//                        @Override
//                        public void onCompleted(Exception e, String result) {
//                            //do stuff with result
//                            try {
//                                JSONObject jsonObject = new JSONObject(result);
////                                Toast.makeText(StudentiCardDetails.this,jsonObject.getString("path"), Toast.LENGTH_LONG).show();
//                                studentInformationEntry.setImageUrl(jsonObject.getString("path"));
//                                studentInformationEntry.setIsImageCaptured(true);
//                                Gson gson = new Gson();
//                                String json = gson.toJson(studentInformationEntry);
//                                postUsingVolley(json);
//                                Log.d( "ImageUrl", jsonObject.getString("path"));
//                            } catch (JSONException e1) {
//                                mStudentDataPostDialog.dismiss();
//                                e1.printStackTrace();
//                            }
//                        }
//                    });

        }
    }


    public void postUsingVolley(String json) {
        if (StaticHelperClass.isNetworkAvailable(this)) {
            try {
                JsonParser parser = new JsonParser();
                jsonObjectStudentData = parser.parse(json).getAsJsonObject();
            } catch (JsonIOException e) {
                e.printStackTrace();
            }
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .setStudentBasicInfo(jsonObjectStudentData)
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
                            Toast.makeText(StudentiCardDetailsEdit.this,"Successfully Updated",Toast.LENGTH_LONG).show();
                            StudentiCardDetailsEdit.super.onBackPressed();
                        }

                        @Override
                        public void onError(Throwable e) {
                            dialog.dismiss();
                            Toast.makeText(StudentiCardDetailsEdit.this,"Error Occurred",Toast.LENGTH_LONG).show();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = getPickImageResultUri(data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            boolean requirePermissions = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ImageUtils.isUriRequiresPermissions(imageUri, this)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                mCropImageUri = imageUri;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    originalBitmap = getResizedBitmap(bitmap, 500);
                    tempBitmap = originalBitmap;
                    imageChanged = true;
                    checkBox.setEnabled(true);
                    rotateLeft.setEnabled(true);
                    rotateRight.setEnabled(true);
                    brightImageSeekBar.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if (!requirePermissions) {
                mCropImageView.setImageUriAsync(imageUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    originalBitmap = getResizedBitmap(bitmap, 500);
                    tempBitmap = originalBitmap;
                    imageChanged = true;
                    checkBox.setEnabled(true);
                    rotateLeft.setEnabled(true);
                    rotateRight.setEnabled(true);
                    brightImageSeekBar.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                originalBitmap = getResizedBitmap(bitmap, 500);
                tempBitmap = originalBitmap;
                imageChanged = true;
                checkBox.setEnabled(true);
                rotateLeft.setEnabled(true);
                rotateRight.setEnabled(true);
                brightImageSeekBar.setEnabled(true);
                try {
                    String ImagePath = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "demo_image", "demo_image");
                    Uri URI = Uri.parse(ImagePath);
                    mCropImageView.setImageUriAsync(URI);
                }
                catch (Exception e)
                {
                    mCropImageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCropImageView.setImageUriAsync(mCropImageUri);
        } else {
            Toast.makeText(this, "Required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */

    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (Objects.requireNonNull(intent.getComponent()).getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    @Override
    public void onBackPressed() {
//        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }

    /**
     * Get URI to image received from capture by camera.
     */



    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap rotateImage(Bitmap sourceImage, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), matrix, true);
    }

    public static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }

                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }

                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public class BrightnessProcessTask extends AsyncTask<Void, Void, Boolean> {

        private  Bitmap image;
        private  int progressValue;

        private BrightnessProcessTask(Bitmap image, int progressValue) {
            this.image = image;
            this.progressValue = progressValue;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                tempBitmap = doBrightness(image, progressValue);
                Thread.sleep(100);
                return true;
            } catch (InterruptedException e) {
                dialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
//            mBrightnessProcessTask = null;
            dialog.dismiss();
            if (success) {
                mCropImageView.setImageBitmap(tempBitmap);
            }
            else{
                mCropImageView.setImageBitmap(originalBitmap);
                Toast.makeText(StudentiCardDetailsEdit.this,"One ERROR occured!!!",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
//            mBrightnessProcessTask = null;
            dialog.dismiss();
        }
    }

    public class RotateProcessTask extends AsyncTask<Void, Void, Boolean> {

        private  Bitmap image;
        private  int angle;

        private RotateProcessTask(Bitmap image, int angle) {
            this.image = image;
            this.angle = angle;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                originalBitmap = rotateImage(image, angle);
                tempBitmap = rotateImage(tempBitmap, angle);
                Thread.sleep(100);
                return true;
            } catch (InterruptedException e) {
                dialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRotateProcessTask = null;
            dialog.dismiss();
            if (success) {
                mCropImageView.setImageBitmap(tempBitmap);
            }
            else{
                mCropImageView.setImageBitmap(originalBitmap);
                Toast.makeText(StudentiCardDetailsEdit.this,"One ERROR occured!!!",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mRotateProcessTask = null;
            dialog.dismiss();
        }
    }

    private void ShiftDataGetRequest() {
        if (StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
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
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allShiftArrayList.add(shiftModel);
                shiftArrayList.add(shiftModel.getShiftName());
            }
            if(allShiftArrayList.size() == 1){
                selectedShift = allShiftArrayList.get(0);
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
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void MediumDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
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
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allMediumArrayList.add(mediumModel);
                mediumnArrayList.add(mediumModel.getMameName());
            }
            if(allMediumArrayList.size() == 1){
                selectedMedium = allMediumArrayList.get(0);
                ClassDataGetRequest();
            }
            try {
                String[] strings = new String[mediumnArrayList.size()];
                strings = mediumnArrayList.toArray(strings);
                ArrayAdapter<String> medium_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                medium_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMedium.setAdapter(medium_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No medium found !!!",Toast.LENGTH_LONG).show();
            }
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void ClassDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
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
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allClassArrayList.add(classModel);
                classArrayList.add(classModel.getClassName());
            }
            if(allClassArrayList.size() == 1){
                selectedClass = allClassArrayList.get(0);
                DepartmentDataGetRequest();
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
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void DepartmentDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
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
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allDepartmentArrayList.add(departmentModel);
                departmentArrayList.add(departmentModel.getDepartmentName());
            }
            if(allDepartmentArrayList.size() == 1){
                selectedDepartment = allDepartmentArrayList.get(0);
                SectionDataGetRequest();
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
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void SectionDataGetRequest() {
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection and select again!!! ",
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
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allSectionArrayList.add(sectionModel);
                sectionArrayList.add(sectionModel.getSectionName());
            }
            if(allSectionArrayList.size() == 1){
                selectedSection = allSectionArrayList.get(0);
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
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    public void GenderDataGetRequest(){
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getallgender()
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
                            parseGenderJsonData(response);
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseGenderJsonData(String jsonString) {
        try {
            allGenderArrayList = new ArrayList<>();
            JSONArray genderJsonArray = new JSONArray(jsonString);
            ArrayList<String> genderArrayList = new ArrayList<>();
            genderArrayList.add("Select Gender");
            for(int i = 0; i < genderJsonArray.length(); ++i) {
                JSONObject genderJsonObject = genderJsonArray.getJSONObject(i);
                GenderModel genderModel = new GenderModel(genderJsonObject.getString("GenderID"), genderJsonObject.getString("GenderName"));
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allGenderArrayList.add(genderModel);
                genderArrayList.add(genderModel.getGenderName());
            }
            try {
                String[] strings = new String[genderArrayList.size()];
                strings = genderArrayList.toArray(strings);
                ArrayAdapter<String> gender_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                gender_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGender.setAdapter(gender_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No gender found !!!",Toast.LENGTH_LONG).show();
            }
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    public void ReligionDataGetRequest(){
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getreligion()
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
                            parseReligionJsonData(response);
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseReligionJsonData(String jsonString) {
        try {
            allReligionArrayList = new ArrayList<>();
            JSONArray religionJsonArray = new JSONArray(jsonString);
            ArrayList<String> religionArrayList = new ArrayList<>();
            religionArrayList.add("Select Religion");
            for(int i = 0; i < religionJsonArray.length(); ++i) {
                JSONObject religionJsonObject = religionJsonArray.getJSONObject(i);
                ReligionModel religionModel = new ReligionModel(religionJsonObject.getString("ReligionID"), religionJsonObject.getString("ReligionName"));
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allReligionArrayList.add(religionModel);
                religionArrayList.add(religionModel.getReligionName());
            }
            try {
                String[] strings = new String[religionArrayList.size()];
                strings = religionArrayList.toArray(strings);
                ArrayAdapter<String> religion_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                religion_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReligion.setAdapter(religion_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No religion found !!!",Toast.LENGTH_LONG).show();
            }
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }


    public void BloodGroupDataGetRequest(){
        if(StaticHelperClass.isNetworkAvailable(StudentiCardDetailsEdit.this)) {
            dialog.show();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.baseUrl))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Observable<String> observable = retrofit
                    .create(RetrofitNetworkService.class)
                    .getbloodgroups()
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
                            parseBloodGroupJsonData(response);
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
            Toast.makeText(StudentiCardDetailsEdit.this,"Please check your internet connection!!! ",
                    Toast.LENGTH_LONG).show();
        }
    }

    void parseBloodGroupJsonData(String jsonString) {
        try {
            allBloodGroupArrayList = new ArrayList<>();
            JSONArray bloodGroupJsonArray = new JSONArray(jsonString);
            ArrayList<String> bloodGroupArrayList = new ArrayList<>();
            bloodGroupArrayList.add("Select Blood Group");
            for(int i = 0; i < bloodGroupJsonArray.length(); ++i) {
                JSONObject bloodGroupJsonObject = bloodGroupJsonArray.getJSONObject(i);
                BloodGroupModel bloodGroupModel = new BloodGroupModel(bloodGroupJsonObject.getString("BloodGroupID"), bloodGroupJsonObject.getString("BloodGroupName"));
//                Toast.makeText(this,classJsonObject.getString("ClassID")+classJsonObject.getString("ClassName"),Toast.LENGTH_LONG).show();
                allBloodGroupArrayList.add(bloodGroupModel);
                bloodGroupArrayList.add(bloodGroupModel.getBloodGroupName());
            }
            try {
                String[] strings = new String[bloodGroupArrayList.size()];
                strings = bloodGroupArrayList.toArray(strings);
                ArrayAdapter<String> bloodGroup_spinner_adapter = new ArrayAdapter<>(this,R.layout.spinner_item, strings);
                bloodGroup_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBloodGroup.setAdapter(bloodGroup_spinner_adapter);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this,"No blood group found !!!",Toast.LENGTH_LONG).show();
            }
            //spinner.setSelectedIndex(1);
        } catch (JSONException e) {
            Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
        }
    }

    private void CheckSelectedData(){
        if(selectedClass.getClassID() == -2) {
            selectedClass.setClassID("0");
        }if(selectedShift.getShiftID() == -2) {
            selectedShift.setShiftID("0");
        }if(selectedSection.getSectionID() == -2) {
            selectedSection.setSectionID("0");
        }if(selectedMedium.getMediumID() == -2) {
            selectedMedium.setMediumID("0");
        }if(selectedDepartment.getDepartmentID() == -2) {
            selectedDepartment.setDepartmentID("0");
        }
    }
}
