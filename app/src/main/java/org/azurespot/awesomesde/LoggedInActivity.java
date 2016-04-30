package org.azurespot.awesomesde;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.azurespot.awesomesde.GithubAPI.GitCallClient;
import org.azurespot.awesomesde.GithubAPI.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoggedInActivity extends AppCompatActivity {

    ListView reposListView;
    ArrayList<String> reposArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        if (LoginMain.loggedIntoFacebook){
            Toast.makeText(this, "You are logged into Facebook!", Toast.LENGTH_LONG).show();
        }
        else if (LoginMain.loggedIntoGitHub){
            Button myRepos = (Button)findViewById(R.id.repoButton);
            myRepos.setVisibility(View.VISIBLE);
        }
    }

    // Button click to access My Repos
    public void listMyRepos(View view){
        reposListView = (ListView) findViewById(R.id.githubReposList);

        // Grabs the URI from the OAuth2 Intent... hold on to it for now
        Bundle extras = getIntent().getExtras();
        Uri githubURI = Uri.parse(extras.getString("Uri"));

        // Once logged in, create REST adapter which points to the GitHub API endpoint.
        GitCallClient client = ServiceGenerator.createService(GitCallClient.class);

        // Fetch and print user of this account.
        Call<List<GitCallClient.UserRepos>> call = client.getUserRepos("noni-mizu");
        call.enqueue(new Callback<List<GitCallClient.UserRepos>>() {
            @Override
            public void onResponse(Call<List<GitCallClient.UserRepos>> call,
                                   final Response<List<GitCallClient.UserRepos>> response) {

                if (response.isSuccessful()) {
                    reposArrayList = new ArrayList<>();
                    for (GitCallClient.UserRepos repoName : response.body()) {
                        // add each repo to ArrayList
                        reposArrayList.add(repoName.getName());
                        System.out.println("REPOS FROM GITHUB: " + repoName.getName());
                    }
                    // AFTER GITHUB CALL MADE, PUT RESULTS THROUGH ADAPTER TO LISTVIEW
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoggedInActivity.this,
                            android.R.layout.simple_list_item_1, reposArrayList);
                    reposListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<GitCallClient.UserRepos>> call, Throwable t) {
                Log.e("Error fetching name", t.getMessage());
                Log.e("Request failed: ",
                        "Cannot request GitHub repo names.");
            }
        });
    }
}
