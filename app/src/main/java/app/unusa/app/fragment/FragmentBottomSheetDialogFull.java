package app.unusa.app.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import app.components.R;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;


public class FragmentBottomSheetDialogFull extends BottomSheetDialogFragment {

    private BottomSheetBehavior mBehavior;
    private AppBarLayout app_bar_layout;
    private LinearLayout lyt_profile;
    ImageButton imageButton;
    ProgressDialog dialog;


    private UnusaTransacton people;
    Context context;
    MyFunctions functions;
    boolean isAdmin;

    public void setPeople(UnusaTransacton people, Context context, MyFunctions functions, boolean isAdmin) {
        this.people = people;
        this.context = context;
        this.functions = functions;
        this.isAdmin = isAdmin;
        dialog = new ProgressDialog(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet_dialog_full, null);

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        app_bar_layout = (AppBarLayout) view.findViewById(R.id.app_bar_layout);
        lyt_profile = (LinearLayout) view.findViewById(R.id.lyt_profile);

        // set data to view
        //Tools.displayImageRound(getActivity(), (ImageView) view.findViewById(R.id.image), people.image);
        ((TextView) view.findViewById(R.id.name)).setText(people.personResponsible.firstName + " " + people.personResponsible.lastName);

        if (people.isIncome) {
            ((TextView) view.findViewById(R.id.fragment_status)).setText(people.transactionType + " (Income)");
        } else {
            ((TextView) view.findViewById(R.id.fragment_status)).setText(people.transactionType + " (Expense)");
        }

        ((TextView) view.findViewById(R.id.fragment_amount)).setText("RAND. " + people.transactionAmount);
        ((TextView) view.findViewById(R.id.transaction_recorded_by)).setText(people.recordedBy.firstName + " " + people.recordedBy.lastName);
        imageButton = (ImageButton) view.findViewById(R.id.fragment_delete);

        if (isAdmin) {
            imageButton.setVisibility(View.GONE);
        } else {
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog();
                }
            });


        }


        ((TextView) view.findViewById(R.id.name_toolbar)).setText(people.personResponsible.firstName + " " + people.personResponsible.lastName);
        ((TextView) view.findViewById(R.id.transaction_due_month)).setText(functions.tellMonthFull(people.dueMonth) + " of " + people.dueYear);
        ((TextView) view.findViewById(R.id.fragment_contact_label)).setText(people.personResponsible.firstName + "'s Contact");
        ((TextView) view.findViewById(R.id.fragment_contact_val)).setText(people.personResponsible.phoneNumber);
        ((TextView) view.findViewById(R.id.fragment_address)).setText(people.personResponsible.address);


        ((View) view.findViewById(R.id.lyt_spacer)).setMinimumHeight(Tools.getScreenHeight() / 2);

        hideView(app_bar_layout);

        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    showView(app_bar_layout, getActionBarSize());
                    hideView(lyt_profile);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    hideView(app_bar_layout);
                    showView(lyt_profile, getActionBarSize());
                }

                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ((ImageButton) view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to delete this transaction?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
                showAlertPogressDialog();
                db.collection(transactionDb).document(people.transactionId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_LONG).show();
                                dialog.cancel();
                                dialog.dismiss();
                                dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "FAILED: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    String transactionDb = "transactions";
    private void showAlertPogressDialog() {
        dialog.setTitle("Deleting transaction");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void hideView(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);
    }

    private void showView(View view, int size) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = size;
        view.setLayoutParams(params);
    }

    private int getActionBarSize() {
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int size = (int) styledAttributes.getDimension(0, 0);
        return size;
    }
}
