package org.ecomap.android.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.sync.LoginTask;
import org.ecomap.android.app.utils.NetworkAvailability;

public class LoginFragment extends DialogFragment {

    private AutoCompleteTextView email;
    private EditText password;
    private Button signIn;
    private TextView signUpLink;
    private TextInputLayout tilEmail, tilPass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_layout, container, false);
        getDialog().setTitle(getString(R.string.sign_in));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        email = (AutoCompleteTextView) getView().findViewById(R.id.email);
        password = (EditText) getView().findViewById(R.id.password);
        signIn = (Button) getView().findViewById(R.id.email_sign_in_button);
        signUpLink = (TextView) getView().findViewById(R.id.link_to_register);

        tilEmail = (TextInputLayout) getView().findViewById(R.id.til_email);
        tilEmail.setErrorEnabled(true);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!email.getText().toString().isEmpty()) {
                        if (!MainActivity.isEmailValid(email.getText().toString())) {
                            tilEmail.setError(getString(R.string.incorrect_email));
                            signIn.setClickable(false);
                            Snackbar.make(v, getString(R.string.fill_all_fields), Snackbar.LENGTH_LONG).show();
                        } else {
                            tilEmail.setErrorEnabled(false);
                            signIn.setClickable(true);
                        }
                    } else if (email.getText().toString().isEmpty()) {
                        tilEmail.setError(getString(R.string.email_blank));
                        signIn.setClickable(false);
                        Snackbar.make(v, getString(R.string.fill_all_fields), Snackbar.LENGTH_LONG).show();
                    } else {
                        tilEmail.setErrorEnabled(false);
                        signIn.setClickable(true);
                    }
                }
            }
        });

        tilPass = (TextInputLayout) getView().findViewById(R.id.til_password);
        tilPass.setErrorEnabled(true);
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (password.getText().toString().isEmpty()) {
                        tilPass.setError(getString(R.string.password_blank));
                        signIn.setClickable(false);
                        Snackbar.make(v, getString(R.string.fill_all_fields), Snackbar.LENGTH_LONG).show();
                    } else {
                        tilPass.setErrorEnabled(false);
                        signIn.setClickable(true);
                    }
                }
            }
        });

        signIn.setClickable(false);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new NetworkAvailability(getActivity().getSystemService(Context.CONNECTIVITY_SERVICE))
                        .isNetworkAvailable()) {
                    new LoginTask(LoginFragment.this, getActivity()).execute(email.getText().toString()
                            , password.getText().toString());
                } else {
                    Snackbar.make(v, getString(R.string.check_internet), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switch to Registration Fragment
                new RegistrationFragment().show(getFragmentManager(), "registration_layout");
                dismiss();
            }
        });
    }
}
