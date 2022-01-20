package app.unusa.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.orm.SugarContext;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import app.components.R;

public class StatisticsActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String contributionDb = "contributionTypes";
    String usersDb = "users";
    String transactionDb = "transactions";


    StatisticsActivity context;
    MyFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        context = StatisticsActivity.this;
        functions = new MyFunctions(context);
        step_one();
        bindViews();
        //initNavigationMenu();
    }


    UnusaUser loggedInUser;
    boolean isAdmin = false;

    // ftech data of user
    private void step_one() {
        isAdmin = false;
        loggedInUser = new UnusaUser();
        SugarContext.init(getApplicationContext());
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                return;
            }
            for (UnusaUser u : users) {
                loggedInUser = u;
            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            if (!loggedInUser.userType.equals("admin")) {
                isAdmin = true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        step_two();
    }


    // fetch Contribution Data
    private void step_two() {
        step_three();
    }

    ArrayList<UnusaTransacton> transactons = new ArrayList<>();

    private void step_three() {
        db.collection(transactionDb)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);

                        }
                        step_four();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }


    ArrayList<UnusaUser> users = new ArrayList<>();

    // fetching user
    private void step_four() {
        db.collection(usersDb)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            users = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                        }
                        step_five();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });
    }


    boolean iPaid = false;
    int totalAmountCollected = 0;
    int single_cont_unpaid_val = 0;
    int contribution_target = 0;
    int this_month_income_val = 0;
    int this_month_expense_val = 0;
    int all_time_income_val = 0;
    int all_time_expense_val = 0;

    ArrayList<String> paidMembersIds = new ArrayList<>();

    //Combining DATA
    void step_five() {
        expected_cash_in_hand_val = 0;
        this_month_income_val = 0;
        this_month_expense_val = 0;
        all_time_income_val = 0;
        activity_title = "Statistics";
        initToolbar();
        single_cont_container.setVisibility(View.VISIBLE);
        single_cont_main_loader.setVisibility(View.GONE);

        for (UnusaTransacton t : transactons) {

            if (t.isIncome) {
                totalAmountCollected += t.transactionAmount;
                all_time_income_val += t.transactionAmount;
                expected_cash_in_hand_val += t.transactionAmount;
            } else {
                all_time_expense_val += t.transactionAmount;
            }

            if (
                    t.dueMonth == (functions.thisMonth+1) &&
                            t.dueYear == functions.thisYear &&
                            t.transactionType.equals("Monthly Contribution")
            ) {
                if (t.isIncome) {
                    this_month_income_val += t.transactionAmount;
                } else {
                    this_month_expense_val += t.transactionAmount;
                }

                if (!paidMembersIds.contains(t.personResponsible.userId)) {
                    paidMembersIds.add(t.personResponsible.userId);
                }
                if (t.personResponsible.userId.equals(loggedInUser.userId)) {
                    iPaid = true;
                }
            }
        }


        single_cont_unpaid_val = contribution_target - totalAmountCollected;
        if (single_cont_unpaid_val < 1) {
            //single_cont_unpaid.setText("(0)");
        } else {
            //single_cont_unpaid.setText("(" + functions.toMoneyFormat(single_cont_unpaid_val) + ")");
        }

        single_cont_num_paid.setText(paidMembersIds.size() + " Members");
        single_cont_num_not_paid.setText((users.size() - paidMembersIds.size()) + " Members");
        if (iPaid) {
            single_cont_my_status.setText("I HAVE PAID THIS MONTH");
            single_cont_my_status.setTextColor(Color.parseColor("#60bf00"));
        } else {
            single_cont_my_status.setText("NOT PAID THIS MONTH");
            single_cont_my_status.setTextColor(Color.parseColor("#cc0000"));
        }

        expected_cash_in_hand_val = all_time_income_val - all_time_expense_val;
        expected_cash_in_hand.setText("R. " + functions.toMoneyFormat(expected_cash_in_hand_val).toString());

        this_month_income.setText("R. " + functions.toMoneyFormat(this_month_income_val) + "");
        this_month_expense.setText("(R. " + functions.toMoneyFormat(this_month_expense_val) + ")");
        all_time_income.setText("R. " + functions.toMoneyFormat(all_time_income_val) + "");
        all_time_expense.setText("R. " + functions.toMoneyFormat(all_time_expense_val) + "");

    }


    RelativeLayout single_cont_main_loader;
    RelativeLayout single_cont_container;
    TextView single_cont_desciption;
    TextView single_cont_unpaid;
    TextView single_cont_num_not_paid;
    TextView single_cont_collected_amount;
    TextView single_cont_num_paid;
    TextView single_cont_see_more;
    TextView expected_cash_in_hand;
    TextView single_cont_my_status;
    TextView all_time_income;
    TextView this_month_expense;
    TextView this_month_income;
    TextView all_time_expense;

    int expected_cash_in_hand_val = 0;

    private void bindViews() {
        single_cont_main_loader = findViewById(R.id.single_cont_main_loader);
        single_cont_container = findViewById(R.id.single_cont_container);
        all_time_expense = findViewById(R.id.all_time_expense);
        single_cont_desciption = findViewById(R.id.single_cont_desciption);
        single_cont_num_paid = findViewById(R.id.single_cont_num_paid);
        this_month_income = findViewById(R.id.this_month_income);
        expected_cash_in_hand = findViewById(R.id.expected_cash_in_hand);
        single_cont_unpaid = findViewById(R.id.single_cont_unpaid);
        all_time_income = findViewById(R.id.all_time_income);
        this_month_expense = findViewById(R.id.this_month_expense);
        single_cont_collected_amount = findViewById(R.id.single_cont_collected_amount);
        single_cont_num_not_paid = findViewById(R.id.single_cont_num_not_paid);
        single_cont_my_status = findViewById(R.id.single_cont_my_status);
        single_cont_see_more = findViewById(R.id.single_cont_see_more);
        single_cont_main_loader.setVisibility(View.VISIBLE);
        single_cont_container.setVisibility(View.GONE);

        single_cont_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UnusaTransactionsActivity.class);
                context.startActivity(i);
                finish();
                return;
            }
        });
    }


    String activity_title = "";

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(activity_title);
        Tools.setSystemBarColor(this);
    }

    private void initNavigationMenu() {
        /*NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                Toast.makeText(getApplicationContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                actionBar.setTitle(item.getTitle());
                drawer.closeDrawers();
                return true;
            }
        });*/

        // open drawer at start
        //drawer.openDrawer(GravityCompat.START);
    }
}
