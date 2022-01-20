package app.unusa.app.activity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jsibbold.zoomage.ZoomageView;

import app.components.R;

@SuppressLint("ValidFragment")
public class DialogImageFragment extends DialogFragment {


    public CallbackResult callbackResult;
    private static final String TAG = "DialogImageFragmentNEW";

    public void setOnCallbackResult(final CallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    private int request_code = 0;
    private View root_view;

    public DialogImageFragment(String my_img_url) {
        this.image_url = my_img_url;
    }

    private String image_url = "";
    ZoomageView product_image;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root_view = inflater.inflate(R.layout.dialog_image_1, container, false);
        //details = root_view.findViewById(R.id.details);
        product_image = root_view.findViewById(R.id.product_image);

        Log.d(TAG, "onCreateView: ===> " + image_url);

        if (image_url.length() > 4) {
           /* Glide.with(getContext()).load(image_url).asBitmap().centerCrop().into(new BitmapImageViewTarget(product_image) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    product_image.setImageDrawable(circularBitmapDrawable);
                }
            });*/

            Glide.with(getContext())
                    .load(image_url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.user)
                    .centerCrop()
                    .into(product_image);


        } else {
            Log.d("ANJANE", "feeData: SHORT LENGTH");
            Glide.with(getContext())
                    .load(R.drawable.user)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.user)
                    .centerCrop()
                    .into(product_image);
        }

        /*spn_from_time = (Button) root_view.findViewById(R.id.spn_from_time);
        spn_to_date = (Button) root_view.findViewById(R.id.spn_to_date);
        spn_to_time = (Button) root_view.findViewById(R.id.spn_to_time);
        tv_email = (TextView) root_view.findViewById(R.id.tv_email);
        et_name = (EditText) root_view.findViewById(R.id.et_name);
        et_location = (EditText) root_view.findViewById(R.id.et_location);
        cb_allday = (AppCompatCheckBox) root_view.findViewById(R.id.cb_allday);
        spn_timezone = (AppCompatSpinner) root_view.findViewById(R.id.spn_timezone);*/

        ((ImageButton) root_view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ((Button) root_view.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataResult();
                dismiss();
            }
        });


        return root_view;
    }

    private void sendDataResult() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void setRequestCode(int request_code) {
        this.request_code = request_code;
    }


    public interface CallbackResult {
        void sendResult(int requestCode, Object obj);
    }

}