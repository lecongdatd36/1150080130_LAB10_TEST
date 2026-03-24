package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUserRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("job")
    private String job;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String name, String job) {
        this.name = name;
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public String getJob() {
        return job;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJob(String job) {
        this.job = job;
    }
}