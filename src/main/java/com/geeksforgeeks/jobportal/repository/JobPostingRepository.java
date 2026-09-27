package com.geeksforgeeks.jobportal.repository;

import com.geeksforgeeks.jobportal.model.JobPosting;
import com.geeksforgeeks.jobportal.model.JobPosting.Status;
import com.geeksforgeeks.jobportal.model.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    @Query("select j from JobPosting j where j.status = :status " +
            "and (:q = '' or lower(j.title) like lower(concat('%', :q, '%')) or lower(j.company) like lower(concat('%', :q, '%'))) " +
            "and (:location = '' or lower(j.location) like lower(concat('%', :location, '%'))) " +
            "order by j.postedAt desc")
    List<JobPosting> searchOpen(@Param("q") String q, @Param("location") String location,
                                @Param("status") Status status);
    List<JobPosting> findByRecruiterOrderByPostedAtDesc(PortalUser recruiter);
    Optional<JobPosting> findByIdAndStatus(Long id, JobPosting.Status status);
    boolean existsByTitleAndCompany(String title, String company);
}
