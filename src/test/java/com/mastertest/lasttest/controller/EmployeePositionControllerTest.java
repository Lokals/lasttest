package com.mastertest.lasttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.EmployeePosition;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.repository.EmployeePositionRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.employee.EmployeePositionService;
import com.mastertest.lasttest.service.person.PersonService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeePositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeePositionService employeePositionService;

    @Autowired
    private EmployeePositionRepository employeePositionRepository;
    @Autowired
    private PersonManagementProperties properties;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;
    private Employee employee;
    private Employee employee2;

    private EmployeePosition employeePosition;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        createPerson(employee, "12345678901");
        employee.setSalary(5000.0);
        employee.setPosition("Tester");
        employee.setEmploymentDate(Date.valueOf("2022-01-01"));
        personRepository.save(employee);

        employee2 = new Employee();
        createPerson(employee2, "12345678909");
        employee2.setSalary(5000.0);
        employee2.setPosition("Dusiciel");
        employee2.setEmploymentDate(Date.valueOf("2022-01-01"));
        personRepository.save(employee2);

        employeePosition = new EmployeePosition();
        employeePosition.setEmployee(employee);
        employeePosition.setSalary(20.0);
        employeePosition.setPositionName(employee.getPosition());
        employeePosition.setStartDate(LocalDate.now());
        employeePosition.setEndDate(LocalDate.now().plusDays(50));
        employeePositionRepository.save(employeePosition);

    }

    private void createPerson(Person person, String pesel) {
        person.setLastName("Test");
        person.setFirstName("Testing");
        person.setPesel(pesel);
        person.setHeight(180.0);
        person.setWeight(80.0);
        person.setType("employee");
        person.setVersion(0L);
        person.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
        employeePositionRepository.deleteAll();

    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdateEmployeePosition_ResultUpdatedEmployeePosition() throws Exception {

        Long empId = employee.getId();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(50);
        String position = "Architekt";
        Double salary = 12.12;
        UpdateEmployeePositionCommand command = new UpdateEmployeePositionCommand();
        command.setSalary(salary);
        command.setPositionName(position);
        command.setEndDate(end);
        command.setStartDate(start);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/employees/" + empId + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positionName").value(position))
                .andExpect(jsonPath("$.startDate").value(start.toString()))
                .andExpect(jsonPath("$.endDate").value(end.toString()))
                .andExpect(jsonPath("$.salary").value(salary.toString()));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdateEmployeePosition_InvalidSalary_ResultNotUpdatedEmployeePosition() throws Exception {

        Long empId = employee.getId();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(50);
        String position = "Architekt";

        UpdateEmployeePositionCommand command = new UpdateEmployeePositionCommand();

        command.setPositionName(position);
        command.setEndDate(end);
        command.setStartDate(start);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/employees/" + empId + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("salary"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be null"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdateEmployeePosition_InvalidPosition_ResultNotUpdatedEmployeePosition() throws Exception {

        Long empId = employee.getId();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(50);
        Double salary = 12.12;
        UpdateEmployeePositionCommand command = new UpdateEmployeePositionCommand();
        command.setSalary(salary);
        command.setEndDate(end);
        command.setStartDate(start);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/employees/" + empId + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("positionName"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdateEmployeePosition_InvalidRepeatedPosition_ResultUpdatedEmployeePosition() throws Exception {

        Long empId = employee2.getId();
        LocalDate start = employeePosition.getStartDate();
        LocalDate end = employeePosition.getEndDate();
        String position = employeePosition.getPositionName();
        Double salary = 12.12;
        UpdateEmployeePositionCommand command = new UpdateEmployeePositionCommand();
        command.setSalary(salary);
        command.setPositionName(position);
        command.setEndDate(end);
        command.setStartDate(start);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/employees/" + empId + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The position Tester is already occupied during the specified period."));

    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdateEmployeePosition_InvalidEndDayBeforeStartDay_ResultUpdatedEmployeePosition() throws Exception {

        Long empId = employee.getId();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(50);
        String position = "Tester Starszy";
        Double salary = 12.12;
        UpdateEmployeePositionCommand command = new UpdateEmployeePositionCommand();
        command.setSalary(salary);
        command.setPositionName(position);
        command.setEndDate(end);
        command.setStartDate(start);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/employees/" + empId + "/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The start date of the position cannot be later than the end date."));
    }

}