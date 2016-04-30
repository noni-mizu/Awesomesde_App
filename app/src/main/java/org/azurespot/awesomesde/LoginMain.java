package org.azurespot.awesomesde;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.azurespot.awesomesde.GithubOAuth.AccessToken;
import org.azurespot.awesomesde.GithubOAuth.LoginService;
import org.azurespot.awesomesde.GithubOAuth.ServiceGenerator2;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

import retrofit2.Call;

/*
This class handles Facebook, Github, and LinkedIn OAuth... plus
email and password that access a database.
*/

public class LoginMain extends AppCompatActivity {
    CallbackManager callbackManager;
    LoginButton loginButtonFB;
    Button loginButtonLI;
    Button loginButtonGH;
    public static boolean loggedIntoFacebook = false;
    public static boolean loggedIntoGitHub = false;
    private ProgressDialog pd;

    private static final String GITHUB_CLIENT_ID = "b822745ef37ae750cea5";
    private static final String GITHUB_CLIENT_SECRET = "86adabd4013434f3506c089b75a13acbb27b6f72";
    private static final String REDIRECT_URL = "http://interviewhelp.io/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        // must be placed after above 2 lines
        setContentView(R.layout.activity_login_main);

        loginButtonLI = (Button) findViewById(R.id.linkedInButton);

        loginButtonFB = (LoginButton) findViewById(R.id.facebookButton);
        loginButtonFB.setReadPermissions("user_friends");

        loginButtonGH = (Button) findViewById(R.id.githubButton);

        // Callback registration for Facebook
        loginButtonFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleSignInResult(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        LoginManager.getInstance().logOut();
                        return null;
                    }
                });
            }

            @Override
            public void onCancel() {
                handleSignInResult(null);
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(LoginMain.class.getCanonicalName(), exception.getMessage());
                handleSignInResult(null);
            }
        });

        // In case your key hash magically changes, this will print out the key hash
        // so you can use that new number and put it into your Facebook developer account
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "org.azurespot.awesomesde",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



    } // end onCreate


    // for Facebook login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Callable<Void> logout) {
        if(logout == null) {
            /* Login error */
            Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
        } else {
            /* Login success */
            Application.getInstance().setLogoutCallable(logout);
            loggedIntoFacebook = true;
            startActivity(new Intent(this, LoggedInActivity.class));
        }
    }

    // for LinkedIn login, once button is clicked...
    public void chooseLinkedIn(View view){
        // set up Alert Dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Show a progress dialog to the user
        pd = ProgressDialog.show(this, "", this.getString(R.string.loading),true);

        // inflate WebView layout for Alert Dialog
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.linkedin_webview, null);
        final WebView webView = (WebView) v.findViewById(R.id.webViewLinkedIn);
        webView.requestFocus(View.FOCUS_DOWN);

        // processes login details in AsyncTask
        LinkedInLogin lil = new LinkedInLogin();
        WebView processedWV = lil.wvClient(webView, pd);

        // put WebView in Dialog box
        alert.create();
        alert.setView(processedWV);
        alert.show();

        // dismiss progress bar
        if(pd!=null && pd.isShowing()){
            pd.dismiss();
        }
    }

    public void chooseGitHub(View view){
        Intent intent = new Intent(LoginMain.this, LoggedInActivity.class);
        intent.putExtra("Uri", (Uri.parse(ServiceGenerator2.API_BASE_URL
                + "#/login"
                + "?client_id="
                + GITHUB_CLIENT_ID
                + "&redirect_uri="
                + REDIRECT_URL).toString()));
        startActivity(intent);

        // the intent filter defined in AndroidManifest will handle the
        // return from ACTION_VIEW intent
        Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(REDIRECT_URL)) {
            // use the parameter your API exposes for the code (mostly it's "code")
            String code = uri.getQueryParameter("code");
            if (code != null) {
                // get access token
                LoginService loginService =
                        ServiceGenerator2.createService(LoginService.class,
                                GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET);
                Call<AccessToken> call = loginService.getAccessToken(code,
                        "authorization_code");
                try {
                    AccessToken accessToken = call.execute().body();

                    Intent postLoginIntent = new Intent(LoginMain.this,
                            LoggedInActivity.class);
                    postLoginIntent.putExtra("Uri", uri.toString());
                    startActivity(postLoginIntent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (uri.getQueryParameter("error") != null) {
                System.out.println("Access token not acquired.");
            }
        }

        loggedIntoGitHub = true;
    }

    public void runProgressDialog(){
        pd = ProgressDialog.show(LoginMain.this, "", LoginMain.this
                .getString(R.string.loading),true);
    }

    public void changeActivity(Boolean status){

        if(status){
            //If everything went Ok, change to another activity.
            Intent startProfileActivity = new Intent(LoginMain.this, LoggedInActivity.class);
            LoginMain.this.startActivity(startProfileActivity);
        }
    }
}
