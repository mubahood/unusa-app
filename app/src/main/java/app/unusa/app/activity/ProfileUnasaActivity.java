package app.unusa.app.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

import app.components.R;
import app.unusa.app.activity.dialog.DialogFullscreen;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

public class ProfileUnasaActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    UnusaUser loggedInUser = new UnusaUser();
    UnusaUser profile = new UnusaUser();
    MyFunctions functions;
    String transactionDb = "transactions";
    CircularImageView profile_image;

    ProfileUnasaActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_profle);
        context = ProfileUnasaActivity.this;
        functions = new MyFunctions(context);
        getExtras();

        initToolbar();
        initComponent();

    }

    String userId = "";

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
        }
        step_1();
    }


    void step_1() {
        db.collection(usersDb).document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(context, "Select User", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        profile = documentSnapshot.toObject(UnusaUser.class);
                        step_2();
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

    void step_2() {
        db.collection(transactionDb)
                .whereEqualTo("personResponsibleId", userId)
                .whereEqualTo("isIncome", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                        feeData();
                    }
                });
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Member's sProfile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }


    int profile_balance_val = 0;
    String profile_is_admin_text = "regular";

    boolean profile_is_admin = false;
    boolean profile_is_verified = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    void feeData() {

        loggedInUser = new UnusaUser();
        SugarContext.init(getApplicationContext());
        try {
            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                Toast.makeText(context, "Not Logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            for (UnusaUser u : users) {
                loggedInUser = u;
                break;
            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (!loggedInUser.userType.equals("admin")) {
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        paidThisMonth = false;


        if (profile.verified != null) {
            if (profile.verified.equals("0")) {
                make_verify.setText("Verify Account");
                profile_is_verified = false;
            } else {
                profile_is_verified = true;
                verified_position.setVisibility(View.VISIBLE);

                make_verify.setText("Unnverify");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.activatedBackgroundIndicator});
                    int resource = a.getResourceId(0, 0);
                    //first 0 is the index in the array, second is the   default value
                    a.recycle();

                    main_container_bg.setBackground(context.getResources().getDrawable(R.color.green_A700));
                }
            }
        } else {
            profile_is_verified = false;
            make_verify.setText("Verify Account");
        }

        if (profile.userType.equals("admin")) {
            make_admin.setText("Remove Admin");
            profile_is_admin = true;
        } else {
            profile_is_admin = false;
            make_admin.setText("Make Admin");
        }

        if (loggedInUser.userId.equals(profile.userId) || loggedInUser.userType.equals("admin")) {
            father_image_container.setVisibility(View.VISIBLE);
            mother_image_container.setVisibility(View.VISIBLE);


            if (profile.profilePhotoMother != null)
                if (profile.profilePhotoMother.length() > 4) {
                    try {
                        Glide.with(getApplicationContext())
                                .load(profile.profilePhotoMother)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.user)
                                .centerCrop()
                                .into(mother_image);


                        mother_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialogImage(profile.profilePhotoMother);
                            }
                        });

                    } catch (Exception e) {
                    }
                }

            if (profile.profilePhotoFather != null)
                if (profile.profilePhotoFather.length() > 4) {
                    try {
                        Glide.with(getApplicationContext())
                                .load(profile.profilePhotoFather)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.user)
                                .centerCrop()
                                .into(father_image);


                        father_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialogImage(profile.profilePhotoFather);
                            }
                        });

                    } catch (Exception e) {
                    }
                }

        }


        if (profile.profilePhoto != null) {
            if (profile.profilePhoto.length() > 4) {


                profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, UnusaTransactionsActivity.class);
                        if (profile.profilePhoto != null) {
                            if (profile.profilePhoto.length() > 4) {
                                showDialogImage(profile.profilePhoto);
                            }
                        }
                    }
                });

                Glide.with(context)
                        .load(R.drawable.user)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.user)
                        .centerCrop()
                        .into(profile_image);

                Glide.with(context)
                        .load(profile.profilePhoto)
                        .asBitmap()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(profile_image) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                profile_image.setImageDrawable(circularBitmapDrawable);
                            }
                        });


            } else {
                Log.d("ANJANE", "feeData: SHORT LENGTH");
                Glide.with(context)
                        .load(R.drawable.user)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.user)
                        .centerCrop()
                        .into(profile_image);
            }
        } else {
            Log.d("ANJANE", "feeData: PROFILE IS SHORT");

            Glide.with(context)
                    .load(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .centerCrop()
                    .into(profile_image);
        }


        if (loggedInUser.userType.equals("admin")) {

            make_verify.setVisibility(View.VISIBLE);
            make_verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profile_is_verified) {
                        profile.verified = "0";
                    } else {
                        profile.verified = "1";
                    }

                    Toast.makeText(context, "Please Wait...", Toast.LENGTH_SHORT).show();
                    db.collection(usersDb).document(profile.userId).set(profile)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (profile_is_verified) {
                                        Toast.makeText(context, "Unverified", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "Verified!", Toast.LENGTH_LONG).show();
                                    }

                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }
            });


            make_admin.setVisibility(View.VISIBLE);
            make_admin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profile_is_admin) {
                        profile_is_admin_text = "regular";
                    } else {
                        profile_is_admin_text = "admin";
                    }

                    if (loggedInUser.userId.equals(profile.userId)) {
                        Toast.makeText(context, "You cannot remove yourself from admin", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(context, "Please Wait...", Toast.LENGTH_SHORT).show();
                    profile.userType = profile_is_admin_text;
                    db.collection(usersDb).document(profile.userId).set(profile)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                }
            });
        } else {
            make_admin.setVisibility(View.GONE);
            make_verify.setVisibility(View.GONE);
        }


        profile_balance_val = 0;
        company_position.setText(profile.company_position.toUpperCase() + "");
        reg_num.setText(profile.reg_num.toUpperCase() + "");
        for (UnusaTransacton t : transactons) {
            if (t.isIncome) {
                profile_balance_val += t.transactionAmount;
            }
            Log.d(TAG, "feeData: MMM ===> "+t.dueMonth+" VS. "+functions.thisMonth);
            Log.d(TAG, "feeData: YYY ===> "+t.dueYear+" VS. "+functions.thisYear);
            if (t.dueMonth == (functions.thisMonth+1) &&
                    t.dueYear == functions.thisYear
            ) {
                paidThisMonth = true;
            }


            LinearLayout a = new LinearLayout(context);
            a.setPadding(35, 10, 35, 10);
            a.setOrientation(LinearLayout.HORIZONTAL);

            TextView b = new TextView(context);
            b.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            b.setText(
                    functions.timeAgo(t.dateRecorded) +
                            ": Paid R. " + functions.toMoneyFormat(t.transactionAmount) + "" +
                            "" + " (" + t.transactionType + "), Due to month of " +
                            functions.tellMonthFull(t.dueMonth) + " of " + t.dueYear + ".\n"

            );
            a.addView(b);

            /*TextView c = new TextView(context);
            c.setText(" - " + functions.timeAgo(t.dateRecorded));
            c.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            a.addView(c);*/
            transactions_container.addView(a);


        }


        if ((profile.next_mother_name != null) && (profile.next_mother_phone != null)) {
            next_mother_name_view.setText(profile.next_mother_name);
            next_mother_phone_view.setText(profile.next_mother_phone);
        } else {
            next_mother_name_view.setText(" - ");
            next_mother_phone_view.setText(" - ");
        }


        if ((profile.next_father_phone != null) && (profile.next_father_name != null)) {
            next_father_name_view.setText(profile.next_mother_name);
            next_father_phone_view.setText(profile.next_father_phone);
        } else {
            next_father_name_view.setText(" - ");
            next_father_phone_view.setText(" - ");
        }


        if ((profile.next_address != null)) {
            next_address.setText(profile.next_address);
        } else {
            next_address.setText(" - ");
        }


        if (profile.company.length() > 2) {
            profile_job.setText(profile.company);
        } else {
            profile_job.setText(" - ");
        }

        if (profile.email.length() > 2) {
            email.setText(profile.email);
        } else {
            email.setText("No Email");
        }

        if (loggedInUser.userType.equals("admin") || loggedInUser.userId.equals(profile.userId)) {
            if (profile.address.length() > 2) {
                address.setText(profile.address);
            } else {
                address.setText("No Address");
            }
        } else {
            address.setText("Seen by Admin only");
        }


        if (loggedInUser.userType.equals("admin") || loggedInUser.userId.equals(profile.userId)) {
            if (profile.phoneNumber.length() > 2) {
                phoneNumber.setText(profile.phoneNumber + "");
            } else {
                phoneNumber.setText("No Phone number");
            }
        } else {
            phoneNumber.setText("Seen by Admin only");
        }


        profile_balance.setText("R. " + functions.toMoneyFormat(profile_balance_val) + "");


        if (profile.nationality != null) {
            if (profile.nationality.length() > 2) {
                nationality.setText(functions.stringCapitalize(profile.nationality));
            } else {
                nationality.setText("Not Mentioned");
            }
        }


        if (paidThisMonth) {
            profile_paid_this_month.setText("PAID THIS MONTH");
            profile_paid_this_month.setTextColor(Color.parseColor("#60bf00"));
        } else {
            profile_paid_this_month.setText("NOT PAID THIS MONTH");
            profile_paid_this_month.setTextColor(Color.parseColor("#cc0000"));

        }

        profile_name.setText(profile.firstName + " " + profile.lastName);
        main_container.setVisibility(View.VISIBLE);
        main_loader.setVisibility(View.GONE);
    }

    LinearLayout transactions_container;
    LinearLayout see_all_transactions;


    boolean paidThisMonth = false;
    TextView profile_name;
    TextView nationality;
    TextView address;
    TextView make_admin;
    TextView make_verify;
    TextView profile_paid_this_month;
    TextView email;
    TextView profile_job;
    TextView reg_num;
    TextView phoneNumber;
    TextView verified_position;
    TextView profile_balance;
    NestedScrollView main_container;
    RelativeLayout main_loader;
    TextView company_position, next_mother_name_view, next_mother_phone_view, next_address, next_occupation, next_father_name_view, next_father_phone_view;

    LinearLayout father_image_container;
    ImageView father_image, mother_image;
    LinearLayout mother_image_container;
    LinearLayout main_container_bg;

    private void initComponent() {
        profile_image = findViewById(R.id.profile_image);

        next_mother_phone_view = findViewById(R.id.next_mother_phone_view);
        next_mother_name_view = findViewById(R.id.next_mother_name_view);
        mother_image = findViewById(R.id.mother_image);
        main_container_bg = findViewById(R.id.main_container_bg);
        verified_position = findViewById(R.id.verified_position);
        father_image_container = findViewById(R.id.father_image_container);
        mother_image_container = findViewById(R.id.mother_image_container);
        father_image_container.setVisibility(View.GONE);
        mother_image_container.setVisibility(View.GONE);
        father_image = findViewById(R.id.father_image);
        next_father_name_view = findViewById(R.id.next_father_name_view);
        next_address = findViewById(R.id.next_address);
        next_father_phone_view = findViewById(R.id.next_father_phone_view);

        main_container = findViewById(R.id.main_container);
        profile_name = findViewById(R.id.profile_name);
        profile_job = findViewById(R.id.profile_job);
        make_admin = findViewById(R.id.make_admin);
        make_verify = findViewById(R.id.make_verify);
        nationality = findViewById(R.id.nationality);
        reg_num = findViewById(R.id.reg_num);
        see_all_transactions = findViewById(R.id.see_all_transactions);
        address = findViewById(R.id.address);
        phoneNumber = findViewById(R.id.phoneNumber);
        transactions_container = findViewById(R.id.transactions_container);
        email = findViewById(R.id.email);
        profile_paid_this_month = findViewById(R.id.profile_paid_this_month);
        profile_balance = findViewById(R.id.profile_balance);
        main_loader = findViewById(R.id.main_loader);
        company_position = findViewById(R.id.company_position);
        main_container.setVisibility(View.GONE);
        main_loader.setVisibility(View.VISIBLE);


        see_all_transactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UnusaTransactionsActivity.class);
                context.startActivity(i);
            }
        });
    }


    DialogImageFragment newFragment;

    private void showDialogImage(String my_img_url) {
        if (my_img_url == null) {
            Toast.makeText(context, "Can't display this image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (my_img_url.length() < 3) {
            Toast.makeText(context, "Can't display this image", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogImageFragment newFragment = new DialogImageFragment(my_img_url);

        newFragment.setRequestCode(DialogFullscreen.DIALOG_QUEST_CODE);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        newFragment.setOnCallbackResult(new DialogImageFragment.CallbackResult() {
            @Override
            public void sendResult(int requestCode, Object obj) {
                if (requestCode == DialogFullscreen.DIALOG_QUEST_CODE) {
                    //displayDataResult((Event) obj);
                    //Toast.makeText(context, "Closed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void openAddTransaction(View view) {
        Intent intent = new Intent(ProfileUnasaActivity.this, AddContributionUnasaActivity.class);
        ProfileUnasaActivity.this.startActivity(intent);
    }

    private static final String TAG = "ROMINA-1";
}
