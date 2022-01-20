package app.unusa.app.activity.verification;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import app.components.R;
import app.unusa.app.utils.Tools;

public class VerificationPhone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_phone);
        Tools.setSystemBarColor(this, R.color.grey_20);
    }
}
