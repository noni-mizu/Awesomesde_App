package org.azurespot.awesomesde.GithubAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by mizu on 4/25/16.
 */
public interface GitCallClient {

    @GET("/users/{username}/repos")
    Call<List<UserRepos>> getUserRepos(
            @Path("username") String name
    );

    class UserRepos {
        String name;

        public UserRepos() {}

        public String getName(){
            return name;
        }
    }
}
