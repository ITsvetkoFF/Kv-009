package org.ecomap.android.app.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.RegisterTask;
import org.ecomap.android.app.utils.NetworkAvailability;


public class RegistrationFragment extends DialogFragment {

    private EditText firstName;
    private EditText secondName;
    private AutoCompleteTextView email;
    private EditText password;
    private EditText confirmPassword;
    private Button signUp;

    private TextInputLayout tilFirstName, tilSecondName, tilEmail, tilPassword, tilConfirmPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_layout, container, false);
        getDialog().setTitle(getString(R.string.sign_up));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        if (v != null) {
            firstName = (EditText) v.findViewById(R.id.first_name);
            secondName = (EditText) v.findViewById(R.id.second_name);
            email = (AutoCompleteTextView) v.findViewById(R.id.email_registration);
            password = (EditText) v.findViewById(R.id.password);
            confirmPassword = (EditText) v.findViewById(R.id.confirm_password);
            signUp = (Button) v.findViewById(R.id.email_sign_up_button);
            tilFirstName = (TextInputLayout) v.findViewById(R.id.til_first_name_reg);
            tilSecondName = (TextInputLayout) v.findViewById(R.id.til_second_name_reg);
            tilEmail = (TextInputLayout) v.findViewById(R.id.til_email_reg);
            tilPassword = (TextInputLayout) v.findViewById(R.id.til_password_reg);
            tilConfirmPassword = (TextInputLayout) v.findViewById(R.id.til_confirm_password_reg);
        }

        tilFirstName.setErrorEnabled(true);
        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (firstName.getText().toString().isEmpty()) {
                        tilFirstName.setError(getString(R.string.first_name_blank));
                        signUp.setClickable(false);
                    } else {
                        tilFirstName.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilSecondName.setErrorEnabled(true);
        secondName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (secondName.getText().toString().isEmpty()) {
                        tilSecondName.setError(getString(R.string.second_name_blank));
                        signUp.setClickable(false);
                    } else {
                        tilSecondName.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilEmail.setErrorEnabled(true);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!email.getText().toString().isEmpty()) {
                        if (!MainActivity.isEmailValid(email.getText().toString())) {
                            tilEmail.setError(getString(R.string.incorrect_email));
                            signUp.setClickable(false);
                        } else {
                            tilEmail.setErrorEnabled(false);
                            signUp.setClickable(true);
                        }
                    } else if (email.getText().toString().isEmpty()) {
                        tilEmail.setError(getString(R.string.email_blank));
                        signUp.setClickable(false);
                    } else {
                        tilEmail.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilPassword.setErrorEnabled(true);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (password.getText().toString().isEmpty()) {
                        tilPassword.setError(getString(R.string.password_blank));
                        signUp.setClickable(false);
                    } else {
                        tilPassword.setErrorEnabled(false);
                        signUp.setClickable(true);
                    }
                }
            }
        });

        tilConfirmPassword.setErrorEnabled(true);
        confirmPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (confirmPassword.getText().toString().isEmpty()) {
                        tilConfirmPassword.setError(getString(R.string.password_blank));
                        signUp.setClickable(false);
                        Snackbar.make(v, getString(R.string.fill_all_fields), Snackbar.LENGTH_LONG).show();
                    } else {
                        tilConfirmPassword.setErrorEnabled(false);
                        confirmPassword.setClickable(true);
                    }
                }
            }
        });

        confirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    confirmPassword.setFocusable(false);
                    confirmPassword.setFocusableInTouchMode(true);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                } else {
                    return false;
                }
            }
        });

        signUp.setClickable(false);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .isNetworkAvailable()) {
                    new RegisterTask(RegistrationFragment.this, getActivity()).execute(firstName.getText().toString()
                            , secondName.getText().toString(), email.getText().toString()
                            , password.getText().toString(), confirmPassword.getText().toString());
                } else {
                    Snackbar.make(v, getString(R.string.check_internet), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ((MainActivity)getActivity()).updateNavigationViewPosition();
    }
}
