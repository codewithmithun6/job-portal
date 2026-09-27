package com.geeksforgeeks.jobportal.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApplicationForm {
    @NotBlank @Size(max = 5000)
    private String coverLetter;

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
