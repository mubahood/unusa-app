package app.unusa.app.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.List;

public class UnusaSingleContributionActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String contributionDb = "contributionTypes";
    String usersDb = "users";
    String transactionDb = "transactions";


    UnusaSingleContributionActivity context;
    MyFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unusa_activity_single_contribution);
        context = UnusaSingleContributionActivity.this;
        functions = new MyFunctions(context);
        step_one();
        bindViews();
        //initNavigationMenu();
    }


    UnusaUser loggedInUser;
    boolean isAdmin = false;
    UnusaContributionType contributionType;

    // ftech data of user
    private void step_one() {
        isAdmin = false;
        loggedInUser = new UnusaUser();
        SugarContext.init(getApplicationContext());
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
            }
            for (UnusaUser u : users) {
                loggedInUser = u;
            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            if (loggedInUser.userType.equals("admin")) {
                isAdmin = true;
            }

        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }


        contributionType = new UnusaContributionType();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("contributionId")) {
                this.contributionType.contributionId = getIntent().getStringExtra("contributionId");
                step_two();
            } else {
                Toast.makeText(context, "Select Member", Toast.LENGTH_SHORT).show();
                onBackPressed();
                finish();
            }
        } else {
            Toast.makeText(context, "Select Member", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // fetch Contribution Data
    private void step_two() {
        db.collection(contributionDb).document(contributionType.contributionId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(context, "Contribution Does not exist", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        contributionType = documentSnapshot.toObject(UnusaContributionType.class);
                        step_three();
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

    ArrayList<UnusaTransacton> transactons = new ArrayList<>();

    private void step_three() {
        db.collection(transactionDb).whereEqualTo("transactionType", contributionType.title)
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

    ArrayList<String> paidMembersIds = new ArrayList<>();

    //Combining DATA
    void step_five() {
        activity_title = contributionType.title;
        if (!contributionType.isOpen) {
            single_cont_close.setText("Contribution Closed");
        }

        initToolbar();
        single_cont_container.setVisibility(View.VISIBLE);
        single_cont_main_loader.setVisibility(View.GONE);
        single_cont_desciption.setText(contributionType.details.toString());

        for (UnusaTransacton t : transactons) {
            totalAmountCollected += t.transactionAmount;
            if (!paidMembersIds.contains(t.personResponsible.userId)) {
                paidMembersIds.add(t.personResponsible.userId);
            }
            if (t.personResponsible.userId.equals(loggedInUser.userId)) {
                iPaid = true;
            }
        }

        try {
            contribution_target = contributionType.contribution_target;
        } catch (Exception e) {
            contribution_target = 0;
        }

        single_cont_unpaid_val = contribution_target - totalAmountCollected;
        if (single_cont_unpaid_val < 1) {
            single_cont_unpaid.setText("(0)");
        } else {
            single_cont_unpaid.setText("(" + functions.toMoneyFormat(single_cont_unpaid_val) + ")");
        }
        single_cont_collected_amount.setText(functions.toMoneyFormat(totalAmountCollected) + "");
        single_cont_num_paid.setText(paidMembersIds.size() + " Members");
        single_cont_num_not_paid.setText((users.size() - paidMembersIds.size()) + " Members");
        if (iPaid) {
            single_cont_my_status.setText("I PAID");
            single_cont_my_status.setTextColor(Color.parseColor("#60bf00"));
        } else {
            single_cont_my_status.setText("NOT PAID");
            single_cont_my_status.setTextColor(Color.parseColor("#cc0000"));
        }
    }


    RelativeLayout single_cont_main_loader;
    RelativeLayout single_cont_container;
    TextView single_cont_desciption;
    TextView single_cont_unpaid;
    TextView single_cont_num_not_paid;
    TextView single_cont_collected_amount;
    TextView single_cont_num_paid;
    TextView single_cont_see_more;
    TextView single_cont_my_status;
    TextView single_cont_delete;
    TextView single_cont_close;

    private void bindViews() {
        single_cont_main_loader = findViewById(R.id.single_cont_main_loader);
        single_cont_close = findViewById(R.id.single_cont_close);
        single_cont_container = findViewById(R.id.single_cont_container);
        single_cont_desciption = findViewById(R.id.single_cont_desciption);
        single_cont_num_paid = findViewById(R.id.single_cont_num_paid);
        single_cont_unpaid = findViewById(R.id.single_cont_unpaid);
        single_cont_collected_amount = findViewById(R.id.single_cont_collected_amount);
        single_cont_num_not_paid = findViewById(R.id.single_cont_num_not_paid);
        single_cont_my_status = findViewById(R.id.single_cont_my_status);
        single_cont_delete = findViewById(R.id.single_cont_delete);
        single_cont_see_more = findViewById(R.id.single_cont_see_more);
        single_cont_main_loader.setVisibility(View.VISIBLE);
        single_cont_container.setVisibility(View.GONE);
        single_cont_container.setVisibility(View.GONE);
        single_cont_delete.setVisibility(View.GONE);

        if (isAdmin) {
            single_cont_close.setVisibility(View.VISIBLE);
            single_cont_delete.setVisibility(View.VISIBLE);


            single_cont_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_contribution();
                }
            });
            if (contributionType.isOpen) {
                single_cont_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        close_contribution();
                    }
                });
            }
        }

        single_cont_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UnusaTransactionsActivity.class);
                i.putExtra("contributionId", contributionType.title);
                context.startActivity(i);
                return;
            }
        });
    }

    ArrayList<UnusaTransacton> temp_transactons = new ArrayList<>();

    private void close_contribution() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Do you really want to close this contribution?");
        //builder.setMessage("All its transactions will be deleted too.");
        builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showAlertPogressDialog();

                contributionType.isOpen = false;
                db.collection(contributionDb).document(contributionType.contributionId)
                        .set(contributionType)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Contribution Closed", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                                dialog.dismiss();
                                finish();
                            }
                        });

            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }


    private void delete_contribution() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Do you really want to delete this contribution?");
        builder.setMessage("All its transactions will be deleted too.");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showAlertPogressDialog();
                db.collection(transactionDb)
                        .whereEqualTo("transactionType", contributionType.title)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                temp_transactons.clear();
                                temp_transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                                for (UnusaTransacton t : temp_transactons) {
                                    db.collection(transactionDb).document(t.transactionId).delete();
                                }

                                db.collection(contributionDb).document(contributionType.contributionId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Contribution Deleted", Toast.LENGTH_LONG).show();
                                                dialog.cancel();
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "FAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    ProgressDialog dialog;

    private void showAlertPogressDialog() {
        dialog = new ProgressDialog(context);
        dialog.setTitle("Processing");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
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
