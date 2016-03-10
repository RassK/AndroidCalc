package ee.android.rassk.androidcalc;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ee.android.rassk.androidcalc.Contracts.CalculatorInputChangeListener;
import ee.android.rassk.androidcalc.Enums.*;
import ee.android.rassk.androidcalc.Models.CalculatorEngine;

public class MainActivity extends Activity implements CalculatorInputChangeListener {

    public static final String TAG = "MainActivity";
    private static final String STATE_CALC = "S_CALC_E";

    private CalculatorEngine mCalculator;
    private TextView mInputView;

    private View.OnClickListener operatorListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String operatorTag = v.getTag().toString();
            Operator op = Operator.valueOf(operatorTag);
            Log.i(TAG, "Operator pressed: " + operatorTag);

            mCalculator.setOperator(op);
        }
    };
    private View.OnClickListener resetListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String resetTag = v.getTag().toString();
            ResetAction action = ResetAction.valueOf(resetTag);
            Log.i(TAG, "Reset clicked: " + resetTag);

            mCalculator.resetInput(action);
        }
    };

    public MainActivity() {
        mCalculator = new CalculatorEngine();
        mCalculator.setInputChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init views
        mInputView = (TextView) findViewById(R.id.inputView);

        // Restore engine state if available
        if (savedInstanceState != null){
            String calcState = savedInstanceState.getString(STATE_CALC);
            mCalculator.restoreState(calcState);

            Log.i(TAG, "Resotre instance state: " + calcState);
        }

        // Init reset buttons
        findViewById(R.id.btn_reset).setOnClickListener(resetListener);
        findViewById(R.id.btn_clear).setOnClickListener(resetListener);
        findViewById(R.id.btn_del).setOnClickListener(resetListener);

        // Init op buttons
        findViewById(R.id.btn_command_add).setOnClickListener(operatorListener);
        findViewById(R.id.btn_command_sub).setOnClickListener(operatorListener);
        findViewById(R.id.btn_command_mult).setOnClickListener(operatorListener);
        findViewById(R.id.btn_command_share).setOnClickListener(operatorListener);

        // else
        findViewById(R.id.btn_command_eq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Evaluate clicked");

                mCalculator.evaluate();
            }
        });

        findViewById(R.id.btn_comma).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Add decimal point");

                mCalculator.addDecimalPoint();
            }
        });

        findViewById(R.id.btn_command_neg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Negate input");

                mCalculator.negate();
            }
        });

        // Init number buttons
        Resources res = getResources();
        for (int i = 0; i < 10; i++) {
            int id = res.getIdentifier("btn" + i, "id", getPackageName());
            Button btn = (Button) findViewById(id);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    String nr = b.getText().toString();
                    Log.d(TAG, "button clicked: " + nr);
                    mCalculator.addInput(nr);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        String state = mCalculator.getInstaceState();
        outState.putString(STATE_CALC, state);

        Log.i(TAG, "Save engine state: " + state);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCurrentInputChanged(String input) {
        mInputView.setText(input);
    }
}
