package app.unusa.app.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.orm.SugarContext;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import app.components.R;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

public class AddNewMemberUnasaActivity extends AppCompatActivity {
    private static final String TAG = "AddNewMemberUnasaActivi";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    MyFunctions functions;
    boolean isEditTask = false;
    private StorageReference mStorageRef;

    String editUserId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        functions = new MyFunctions(AddNewMemberUnasaActivity.this);
        setContentView(R.layout.activity_unasa_add_new_member);
        step_one();
        initToolbar();
    }

    boolean isAdmin = false;
    ImageView imgView_1;

    private void step_one() {
        SugarContext.init(AddNewMemberUnasaActivity.this);

        List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
        if (users.isEmpty()) {
            Toast.makeText(this, "Login before you proceed", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }

        for (UnusaUser u : users) {
            loggedInUser = u;
            if (loggedInUser.userType.equals("admin")) {
                isAdmin = true;
            } else {
                isAdmin = false;
            }
            break;
        }


        try {
            if (loggedInUser != null) {
                if (loggedInUser.username != null) {
                    if (loggedInUser.username.length() > 2) {
                        //initComponent_2();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "startMainDashboard: --> " + e.getMessage());
            onBackPressed();
        }


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("taskEdit")) {
                editUserId = extras.getString("taskEdit");
                if (editUserId.length() > 2) {
                    isEditTask = true;
                    fetchEditMember();
                } else {
                    bindViews();
                }

            } else {
                isEditTask = false;
                bindViews();
            }

        } else {
            bindViews();
        }
    }

    UnusaUser EditMember = new UnusaUser();
    boolean bind_called = false;

    private void fetchEditMember() {
        db.collection(usersDb).document(editUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(AddNewMemberUnasaActivity.this, "Member not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        EditMember = documentSnapshot.toObject(UnusaUser.class);
                        bindViews();
                    }
                });
    }


    UnusaUser newMember = new UnusaUser();
    AppCompatEditText
            first_name,
            last_name,
            address,
            nationality,
            password_view,
            occupation,
            email,
            phone_number;

    FloatingActionButton btnChoosePhoto;

    AppCompatEditText company_position, reg_num;

    AppCompatEditText next_mother_name, reg_date, next_father_name, next_mother_phone, next_father_phone, next_address;
    ImageView mother_photo_view;
    ImageView father_photo_view;
    Button bt_submit;
    String reg_date_val = "";

    void bindViews() {
        if (bind_called) {
            return;
        }
        bind_called = true;
        next_mother_name = findViewById(R.id.next_mother_name);
        reg_date = findViewById(R.id.reg_date);
        next_father_name = findViewById(R.id.next_father_name);
        father_photo_view = findViewById(R.id.father_photo_view);
        next_mother_phone = findViewById(R.id.next_mother_phone);
        next_father_phone = findViewById(R.id.next_father_phone);
        mother_photo_view = findViewById(R.id.mother_photo_view);
        bt_submit = findViewById(R.id.bt_submit);
        next_address = findViewById(R.id.next_address);
        imgView_1 = findViewById(R.id.imgView_1);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        dialog = new ProgressDialog(AddNewMemberUnasaActivity.this);
        first_name = findViewById(R.id.first_name);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        email = findViewById(R.id.email);
        last_name = findViewById(R.id.last_name);
        password_view = findViewById(R.id.password_view);
        imageView = findViewById(R.id.imageView);
        phone_number = findViewById(R.id.phone_number);
        occupation = findViewById(R.id.occupation);
        address = findViewById(R.id.address);
        company_position = findViewById(R.id.company_position);
        reg_num = findViewById(R.id.reg_num);
        nationality = findViewById(R.id.nationality);

        reg_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cur_calender = Calendar.getInstance();
                Calendar min_calender = Calendar.getInstance();
                min_calender.set(2019, 1, 1);
                DatePickerDialog datePicker = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                long date_ship_millis = calendar.getTimeInMillis();
                                reg_date_val = String.valueOf(date_ship_millis);
                                reg_date.setText(Tools.getFormattedDateSimple(date_ship_millis));
                            }
                        },
                        cur_calender.get(Calendar.YEAR),
                        cur_calender.get(Calendar.MONTH),
                        cur_calender.get(Calendar.DAY_OF_MONTH)
                );
                //set dark light
                datePicker.setThemeDark(false);
                datePicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
                datePicker.setMinDate(min_calender);
                datePicker.show(getFragmentManager(), "Pick date");
            }
        });

        if (isEditTask) {

            if (!isAdmin) {
                reg_num.setVisibility(View.GONE);
                company_position.setVisibility(View.GONE);
            } else {
                company_position.setVisibility(View.VISIBLE);
                reg_num.setVisibility(View.VISIBLE);

            }
            company_position.setText(EditMember.company_position);
            reg_num.setText(EditMember.reg_num);
            first_name.setText(EditMember.firstName);
            last_name.setText(EditMember.lastName);
            phone_number.setText(EditMember.phoneNumber);
            occupation.setText(EditMember.company);
            address.setText(EditMember.address);
            password_view.setText(EditMember.password);
            nationality.setText(EditMember.nationality);
            if (EditMember.regDate != null) {
                if (EditMember.regDate.length() > 3) {
                    try {
                        reg_date.setText(Tools.getFormattedDateSimple(
                                Long.valueOf(EditMember.regDate)
                        ));
                        reg_date_val = EditMember.regDate;
                    } catch (Exception e) {
                        reg_date_val = "";
                    }
                }
            }

            email.setText(EditMember.email);

            if (EditMember.next_mother_phone != null) {
                next_mother_phone.setText(EditMember.next_mother_phone);
            }


            if (EditMember.next_father_name != null) {
                next_father_name.setText(EditMember.next_father_name);
            }

            if (EditMember.next_father_phone != null) {
                next_father_phone.setText(EditMember.next_father_phone);
            }

            if (EditMember.next_mother_name != null) {
                next_mother_name.setText(EditMember.next_mother_name);
            }

            if (EditMember.next_address != null) {
                next_address.setText(EditMember.next_address);
            }


            if (EditMember.profilePhotoFather != null) {
                if (EditMember.profilePhotoFather.length() > 5) {
                    Glide.with(getApplicationContext())
                            .load(EditMember.profilePhotoFather)
                            .placeholder(R.drawable.user)
                            .centerCrop()
                            .into(father_photo_view);
                }
            }


            if (EditMember.profilePhoto != null) {
                if (EditMember.profilePhoto.length() > 5) {
                    Glide.with(getApplicationContext())
                            .load(EditMember.profilePhoto)
                            .placeholder(R.drawable.user)
                            .centerCrop()
                            .into(imgView_1);
                }
            }

            if (EditMember.profilePhotoMother != null) {
                if (EditMember.profilePhoto.length() > 5) {
                    Glide.with(getApplicationContext())
                            .load(EditMember.profilePhotoMother)
                            .placeholder(R.drawable.user)
                            .centerCrop()
                            .into(mother_photo_view);
                }
            }
        }
        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        imgView_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        mother_photo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageMother();
            }
        });
        father_photo_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageFather();
            }
        });

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditTask) {
                    updateUser();
                } else {
                    uploadNewUser();
                }
            }
        });

    }

    UnusaUser loggedInUser = new UnusaUser();
    private ImageView imageView;
    private Uri filePath;
    private Uri filePathMother;
    private Uri filePathFather;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int PICK_IMAGE_REQUEST_MOTHER = 72;
    private final int PICK_IMAGE_REQUEST_FATHER = 73;

    private void chooseImageMother() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_MOTHER);
    }

    private void chooseImageFather() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_FATHER);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgView_1.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_MOTHER && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePathMother = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePathMother);
                mother_photo_view.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_FATHER && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePathFather = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePathFather);
                father_photo_view.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int REQUEST_CODE = 1;
    StorageReference ref_main;
    StorageReference ref_mother;
    StorageReference ref_father;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void uploadImage() {


        if (filePath != null) {

            if (isEditTask) {
                ref_main = mStorageRef.child("images/" + EditMember.userId);
            } else {
                ref_main = mStorageRef.child("images/" + newMember.userId);
            }

            ref_main.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref_main.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (isEditTask) {
                                EditMember.profilePhoto = uri.toString() + "";
                            } else {
                                newMember.profilePhoto = uri.toString() + "";
                            }
                            if (isEditTask) {
                                Toast.makeText(AddNewMemberUnasaActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                                do_update_user_process();
                            } else {
                                addNewMEmberProcess();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to get photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                            if (isEditTask) {


                                do_update_user_process();
                            } else {
                                addNewMEmberProcess();
                            }
                            return;
                        }
                    });
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to upload photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (isEditTask) {
                        Toast.makeText(AddNewMemberUnasaActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                        do_update_user_process();
                    } else {
                        addNewMEmberProcess();
                    }
                    return;
                }
            });


        } else {
            if (isEditTask) {

                do_update_user_process();
            } else {
                addNewMEmberProcess();
            }
            return;
        }
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (isEditTask) {
            getSupportActionBar().setTitle("Editing profile");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.unusa_menu_done, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("Done")) {
            if (isEditTask) {
                updateUser();
            } else {
                uploadNewUser();
            }
        }

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            //Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateUser() {
        EditMember.firstName = functions.stringCapitalize(first_name.getText().toString());
        EditMember.lastName = functions.stringCapitalize(last_name.getText().toString());
        EditMember.address = functions.stringCapitalize(address.getText().toString());
        EditMember.company = functions.stringCapitalize(occupation.getText().toString());
        EditMember.phoneNumber = phone_number.getText().toString();
        EditMember.password = password_view.getText().toString();
        EditMember.username = EditMember.reg_num;

        if (isAdmin) {
            EditMember.company_position = company_position.getText().toString();
            EditMember.reg_num = reg_num.getText().toString();
            EditMember.username = newMember.reg_num;
        }

        EditMember.phoneNumber = phone_number.getText().toString();
        EditMember.nationality = functions.stringCapitalize(nationality.getText().toString());
        EditMember.email = email.getText().toString();

        if (reg_date_val.length() < 4) {
            Toast.makeText(this, "Pick registration date", Toast.LENGTH_SHORT).show();
            reg_date.performClick();
            return;
        }
        EditMember.regDate = reg_date_val;

        if (EditMember.password.length() < 4) {
            password_view.requestFocus();
            Toast.makeText(this, "Password too shot", Toast.LENGTH_SHORT).show();
            return;
        }

        if (EditMember.company_position.length() < 2) {
            EditMember.company_position = "Member";
        }

        if (EditMember.company_position.length() < 2) {
            EditMember.reg_num = "Not submited.";
            Toast.makeText(this, "Enter Registration number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (EditMember.email.length() < 2) {
            EditMember.email = "Not submited.";
        }

        if (EditMember.nationality.length() < 2) {
            EditMember.nationality = "Not submited.";
        }

        if (EditMember.company.length() < 2) {
            EditMember.company = "Not submited.";
        }

        if (EditMember.address.length() < 2) {
            EditMember.address = "No address.";
        }

        if ((EditMember.firstName.length() < 3) || (EditMember.lastName.length() < 3)) {
            Toast.makeText(this, "First or Last name too short", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((EditMember.phoneNumber.length() < 6)) {
            Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }


        EditMember.next_mother_name = next_mother_name.getText().toString() + "";

        if ((EditMember.next_mother_name.length() < 3)) {
            EditMember.next_mother_name = "";
        }

        EditMember.next_mother_phone = next_mother_phone.getText().toString() + "";

        if ((EditMember.next_mother_phone.length() < 3)) {
            Toast.makeText(this, "Mother's phone too short", Toast.LENGTH_LONG).show();
            next_mother_phone.requestFocus();
            return;
        }

        EditMember.next_father_name = next_father_name.getText().toString() + "";

        if ((EditMember.next_father_name.length() < 3)) {
            EditMember.next_father_name = "";
        }

        EditMember.next_father_phone = next_father_phone.getText().toString() + "";
        if ((EditMember.next_father_phone.length() < 4)) {
            EditMember.next_father_phone = "";
        }


        EditMember.next_address = next_address.getText().toString() + "";

        if ((EditMember.next_address.length() < 4)) {
            EditMember.next_address = "No Address";
        }


        showAlertDialog();
        if (filePath == null) {
            do_update_user_process();
        } else {
            uploadImage();
        }


    }

    private void do_update_user_process_3() {
        db.collection(usersDb).document(EditMember.userId).set(EditMember)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNewMemberUnasaActivity.this, "Profile updated successfully. ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewMemberUnasaActivity.this, "Failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void do_update_user_process_2() {
        if (filePathFather != null) {

            if (isEditTask) {
                ref_father = mStorageRef.child("images/" + EditMember.userId + "Father");
            } else {
                ref_father = mStorageRef.child("images/" + newMember.userId + "Father");
            }

            ref_father.putFile(filePathFather).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref_father.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (isEditTask) {
                                EditMember.profilePhotoFather = uri.toString() + "";
                            } else {
                                newMember.profilePhotoFather = uri.toString() + "";
                            }
                            if (isEditTask) {

                                do_update_user_process_3();
                            } else {
                                addNewMEmberProcess();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to get photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                            if (isEditTask) {

                                do_update_user_process_3();
                            } else {
                                addNewMEmberProcess();
                            }
                            return;
                        }
                    });
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to upload photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (isEditTask) {

                        do_update_user_process_3();
                    } else {
                        addNewMEmberProcess();
                    }
                    return;
                }
            });


        } else {
            if (isEditTask) {


                do_update_user_process_3();
            } else {
                addNewMEmberProcess();
            }
            return;
        }
    }

    private void do_update_user_process() {

        if (filePathMother != null) {

            if (isEditTask) {
                ref_mother = mStorageRef.child("images/" + EditMember.userId + "Mother");
            } else {
                ref_mother = mStorageRef.child("images/" + newMember.userId + "Mother");
            }

            ref_mother.putFile(filePathMother).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref_mother.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (isEditTask) {
                                EditMember.profilePhotoMother = uri.toString() + "";
                            } else {
                                newMember.profilePhotoMother = uri.toString() + "";
                            }
                            if (isEditTask) {

                                do_update_user_process_2();
                            } else {
                                addNewMEmberProcess();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to get photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                            if (isEditTask) {

                                do_update_user_process_2();
                            } else {
                                addNewMEmberProcess();
                            }
                            return;
                        }
                    });
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddNewMemberUnasaActivity.this, "Failed to upload photo name because " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (isEditTask) {

                        do_update_user_process_2();
                    } else {
                        addNewMEmberProcess();
                    }
                    return;
                }
            });


        } else {
            if (isEditTask) {


                do_update_user_process_2();
            } else {
                addNewMEmberProcess();
            }
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void uploadNewUser() {
        /*uploadImage();
        if (true)
            return;*/
        newMember.firstName = functions.stringCapitalize(first_name.getText().toString());
        newMember.lastName = functions.stringCapitalize(last_name.getText().toString());
        newMember.address = functions.stringCapitalize(address.getText().toString());
        newMember.company = functions.stringCapitalize(occupation.getText().toString());
        newMember.phoneNumber = phone_number.getText().toString();
        newMember.nationality = functions.stringCapitalize(nationality.getText().toString());
        newMember.email = email.getText().toString();
        newMember.reg_num = reg_num.getText().toString();
        newMember.company_position = company_position.getText().toString();
        newMember.password = password_view.getText().toString();

        newMember.next_father_name = next_father_name.getText().toString();
        newMember.next_father_phone = next_father_phone.getText().toString();
        newMember.next_address = next_address.getText().toString();
        newMember.next_mother_phone = next_mother_phone.getText().toString();
        newMember.next_mother_name = next_mother_name.getText().toString();

        if ((newMember.firstName.length() < 3)) {
            first_name.requestFocus();
            Toast.makeText(this, "First name too short", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reg_date_val.length() < 4) {
            Toast.makeText(this, "Pick registration date", Toast.LENGTH_SHORT).show();
            reg_date.performClick();
            return;
        }
        newMember.regDate = reg_date_val;


        if (newMember.password.length() < 4) {
            Toast.makeText(this, "Password too short.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newMember.reg_num.length() < 2) {
            Toast.makeText(this, "Enter Reg No.", Toast.LENGTH_SHORT).show();
            reg_num.requestFocus();
            return;
        }

        if (newMember.company_position.length() < 2) {
            Toast.makeText(this, "Enter Position.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newMember.email.length() < 2) {
            newMember.email = "Not submited.";
        }

        if (newMember.nationality.length() < 2) {
            newMember.nationality = "Not submited.";
        }

        if (newMember.company.length() < 2) {
            newMember.company = "Not submited.";
        }

        if (newMember.address.length() < 2) {
            newMember.address = "No address.";
        }



        if ((newMember.phoneNumber.length() < 6)) {
            newMember.phoneNumber = "";
        }

        newMember.userId = db.collection(usersDb).document().getId();
        newMember.profilePhoto = "";

        EditMember.phoneNumber = phone_number.getText().toString();
        newMember.username = newMember.reg_num;
        newMember.password = "2020";
        newMember.userType = "regular";

        validatePhoneNumber();
    }

    private void validatePhoneNumber() {
        showAlertDialog();
        db.collection(usersDb).whereEqualTo("reg_num", newMember.reg_num)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            if (filePath == null) {
                                addNewMEmberProcess();
                            } else {
                                uploadImage();
                            }

                            return;
                        } else {
                            Toast.makeText(AddNewMemberUnasaActivity.this, "Registration number exist.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            dialog.dismiss();
                            return;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewMemberUnasaActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewMEmberProcess() {
        db.collection(usersDb).document(newMember.userId).set(newMember)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNewMemberUnasaActivity.this, "New member added successfully. ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewMemberUnasaActivity.this, "Failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }


    ProgressDialog dialog;

    private void showAlertDialog() {
        if (isEditTask) {
            dialog.setTitle("Updating profile");
        } else {
            dialog.setTitle("Adding New Member");
        }
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }

    private void hideAlertDialog() {
        /*builder.clos
        builder.setTitle("Please wait...");
        builder.setCancelable(false);
        builder.show();*/
    }


}
