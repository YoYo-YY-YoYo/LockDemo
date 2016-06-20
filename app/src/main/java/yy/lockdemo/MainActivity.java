package yy.lockdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import yy.lockdemo.widget.LockView;

public class MainActivity extends AppCompatActivity {

    private LockView lockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lockView = (LockView) findViewById(R.id.lock_view);

        lockView.setKeyNumber(3);
        lockView.setMinPassSize(4);
        lockView.setKeyValues(
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9"
        );
        lockView.setOnConfirmPassListener(new LockView.ConfirmPassListener() {
            @Override
            public void onConfirm(String pass) {
                Toast.makeText(getApplicationContext(), pass, Toast.LENGTH_SHORT).show();
                if (pass != null && pass.equals("1234")) {
                    finish();
                } else {
                    lockView.error();
                }
            }
        });
    }
}
