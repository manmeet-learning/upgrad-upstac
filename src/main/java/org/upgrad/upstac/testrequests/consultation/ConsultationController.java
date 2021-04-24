package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;


    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;
    
    //Get all tests which are due for consultation and not assigned to any doctor.
    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {

        //Fetch and return all tests which are due for consultation and not assigned to any doctor.
        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    //Get all tests which are assigned to logged-in doctor.
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {

        //Get logged-in user(doctor) related details.
        User loggedInUser=userLoggedInService.getLoggedInUser();

        //Return list of tests which are assigned to logged-in user(doctor).
        return testRequestQueryService.findByDoctor(loggedInUser);
    }

    //Assign test to logged in doctor & return test details.
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        try {
            //Get logged-in user(doctor) related details.
            User loggedInUser =userLoggedInService.getLoggedInUser();

            //Assign test to logged in doctor & return test details.
            return testRequestUpdateService.assignForConsultation(id,loggedInUser);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

    //Update consultation details & return test details.
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {
        try {
            //Get logged-in user(doctor) related details.
            User loggedInUser =userLoggedInService.getLoggedInUser();

            //Update consultation details & return test details.
            return testRequestUpdateService.updateConsultation(id,testResult,loggedInUser);
        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }
}
