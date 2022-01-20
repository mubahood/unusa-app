package app.unusa.app.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.components.R;
import app.unusa.app.adapter.UnusaAdapterPeople;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

public class MembersUnasaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UnusaAdapterPeople adapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    ArrayList<UnusaUser> allMembers = new ArrayList<>();

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View bottom_sheet;
    UnusaUser loggedInUser = new UnusaUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_members_activity);
        bindViews();
        initComponent_1();
        initToolbar();
        //showBottomSheetDialog(adapter.getItem(0));
    }


    private static final String TAG = "MembersUnasaActivity";

    // Getting loggedin User
    private void initComponent_1() {
        SugarContext.init(MembersUnasaActivity.this);

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
                initComponent_2();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "startMainDashboard: --> " + e.getMessage());
            onBackPressed();
        }
    }

    boolean isAdmin = false;
    String reg_num = "";

    // Getting loggedin User
    private void initComponent_2() {
        Query query;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            try {
                reg_num = getIntent().getStringExtra("reg_num");
            } catch (Exception e) {
                reg_num = "";
            }
        }

        if (reg_num.length() > 1) {
            query = db.collection(usersDb).whereEqualTo("reg_num", reg_num);
        } else {
            query = db.collection(usersDb);
        }


        // initComponent();
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                allMembers = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                Collections.sort(allMembers, new CustomComparator());
                if (allMembers.size() < 1) {
                    Toast.makeText(MembersUnasaActivity.this, "No Data to Display", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }

                initComponent();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MembersUnasaActivity.this, "Poor Connection", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });

    }

    public class CustomComparator implements Comparator<UnusaUser> {
        @Override
        public int compare(UnusaUser o1, UnusaUser o2) {
            return o1.firstName.compareTo(o2.firstName);
        }
    }


    RelativeLayout main_loader;
    LinearLayout main_container;
    FloatingActionButton fab;

    private void initComponent() {

        main_loader = findViewById(R.id.main_loader);
        main_container = findViewById(R.id.main_container);
        fab = findViewById(R.id.fab);

        if (loggedInUser.userType.equals("admin")) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

        main_loader.setVisibility(View.GONE);
        main_container.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set data and list adapter
        adapter = new UnusaAdapterPeople(this, allMembers);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new UnusaAdapterPeople.OnItemClickListener() {
            @Override
            public void onItemClick(View view, UnusaUser obj, int pos) {
                showBottomSheetDialog(obj);
            }
        });

        bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (reg_num.length() > 1) {
            actionBar.setTitle("REG No.: " + reg_num);
        } else {
            actionBar.setTitle("All Members");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            if (loggedInUser.userType.equals("admin")) {
                share_data();
            } else {
                Toast.makeText(getApplicationContext(), "Only admins can share data", Toast.LENGTH_LONG).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    MaterialRippleLayout add_monthly_contribution;
    MaterialRippleLayout add_other_contribution;
    MaterialRippleLayout delete_member;
    MaterialRippleLayout edit_member;

    int myCounter = 0;
    String shareBody = "";

    private void share_data() {
        myCounter = 0;
        MyFunctions functions;
        functions = new MyFunctions(MembersUnasaActivity.this);
        shareBody += "*UNUSA REGISTERED MEMBERS" + "" +
                "\n\n*DATE: " + functions.toDateOne(functions.getTimeStamp()) + "" +
                " at " + functions.toTimeOne(functions.getTimeStamp()) + ".\n\n";

        for (UnusaUser u : allMembers) {
            myCounter++;
            shareBody += "\n" + myCounter + ". " + u.firstName + " " + u.lastName + "  *ID: " + u.reg_num + "*";
        }

        shareBody += "\n\n_Generated by UNUSA APP as on " + functions.toDateOne(functions.getTimeStamp()) + "" +
                " at " + functions.toTimeOne(functions.getTimeStamp()) + ", By " +
                functions.stringCapitalize(loggedInUser.firstName) + " " +
                functions.stringCapitalize(loggedInUser.lastName) + "._";


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "UNUSA MEMBERS");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "UNUSA APP"));
    }

    void bindViews() {


    }

    private void showBottomSheetDialog(final UnusaUser people) {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.sheet_list, null);

        ((View) view.findViewById(R.id.lyt_preview)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MembersUnasaActivity.this, ProfileUnasaActivity.class);
                intent.putExtra("userId", people.userId);
                MembersUnasaActivity.this.startActivity(intent);
                return;
                //Toast.makeText(MembersUnasaActivity.this, "Preview '" + people.name + "' clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ((View) view.findViewById(R.id.lyt_share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MembersUnasaActivity.this, "Calling '" + people.firstName + " " + people.lastName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + people.phoneNumber));
                startActivity(intent);
            }
        });

        ((View) view.findViewById(R.id.lyt_get_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MembersUnasaActivity.this, "Whatsapp '" + people.firstName + " " + people.lastName, Toast.LENGTH_SHORT).show();

                Uri uri = Uri.parse("smsto:" + people.phoneNumber);
                Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                i.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(i, "From UNUSA - APP"));


                /*PackageManager pm=getPackageManager();
                try {

                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    String text = "YOUR TEXT HERE";

                    PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(waIntent, "Share with"));

                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(MembersUnasaActivity.this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                            .show();
                }*/

            }
        });


        add_monthly_contribution = view.findViewById(R.id.add_monthly_contribution);
        add_other_contribution = view.findViewById(R.id.add_other_contribution);
        delete_member = view.findViewById(R.id.delete_member);
        edit_member = view.findViewById(R.id.edit_member);
        add_monthly_contribution.setVisibility(View.GONE);
        add_other_contribution.setVisibility(View.GONE);

        edit_member.setVisibility(View.VISIBLE);
        edit_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((!isAdmin) && (!people.userId.equals(loggedInUser.userId))) {
                    Toast.makeText(MembersUnasaActivity.this, "You cannot edit another member's profile", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MembersUnasaActivity.this, AddNewMemberUnasaActivity.class);
                intent.putExtra("taskEdit", people.userId);
                intent.putExtra("userId", people.userId);
                MembersUnasaActivity.this.startActivity(intent);
                //finish();
                return;
                // aaToast.makeText(MembersUnasaActivity.this, "Copy '" + people.name + "' clicked", Toast.LENGTH_SHORT).show();
            }
        });

        if (isAdmin) {
            delete_member.setVisibility(View.VISIBLE);
            add_monthly_contribution.setVisibility(View.VISIBLE);
            add_other_contribution.setVisibility(View.VISIBLE);


        }


        if (isAdmin) {

            add_monthly_contribution.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MembersUnasaActivity.this, AddContributionUnasaActivity.class);
                    intent.putExtra("userId", people.userId);
                    MembersUnasaActivity.this.startActivity(intent);
                    //finish();
                    return;
                    // aaToast.makeText(MembersUnasaActivity.this, "Copy '" + people.name + "' clicked", Toast.LENGTH_SHORT).show();
                }
            });


            add_other_contribution.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MembersUnasaActivity.this, EventesUnasaActivity.class);
                    intent.putExtra("userId", people.userId);
                    intent.putExtra("task", "addOther");
                    MembersUnasaActivity.this.startActivity(intent);
                    //finish();
                    return;
                    // aaToast.makeText(MembersUnasaActivity.this, "Copy '" + people.name + "' clicked", Toast.LENGTH_SHORT).show();
                }
            });


            delete_member.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteDialog(people.userId);
                    return;
                    // aaToast.makeText(MembersUnasaActivity.this, "Copy '" + people.name + "' clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }


        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }


    String transactionDb = "transactions";
    ArrayList<UnusaTransacton> temp_transactons = new ArrayList<>();

    private void showDeleteDialog(final String usersId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MembersUnasaActivity.this);
        builder.setTitle("Do you really want to delete this member?");
        builder.setMessage("All his/her transactions will be deleted too.");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                showAlertPogressDialog();
                db.collection(transactionDb)
                        .whereEqualTo("personResponsibleId", usersId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    delete_member_process(usersId);
                                    return;
                                }

                                temp_transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                                for (UnusaTransacton t : temp_transactons) {
                                    db.collection(transactionDb).document(t.transactionId).delete();
                                }
                                delete_member_process(usersId);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MembersUnasaActivity.this, "FAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    private void delete_member_process(String usersId) {
        db.collection(usersDb).document(usersId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MembersUnasaActivity.this, "Member Deleted", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        dialog.dismiss();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MembersUnasaActivity.this, "FAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.cancel();
                dialog.dismiss();
            }
        });
    }

    ProgressDialog dialog;

    private void showAlertPogressDialog() {
        dialog = new ProgressDialog(MembersUnasaActivity.this);
        dialog.setTitle("Deleting transaction");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }


    public void openAddNewMember(View view) {
        if (!isAdmin) {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MembersUnasaActivity.this, AddNewMemberUnasaActivity.class);
        MembersUnasaActivity.this.startActivity(intent);
        //finish();
        return;
    }
}
