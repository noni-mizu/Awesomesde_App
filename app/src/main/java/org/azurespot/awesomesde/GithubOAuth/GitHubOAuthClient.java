package org.azurespot.awesomesde.GithubOAuth;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by mizu on 4/15/16.
 */
public interface GitHubOAuthClient {

    // Put all API definitions here
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    // Get the user for login oAuth
    @GET("/{user}")
    Call<User> user(
            @Path("user") String user
    );

    class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }
}
