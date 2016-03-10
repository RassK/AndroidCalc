package ee.android.rassk.androidcalc.Models;

import android.util.Log;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import ee.android.rassk.androidcalc.Contracts.CalculatorInputChangeListener;
import ee.android.rassk.androidcalc.Enums.*;

/**
 * Created by Rasmus on 10.03.2016.
 */
public class CalculatorEngine {

    public static final String TAG = "CalculatorEngine";

    /* State tags */
    private static final String STATE_CURRENT_INPUT = "S_CURRENT_INPUT";
    private static final String STATE_RESERVED = "S_RESERVED";
    private static final String STATE_OPERATOR = "S_OPERATOR";
    private static final String STATE_FINALIZED = "S_FINALIZED";

    private String mCurrentInput = "";
    private String mReserved;
    private Operator mOperator;
    private boolean mFinalized;
    private CalculatorInputChangeListener mListener;
    private DecimalFormat mNumberFormat;

    public CalculatorEngine() {
        mOperator = Operator.None;

        mNumberFormat = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        mNumberFormat.setMaximumFractionDigits(5);
    }

    public void setInputChangeListener(CalculatorInputChangeListener listener) {
        mListener = listener;
    }

    public void addInput(String value) {
        if (mFinalized) {
            mCurrentInput = "";
            mFinalized = false;
        }

        mCurrentInput += value;
        onInputChanged();
    }

    public String getCurrentInput() {
        return mCurrentInput;
    }

    public void resetInput(ResetAction action) {
        switch (action) {
            case Full:
                mCurrentInput = "";
                mOperator = Operator.None;
                mFinalized = false;
                break;
            case Clear:
                mCurrentInput = "";
                break;
            case Delete:
                if (mCurrentInput.length() > 0)
                    mCurrentInput = mCurrentInput.substring(0, mCurrentInput.length() - 1);
                break;
        }

        onInputChanged();
    }

    /* Ops */
    public void setOperator(Operator op) {
        if (mOperator != Operator.None) {
            try {
                calculate();
            } catch (Exception ex){
                Log.e(TAG, "Calculation failed: " + ex.getMessage());
            }
        }

        mOperator = op;
        mReserved = mCurrentInput;
        mFinalized = true;
    }

    public void addDecimalPoint(){
        char separator = mNumberFormat.getDecimalFormatSymbols().getDecimalSeparator();
        if (!mCurrentInput.contains(String.valueOf(separator))){
            if (mCurrentInput.equals(""))
                mCurrentInput += 0;

            mCurrentInput += separator;
        }

        onInputChanged();
    }

    public void negate(){
        if (mCurrentInput.length() > 0 && mCurrentInput.charAt(0) == '-'){
            mCurrentInput = mCurrentInput.substring(1);
        } else {
            mCurrentInput = '-' + mCurrentInput;
        }

        onInputChanged();
    }

    public void evaluate() {
        if (mReserved == null)
            return;

        try {
            calculate();

            mFinalized = true;
            mOperator = Operator.None;

            onInputChanged();
        } catch (Exception ex) {
            Log.e(TAG, "Calculation failed: " + ex.getMessage());
        }
    }

    /* Instance state */
    public String getInstaceState(){
        try {
            JSONObject obj = new JSONObject();
            obj.put(STATE_CURRENT_INPUT, mCurrentInput);
            obj.put(STATE_RESERVED, mReserved);
            obj.put(STATE_OPERATOR, mOperator.toString());
            obj.put(STATE_FINALIZED, mFinalized);

            return obj.toString();
        } catch (Exception ex){
            Log.e(TAG, "Unable to build instance state string: " + ex.getMessage());

            return null;
        }
    }

    public void restoreState(String state){
        try{
            JSONObject obj = new JSONObject(state);

            mCurrentInput = obj.getString(STATE_CURRENT_INPUT);
            mReserved = obj.getString(STATE_RESERVED);
            mFinalized = obj.getBoolean(STATE_FINALIZED);
            mOperator = Operator.valueOf(obj.getString(STATE_OPERATOR));

            onInputChanged();
        } catch (Exception ex){
            Log.e(TAG, "Unable to restore state: " + ex.getMessage());
        }
    }

    private void calculate() throws ParseException {
        double result = 0;
        double operand1 = mNumberFormat.parse(mReserved).doubleValue();
        double operand2 = mNumberFormat.parse(mCurrentInput).doubleValue();

        Log.i(TAG, String.format("Calculating %f %s %f", operand1, mOperator.toString(), operand2));

        switch (mOperator) {
            case Add:
                result = (operand1 + operand2);
                break;
            case Subtract:
                result = (operand1 - operand2);
                break;
            case Multiply:
                result = (operand1 * operand2);
                break;
            case Divide:
                result = (operand1 / operand2);
                break;
            default:
                return;
        }

        mCurrentInput = mNumberFormat.format(result);
        onInputChanged();
    }

    private void onInputChanged() {
        if (mListener != null) {
            mListener.onCurrentInputChanged(mCurrentInput);
        }
    }
}
