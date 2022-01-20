package app.unusa.app.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.orm.SugarContext;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.components.R;
import app.unusa.app.adapter.UnusaTransactionsAdapter;
import app.unusa.app.fragment.FragmentBottomSheetDialogFull;
import app.unusa.app.model.UnusaContributionType;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

import static app.unusa.app.utils.MyFunctions.hideKeyboard;
import static app.unusa.app.utils.MyFunctions.toDateTwo;

public class UnusaTransactionsActivity extends AppCompatActivity {

    private static final String TAG = "MUBAHOOOOOD";
    RelativeLayout filter_dialog;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    String transactionDb = "transactions";
    UnusaTransactionsActivity context;
    boolean isSpecificDueMonth = false;
    String temp = "";
    String contributionId = "";
    boolean isCustomeTransaction = false;
    ArrayList<UnusaTransacton> transactons = new ArrayList<>();
    ArrayList<UnusaUser> allUsers = new ArrayList<>();
    MyFunctions functions;
    int defaultDueMonth;
    int defaultDueYear;
    boolean isAdmin = false;
    UnusaUser loggedInUser = new UnusaUser();
    LinearLayout main_container, no_data;
    RelativeLayout main_loader;
    Button filter_dueMonth;
    AppCompatButton apply_filter_button;
    FloatingActionButton filter_fab;
    String title = "";
    boolean filtered = false;
    boolean readyToShare = false;
    String shareBody = "";
    ArrayList<UnusaUser> paidMembers = new ArrayList<>();
    ArrayList<UnusaUser> notPaidMembers = new ArrayList<>();
    ArrayList<String> paidMembersIds = new ArrayList<>();
    ArrayList<String> notPaidMembersIds = new ArrayList<>();
    int paid_members_num = 0;
    int total_income = 0;
    int total_expenditure = 0;
    int myCounter = 0;
    private RecyclerView recyclerView;
    private UnusaTransactionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = UnusaTransactionsActivity.this;
        functions = new MyFunctions(context);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            try {

                if (intent.hasExtra("contributionId")) {
                    contributionId = intent.getStringExtra("contributionId");
                }
                if (contributionId.length() > 0) {
                    isCustomeTransaction = true;
                    get_contribution();
                }
            } catch (Exception e) {
                isCustomeTransaction = false;
                Toast.makeText(context, " ==> " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }


            if ((intent.hasExtra("defaultDueMonth")) && intent.hasExtra("defaultDueYear")) {

                try {
                    if (getIntent().hasExtra("defaultDueYear")) {
                        defaultDueMonth = Integer.valueOf(getIntent().getStringExtra("defaultDueMonth"));
                        isSpecificDueMonth = true;
                    } else {
                        isSpecificDueMonth = false;
                    }
                } catch (Exception e) {
                    defaultDueMonth = functions.thisMonth;
                    Log.d(TAG, "onCreate: 222");
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                try {
                    if (getIntent().hasExtra("defaultDueYear")) {
                        defaultDueYear = Integer.valueOf(getIntent().getStringExtra("defaultDueYear"));
                        isSpecificDueMonth = true;
                    } else {
                        isSpecificDueMonth = false;
                    }


                } catch (Exception e) {
                    defaultDueYear = functions.thisYear;
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCreate: 333");
                }

            } else {
                defaultDueMonth = functions.thisMonth;
                defaultDueYear = functions.thisYear;
            }
        } else {
            isCustomeTransaction = false;
            defaultDueMonth = functions.thisMonth;
            defaultDueYear = functions.thisYear;
        }


        setContentView(R.layout.activity_bottom_sheet_full);

        bindViews();
        step_one();

        //initComponent();
        //initToolbar();
        //Toast.makeText(this, "Swipe up bottom sheet", Toast.LENGTH_SHORT).show();
    }


    private void get_contribution() {
        db.collection("contributionTypes")
                .whereEqualTo("title", contributionId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(context, "Specific contribution not found " + contributionId, Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        is_fully_loaded = true;
                        contributionType = queryDocumentSnapshots.toObjects(UnusaContributionType.class).get(0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to get specific contribution", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        });
    }

    boolean is_fully_loaded = false;
    UnusaContributionType contributionType = null;

    // fetching loggedin User
    @SuppressLint("LongLogTag")
    private void step_one() {

        SugarContext.init(getApplicationContext());
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                return;
            }
            for (UnusaUser u : users) {
                loggedInUser = u;

            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            if (!loggedInUser.userType.equals("admin")) {
                isAdmin = true;
                //Toast.makeText(context, "You are not admin", Toast.LENGTH_SHORT).show();
                //onBackPressed();
            }

            step_two();
            return;
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: 444");
        }

    }

    ArrayList<UnusaTransacton> temp_transactons = new ArrayList<>();

    private void step_two() {
        Query query;
        if (isCustomeTransaction) {
            query = db.collection(transactionDb)
                    .whereEqualTo("transactionType", contributionId);
        } else if (isSpecificDueMonth) {
            query = db.collection(transactionDb);
        } else {
            query = db.collection(transactionDb);
        }

        if (!isCustomeTransaction) {
            query = db.collection(transactionDb);
        }

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            main_loader.setVisibility(View.GONE);
                            no_data.setVisibility(View.VISIBLE);
                            initToolbar();
                            Toast.makeText(context, "No data to display.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (isSpecificDueMonth) {
                            temp_transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                            for (UnusaTransacton t : temp_transactons) {
                                if (t.dueMonth == defaultDueMonth && t.dueYear == defaultDueYear) {
                                    transactons.add(t);
                                }
                            }
                        } else {
                            transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                        }

                        if (transactons.isEmpty()) {
                            main_loader.setVisibility(View.GONE);
                            no_data.setVisibility(View.VISIBLE);
                            initToolbar();
                            Toast.makeText(context, "No data to display.", Toast.LENGTH_SHORT).show();
                            //return;
                        }

                        db.collection(usersDb).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    allUsers = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                                    Collections.sort(transactons, new CustomComparator());
                                    initToolbar();
                                    initComponent();
                                } else {
                                    main_loader.setVisibility(View.GONE);
                                    no_data.setVisibility(View.VISIBLE);
                                    initToolbar();
                                    Toast.makeText(context, "No data to display.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        });

                        if (isCustomeTransaction) {
                            title = contributionId;
                        } else {
                            title = functions.tellMonthShort(defaultDueMonth) + " of " + defaultDueYear;
                        }

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

    private void bindViews() {

        filter_fab = findViewById(R.id.filter_fab);
        filter_dueMonth = findViewById(R.id.filter_dueMonth);
        apply_filter_button = findViewById(R.id.apply_filter_button);
        filter_dialog = findViewById(R.id.filter_dialog);
        filter_dialog.setVisibility(View.GONE);
        main_container = findViewById(R.id.main_container);
        no_data = findViewById(R.id.no_data);
        main_loader = findViewById(R.id.main_loader);
        main_loader.setVisibility(View.VISIBLE);
        no_data.setVisibility(View.GONE);

        apply_filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filtered) {
                    openTransactions();
                } else {
                    Toast.makeText(context, "Select due month", Toast.LENGTH_SHORT).show();
                    setMonth();
                }
            }
        });

        filter_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_dialog.setVisibility(View.GONE);
                if (transactons.isEmpty()) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    main_container.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void openTransactions() {
        Intent intent = new Intent(UnusaTransactionsActivity.this, UnusaTransactionsActivity.class);
        defaultDueMonth++;

        intent.putExtra("defaultDueMonth", defaultDueMonth + "");
        intent.putExtra("defaultDueYear", defaultDueYear + "");

        UnusaTransactionsActivity.this.startActivity(intent);
        finish();
    }

    private void initComponent() {
        filter_dueMonth.setText(functions.tellMonthShort(defaultDueMonth) + " of " + defaultDueYear);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        main_loader.setVisibility(View.GONE);


        //set data and list adapter
        adapter = new UnusaTransactionsAdapter(this, transactons);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new UnusaTransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, UnusaTransacton obj, int pos) {
                FragmentBottomSheetDialogFull fragment = new FragmentBottomSheetDialogFull();
                fragment.setPeople(obj, context, functions, isAdmin);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });

        filter_dueMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMonth();
            }
        });

        // display first sheet
        //FragmentBottomSheetDialogFull fragment = new FragmentBottomSheetDialogFull();
        //fragment.setPeople(adapter.getItem(0), context, functions, isAdmin);
        //fragment.show(getSupportFragmentManager(), fragment.getTag());
    }

    private void setMonth() {
        final Calendar today = Calendar.getInstance();
        final int year = today.get(Calendar.YEAR);
        final int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        hideKeyboard(UnusaTransactionsActivity.this);
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(
                context,
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        filtered = true;
                        defaultDueMonth = selectedMonth;
                        defaultDueYear = selectedYear;
                    }
                }, year, month);

        builder.setActivatedMonth(month)
                .setMinYear(2019)
                .setActivatedYear(year)
                .setMaxYear(today.get(Calendar.YEAR) + 1)
                .setTitle("Transactions Due Month")
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

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (isCustomeTransaction) {
            actionBar.setTitle("" + contributionId + "");
        } else {
            actionBar.setTitle("" + functions.tellMonthShort(defaultDueMonth) + " of " + defaultDueYear);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unasa_transactions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            //Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }

        if (item.getTitle().equals("Add Expense")) {
            if (loggedInUser.userType.equals("admin")) {
                Intent i = new Intent(context, AddExpenseUnasaActivity.class);
                context.startActivity(i);
                finish();
            } else {
                Toast.makeText(context, "Unauthorized access", Toast.LENGTH_SHORT).show();
            }
        }

        if (item.getTitle().equals("Share")) {
            if (loggedInUser.userType.equals("admin")) {
                prepare_share_data();
            } else {
                Toast.makeText(context, "Only admins can share data", Toast.LENGTH_LONG).show();

            }
        }

        if (item.getTitle().equals("Search")) {
            main_container.setVisibility(View.GONE);
            main_loader.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
            filter_dialog.setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    private void prepare_share_data() {

        if (isCustomeTransaction) {
            title = contributionId;
        } else {
            title = functions.tellMonthShort(defaultDueMonth) + " of " + defaultDueYear;
        }

        if (isCustomeTransaction) {
            if (!is_fully_loaded) {
                get_contribution();
                return;
            }
        }

        shareBody += "*U N U S A*\n\nTransactions of *" + title + "*. As on " + functions.toDateOne(functions.getTimeStamp()) + "" +
                " at " + functions.toTimeOne(functions.getTimeStamp()) + ".\n";

        for (UnusaTransacton t : transactons) {

            if (isCustomeTransaction) {
                if (t.isIncome) {
                    if (!paidMembersIds.contains(t.personResponsible.userId)) {
                        paidMembers.add(t.personResponsible);
                        paidMembersIds.add(t.personResponsible.userId);
                    }
                }
            }

            if ((t.dueMonth == defaultDueMonth) && (t.dueYear == defaultDueYear)) {
                if (isCustomeTransaction) {
                    if (t.isIncome && t.transactionType.equals("Monthly Contribution")) {
                        if (!paidMembersIds.contains(t.personResponsible.userId)) {
                            paidMembers.add(t.personResponsible);
                            paidMembersIds.add(t.personResponsible.userId);
                        }
                    }
                } else {
                    if (t.isIncome) {
                        if (!paidMembersIds.contains(t.personResponsible.userId)) {
                            paidMembers.add(t.personResponsible);
                            paidMembersIds.add(t.personResponsible.userId);
                        }
                    }
                }
            }

            if (t.isIncome) {
                total_income += t.transactionAmount;
            } else {
                total_expenditure += t.transactionAmount;
            }
        }

        paid_members_num = paidMembers.size();


        for (UnusaUser u : allUsers) {
            if (!paidMembersIds.contains(u.userId)) {
                if (isCustomeTransaction) {
                    int trans_day = toDateTwo(contributionType.dateCreated, "d");
                    int reg_day = toDateTwo(u.regDate, "d");

                    int trans_month = toDateTwo(contributionType.dateCreated, "m");
                    int reg_month = toDateTwo(u.regDate, "m");

                    int trans_year = toDateTwo(contributionType.dateCreated, "y");
                    int reg_year = toDateTwo(u.regDate, "y");

                    Log.d(TAG, "prepare_share_data: " +
                            ", trans_year: " + trans_year +
                            ", reg_year: " + reg_year +
                            "\n\n");

                    is_greater = false;

                    if (trans_year > reg_year) {
                        if (trans_month > reg_month) {
                            if (trans_day > reg_day) {
                                is_greater = true;
                            } else {
                                is_greater = false;
                            }
                        } else {
                            is_greater = false;
                        }

                    } else {
                        is_greater = false;
                    }


                    if (is_greater) {
                        Log.d(TAG, "prepare_share_data: " + u.firstName + " " +
                                " " + u.lastName + " " +
                                " " + Long.valueOf(u.regDate) +
                                " " + Long.valueOf(contributionType.dateCreated) +
                                "\n====\n"
                        );
                        if (!notPaidMembersIds.contains(u.userId)) {
                            notPaidMembersIds.add(u.userId);
                            notPaidMembers.add(u);
                        }
                        //shareBody += "\n" + myCounter + ". " + functions.stringCapitalize(u.firstName) + " " + functions.stringCapitalize(u.lastName);
                    }
                } else {
                    if (!notPaidMembersIds.contains(u.userId)) {


                        notPaidMembersIds.add(u.userId);
                        notPaidMembers.add(u);
                    }
                }
            }


        }

        shareBody += "\n*TOTAL INCOME   :* R. " + total_income;

        if (!isCustomeTransaction)
            shareBody += "\n*TOTAL EXPENSE :* R. " + total_expenditure;

        if (!isCustomeTransaction)
            shareBody += "\n\n*EXPECTED AMOUNT :* R. " + (allUsers.size() * 100);

        if (!isCustomeTransaction)
            shareBody += "\n*NOT PAID AMOUNT   :* R. " + (notPaidMembers.size() * 100);

        shareBody += "\n\n*PAID MEMBERS     :* " + (paidMembers.size());
        shareBody += "\n*NOT PAID MEMBERS  :* " + notPaidMembers.size();

        shareBody += "\n\n*MEMBERS PAID* (" + paidMembers.size() + ")";
        myCounter = 0;
        for (UnusaUser u : paidMembers) {
            myCounter++;
            shareBody += "\n" + myCounter + ". " + functions.stringCapitalize(u.firstName) + " " + functions.stringCapitalize(u.lastName);
        }

        shareBody += "\n\n*MEMBERS NOT PAID* (" + notPaidMembers.size() + ")";
        myCounter = 0;
        for (UnusaUser u : notPaidMembers) {
            myCounter++;
            if (!paidMembersIds.contains(u.userId)) {
                shareBody += "\n" + myCounter + ". " + functions.stringCapitalize(u.firstName) + " " + functions.stringCapitalize(u.lastName);

            }
        }

        shareBody += "\n\n*NOT PAID (JOINED LATE)* ";
        myCounter = 0;
        for (UnusaUser u : allUsers) {

            if (!paidMembersIds.contains(u.userId)) {
                if (!notPaidMembersIds.contains(u.userId)) {
                    myCounter++;
                    shareBody += "\n" + myCounter + ". " + functions.stringCapitalize(u.firstName) + " " + functions.stringCapitalize(u.lastName);
                }
            }
        }


        shareBody += "\n\n_Generated by UNUSA APP as on " + functions.toDateOne(functions.getTimeStamp()) + "" +
                " at " + functions.toTimeOne(functions.getTimeStamp()) + ", By " +
                functions.stringCapitalize(loggedInUser.firstName) + " " +
                functions.stringCapitalize(loggedInUser.lastName) + "._";


        //paid_members_num
        //allUsers
        share_data();
    }

    boolean is_greater = false;

    private void share_data() {


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, title));

    }

    public class CustomComparator implements Comparator<UnusaTransacton> {
        @Override
        public int compare(UnusaTransacton o1, UnusaTransacton o2) {
            return o2.dateRecorded.compareTo(o1.dateRecorded);
        }
    }

}
