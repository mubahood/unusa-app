package app.unusa.app.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import app.components.R;
import app.unusa.app.model.UnusaContributionType;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;
import com.orm.SugarContext;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static app.unusa.app.utils.MyFunctions.hideKeyboard;

public class AddOtherContributionUnasaActivity extends AppCompatActivity {
    private RelativeLayout progress_bar;
    NestedScrollView nested_scroll_view;

    private static final String TAG = "AddContributionUnasaAct";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    String transactionDb = "transactions";
    UnusaUser memberResponsible;
    AddOtherContributionUnasaActivity context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_add_other_contribution_unasa);
        step_one();
        getExtras();
        context = AddOtherContributionUnasaActivity.this;
    }


    void getExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("userId")) {
                this.userId = getIntent().getStringExtra("userId");
            } else {
                Toast.makeText(context, "Select member", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (extras.containsKey("contributionId")) {
                this.contributionId = getIntent().getStringExtra("contributionId");
            } else {
                Toast.makeText(context, "Select contribution", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    String userId = "";
    String contributionId = "";

    // ftech data of user
    private void step_one() {
        bindViews();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("userId")) {
                this.userId = getIntent().getStringExtra("userId");
                step_two();
            } else {
                Toast.makeText(context, "Select Member", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        } else {
            Toast.makeText(context, "Select Member", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    // fetching loggedin User
    private void step_four() {
        initToolbar();
        SugarContext.init(getApplicationContext());
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                return;
            }
            for (UnusaUser u : users) {
                loggedInUser = u;
                break;
            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            if (!loggedInUser.userType.equals("admin")) {
                Toast.makeText(context, "You are not admin", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            return;
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "startMainDashboard: --> " + e.getMessage());
        }

    }

    UnusaUser loggedInUser = new UnusaUser();

    private void step_two() {
        Task<QuerySnapshot> query = db.collection(usersDb).whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(context, "No Member found.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            return;
                        }
                        ArrayList<UnusaUser> users = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                        if (users.isEmpty()) {
                            Toast.makeText(context, "No Member found.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            return;
                        }


                        memberResponsible = new UnusaUser();
                        for (UnusaUser u : users) {
                            if (u.userId.equals(userId)) {
                                memberResponsible = u;
                                progress_bar.setVisibility(View.GONE);
                                nested_scroll_view.setVisibility(View.VISIBLE);
                                name.setText(memberResponsible.firstName + " " + memberResponsible.lastName);
                                step_three();
                                return;
                            }
                        }

                        Toast.makeText(context, "Member not found.", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        return;

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "FAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        return;
                    }
                });
    }


    AppCompatEditText name, amount, transactionDetails;
    Button dueMonth;

    // binding views
    RelativeLayout success_dialog;
    TextView success_date;
    TextView success_time;
    TextView success_phone;
    TextView success_recorded_by;
    TextView success_name;
    TextView success_amount;
    FloatingActionButton success_fab;

    public void bindViews() {
        myFunctions = new MyFunctions(context);
        transacton = new UnusaTransacton();
        name = findViewById(R.id.name);
        success_dialog = findViewById(R.id.success_dialog);
        success_time = findViewById(R.id.success_time);
        success_phone = findViewById(R.id.success_phone);
        success_amount = findViewById(R.id.success_amount);
        success_fab = findViewById(R.id.success_fab);
        success_recorded_by = findViewById(R.id.success_recorded_by);
        success_dialog.setVisibility(View.GONE);
        transactionDetails = findViewById(R.id.transactionDetails);
        dueMonth = findViewById(R.id.dueMonth);
        success_name = findViewById(R.id.success_name);
        amount = findViewById(R.id.amount);
        success_date = findViewById(R.id.success_date);
        progress_bar = findViewById(R.id.contribution_progress_bar);
        nested_scroll_view = findViewById(R.id.nested_scroll_view);
        nested_scroll_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);


        dueMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMonth();
            }
        });

        success_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    private void showDialogPaymentSuccess() {
        success_dialog.setVisibility(View.VISIBLE);
        success_date.setText(myFunctions.toDateOne(transacton.dateRecorded));
        success_time.setText(myFunctions.toTimeOne(transacton.dateRecorded));
        success_name.setText(transacton.personResponsible.firstName + " " + transacton.personResponsible.lastName);
        success_phone.setText(transacton.personResponsible.phoneNumber);
        success_amount.setText("R. " + transacton.transactionAmount);
        success_recorded_by.setText(myFunctions.stringCapitalize(transacton.recordedBy.firstName + " " + transacton.recordedBy.lastName));
        //newFragment = new DialogPaymentSuccessFragment();
    }


    private void setMonth() {
        final Calendar today = Calendar.getInstance();
        final int year = today.get(Calendar.YEAR);
        final int month = today.get(Calendar.MONTH) ;
        int day = today.get(Calendar.DAY_OF_MONTH);

        hideKeyboard(AddOtherContributionUnasaActivity.this);

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(
                context,
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        transacton.dueMonth = selectedMonth;
                        transacton.dueMonth = transacton.dueMonth + 1;
                        transacton.dueYear = selectedYear;
                        dueMonth.setText(myFunctions.tellMonthShort(transacton.dueMonth) + " - " + " " + transacton.dueYear);
                    }
                }, year, month);

        builder.setActivatedMonth(month )
                .setMinYear(2019)
                .setActivatedYear(year)
                .setMaxYear(today.get(Calendar.YEAR) + 1)
                .setTitle("Contribution Due Month")
                .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                .setOnMonthChangedListener(new MonthPickerDialog.OnMonthChangedListener() {
                    @Override
                    public void onMonthChanged(int selectedMonth) {
                        // on month selected
                    }
                }).setOnYearChangedListener(new MonthPickerDialog.OnYearChangedListener() {
            @Override
            public void onYearChanged(int selectedYear) {
                // on year selected
            }
        })
                .build()
                .show();
    }


    MyFunctions myFunctions;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("" + contributionType.title.toString());
        getSupportActionBar().setSubtitle(contributionType.details.toString());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }


    private void submitAction() {


        db.collection(transactionDb).document(transacton.transactionId).set(transacton)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        showDialogPaymentSuccess();


                        progress_bar.setVisibility(View.GONE);
                        nested_scroll_view.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progress_bar.setVisibility(View.GONE);
                nested_scroll_view.setVisibility(View.VISIBLE);
            }
        });


    }


    UnusaContributionType contributionType;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            step_five();
            //Toast.makeText(getApplicationContext(), "Please Wait...", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    UnusaTransacton transacton;

    String contributionDb = "contributionTypes";

    //Validating
    private void step_three() {
        contributionType = new UnusaContributionType();
        db.collection(contributionDb).document(contributionId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(context, "Contribution does not exist", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        contributionType = documentSnapshot.toObject(UnusaContributionType.class);
                        step_four();
                    }
                });
    }


    private void step_five() {
        if ((transacton.dueMonth < 1) || (transacton.dueYear < 2019)) {
            Toast.makeText(context, "Select due month", Toast.LENGTH_SHORT).show();
            setMonth();
            return;
        }
        transacton.transactionAmount = 0;

        try {
            transacton.transactionAmount = Integer.parseInt(amount.getText().toString());
        } catch (Exception e) {
            Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        transacton.transactionId = db.collection(transactionDb).document().getId();
        transacton.recordedBy = loggedInUser;
        transacton.personResponsible = memberResponsible;
        transacton.transactionType = contributionType.title;
        transacton.dateRecorded = myFunctions.getTimeStamp();
        transacton.transactionDetails = transactionDetails.getText().toString();
        transacton.isIncome = true;
        transacton.personResponsibleId = memberResponsible.userId;
        transacton.recordedById = loggedInUser.userId;
        validate_members();
    }

    private void validate_members() {
        progress_bar.setVisibility(View.VISIBLE);
        nested_scroll_view.setVisibility(View.GONE);

        Task<QuerySnapshot> query = db.collection(transactionDb)
                .whereEqualTo("dueMonth", transacton.dueMonth)
                .whereEqualTo("transactionType", transacton.transactionType)
                .whereEqualTo("personResponsibleId", transacton.personResponsibleId)
                .whereEqualTo("dueYear", transacton.dueYear)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            submitAction();
                            return;
                        } else {
                            Toast.makeText(context, "Paid this contribution already.", Toast.LENGTH_SHORT).show();
                            nested_scroll_view.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                            return;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        nested_scroll_view.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                        return;
                    }
                });
        //submitAction();
    }
}


