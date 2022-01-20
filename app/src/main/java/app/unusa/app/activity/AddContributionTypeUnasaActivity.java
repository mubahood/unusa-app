package app.unusa.app.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.Calendar;

import app.components.R;
import app.unusa.app.model.UnusaContributionType;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

import static app.unusa.app.utils.MyFunctions.hideKeyboard;


public class AddContributionTypeUnasaActivity extends AppCompatActivity {
    private RelativeLayout progress_bar;
    NestedScrollView nested_scroll_view;

    private static final String TAG = "AddContributionUnasaAct";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AddContributionTypeUnasaActivity context;

    AppCompatEditText contribution_title;
    AppCompatEditText transaction_details;
    AppCompatEditText contribution_target;
    Button start_month, end_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_add_contribution_type);
        context = AddContributionTypeUnasaActivity.this;
        myFunctions = new MyFunctions(context);
        initToolbar();
        bindViews();
    }


    RelativeLayout contribution_progress_bar;

    void bindViews() {
        contributionType = new UnusaContributionType();
        contribution_title = findViewById(R.id.contribution_title);
        transaction_details = findViewById(R.id.transaction_details);
        contribution_target = findViewById(R.id.contribution_target);
        start_month = findViewById(R.id.start_month);
        end_month = findViewById(R.id.end_month);
        contribution_progress_bar = findViewById(R.id.contribution_progress_bar);

        start_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMonthStart();
            }
        });
        end_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMonthEnd();
            }
        });
    }


    String userId;


    private void setMonthEnd() {
        final Calendar today = Calendar.getInstance();
        final int year = today.get(Calendar.YEAR);
        final int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        hideKeyboard(AddContributionTypeUnasaActivity.this);

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(
                context,
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        contributionType.endMonth = selectedMonth + 1;
                        contributionType.endYear = selectedYear;

                        end_month.setText(
                                myFunctions.tellMonthShort(contributionType.endMonth) +
                                        " - "
                                        + contributionType.endYear
                        );

                    }
                }, year, month);

        builder.setActivatedMonth(month)
                .setMinYear(2019)
                .setActivatedYear(year)
                .setMaxYear(today.get(Calendar.YEAR) + 1)
                .setTitle("Contribution End Month")
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


    private void setMonthStart() {
        final Calendar today = Calendar.getInstance();
        final int year = today.get(Calendar.YEAR);
        final int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        hideKeyboard(AddContributionTypeUnasaActivity.this);


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

                        contributionType.startMonth = monthOfYear + 1;
                        contributionType.startYear = year;

                        long date_ship_millis = calendar.getTimeInMillis();
                        contributionType.dateCreated = String.valueOf(date_ship_millis);
                        start_month.setText(Tools.getFormattedDateSimple(date_ship_millis));
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


    MyFunctions myFunctions;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Adding New Contribution");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {

            validateData();
            //Toast.makeText(getApplicationContext(), "Please Wait...", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    String contributionDb = "contributionTypes";
    UnusaContributionType contributionType;

    boolean isReady = true;

    private void validateData() {
        isReady = true;
        contributionType.contributionId = db.collection(contributionDb).document().getId();
        contributionType.title = contribution_title.getText().toString();
        contributionType.details = transaction_details.getText().toString();
        try {
            contributionType.contribution_target = Integer.parseInt(contribution_target.getText().toString());
        } catch (Exception e) {
            Toast.makeText(context, "Please specify Contribution target", Toast.LENGTH_LONG).show();
            return;
        }


        if (
                (contributionType.startYear < 1) ||
                        (contributionType.endYear < 1) ||
                        (contributionType.endMonth < 1) ||
                        (contributionType.startMonth < 1)
        ) {
            Toast.makeText(context, "Select valid date range", Toast.LENGTH_SHORT).show();
            isReady = false;
            return;
        }

        if (contributionType.dateCreated.length() < 3) {
            Toast.makeText(context, "Select dateCreated date", Toast.LENGTH_SHORT).show();
            isReady = false;
            return;
        }

        if (contributionType.endYear < contributionType.startYear) {
            Toast.makeText(context, "Select valid date range", Toast.LENGTH_SHORT).show();
            isReady = false;
            return;
        }

        if (contributionType.endYear == contributionType.startYear) {
            if (contributionType.endMonth < contributionType.startMonth) {
                Toast.makeText(context, "Select valid date range", Toast.LENGTH_SHORT).show();
                isReady = false;
                return;
            }
        }

        if (contributionType.title.length() < 2) {
            Toast.makeText(context, "Enter title.", Toast.LENGTH_SHORT).show();
            isReady = false;
            return;
        }

        contributionType.isOpen = true;

        if (isReady) {
            Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
            contribution_progress_bar.setVisibility(View.VISIBLE);

            db.collection(contributionDb).whereEqualTo("title", contributionType.title)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                contribution_progress_bar.setVisibility(View.GONE);
                                Toast.makeText(context, "Title already exist.", Toast.LENGTH_LONG).show();
                                return;
                            } else {

                                db.collection(contributionDb).document(contributionType.contributionId)
                                        .set(contributionType)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Added Successfully.", Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        contribution_progress_bar.setVisibility(View.GONE);
                                    }
                                });


                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    contribution_progress_bar.setVisibility(View.GONE);
                }
            });


        }

    }


}
