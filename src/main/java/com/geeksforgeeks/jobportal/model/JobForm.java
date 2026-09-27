package com.geeksforgeeks.jobportal.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JobForm {
    @NotBlank @Size(max = 140)
    private String title;
    @NotBlank @Size(max = 120)
    private String company;
    @NotBlank @Size(max = 120)
    private String location;
    @NotBlank
    private String employmentType;
    @NotBlank @Size(max = 10000)
    private String description;
    @Size(max = 120)
    private String salaryRange;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
}
