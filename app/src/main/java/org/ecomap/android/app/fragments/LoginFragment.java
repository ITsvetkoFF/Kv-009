package org.ecomap.android.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.ecomap.android.app.R;
import org.ecomap.android.app.activities.MainActivity;
import org.ecomap.android.app.tasks.LoginTask;
import org.ecomap.android.app.tasks.SocialLoginTask;
import org.ecomap.android.app.utils.NetworkAvailability;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginFragment extends DialogFragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private EditText email;
    private EditText password;
    private Button signIn;
    private TextInputLayout tilEmail, tilPass;
    private InputMethodManager imm;
    private Context mContext;
    private Activity mActivity;
    private TextView signUpLink;

    private CallbackManager callbackManager;
    private LoginManager loginManager;

    //Google
    public static final int RC_SIGN_IN = 101;
    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();
        mActivity = getActivity();
        mContext = getActivity().getApplicationContext();
        //Google
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Google
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        //Google
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
        //Facebook
        if (loginManager != null) {
            loginManager.logOut();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_layout, container, false);
        getDialog().setTitle(getString(R.string.sign_in));

        //Google
        Button googleLoginButton = (Button) view.findViewById(R.id.google_login_button);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGplus();
            }
        });

        Button facebookLoginButton = (Button) view.findViewById(R.id.facebook_login_button);
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager.logInWithReadPermissions(LoginFragment.this, Arrays.asList("public_profile, email"));
            }
        });

        // Callback registration
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                try {
                                    new SocialLoginTask(LoginFragment.this, getActivity())
                                            .execute("facebook",
                                                    jsonObject.getString("first_name"),
                                                    jsonObject.getString("last_name"),
                                                    jsonObject.getString("id"),
                                                    jsonObject.getString("email"));
                                } catch (Exception e) {
                                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager.onActivityResult(requestCode, resultCode, data))
            return;

        if (requestCode == RC_SIGN_IN) {
            if (resultCode != MainActivity.RESULT_OK) {
                mSignInClicked = false;
            }
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        if (v != null) {
            email = (EditText) v.findViewById(R.id.email_login);
            password = (EditText) v.findViewById(R.id.password);

            signIn = (Button) v.findViewById(R.id.email_sign_in_button);
            signUpLink = (TextView) v.findViewById(R.id.link_to_register);

            tilEmail = (TextInputLayout) v.findViewById(R.id.til_email);
            tilPass = (TextInputLayout) v.findViewById(R.id.til_password);
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);


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

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    password.setFocusable(false);
                    password.setFocusableInTouchMode(true);

                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return true;
                } else {
                    return false;
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

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ((MainActivity) getActivity()).updateNavigationViewPosition();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mActivity, 0).show();
            Log.e(TAG, "" + connectionResult.getErrorCode());
            return;
        }
        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = connectionResult;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                Log.e(TAG, "" + connectionResult.getErrorCode());
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        getProfileInformation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    /**
     * Sign-in into google
     */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Method to resolve any sign-in errors
     */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(mActivity, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String[] wholeName = personName.split(" ", 2);
                String firstName = wholeName[0];
                String lastName = wholeName[1];
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                new SocialLoginTask(LoginFragment.this, getActivity())
                        .execute("google",
                                firstName,
                                lastName,
                                personGooglePlusProfile,
                                email);
            } else {
                Toast.makeText(mContext,
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}