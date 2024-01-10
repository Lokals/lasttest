package com.mastertest.lasttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.*;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.EmployeePositionRepository;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.employee.EmployeePositionService;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.person.PersonService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PersonControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private PersonService personService;

    @Autowired
    private PersonManagementProperties properties;

    @Autowired
    private EmployeePositionService employeePositionService;

    @Autowired
    private EmployeePositionRepository employeePositionRepository;
    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private ImportStatusService importStatusService;

    @Autowired
    private ImportStatusRepository importStatusRepository;

    private Employee employee;
    private Employee employee2;
    private Student student;
    private Retiree retiree;

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


        student = new Student();
        createPerson(student, "12345678902");
        student.setScholarship(1000.0);
        student.setStudyField("Science");
        student.setUniversityName("University");
        student.setYearOfStudy(1);
        personRepository.save(student);


        retiree = new Retiree();
        createPerson(retiree, "12345678903");
        retiree.setHeight(160.0);
        retiree.setYearsWorked(10);
        retiree.setPensionAmount(5000.0);
        personRepository.save(retiree);

        employeePosition = new EmployeePosition();
        employeePosition.setEmployee(employee);
        employeePosition.setSalary(20.0);
        employeePosition.setPositionName(employee.getPosition());
        employeePosition.setStartDate(LocalDate.now());
        employeePosition.setEndDate(LocalDate.now().plusDays(50));
        employeePositionRepository.save(employeePosition);

    }


    private void createPerson(Person person, String pesel){
        person.setLastName("Test");
        person.setFirstName("Testing");
        person.setPesel(pesel);
        person.setHeight(180.0);
        person.setWeight(80.0);
        person.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
        employeePositionRepository.deleteAll();
        importStatusRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_ResultCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("marcin.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pesel").value("12345678910"))
                .andExpect(jsonPath("$.firstName").value("jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.height").value("180.0"))
                .andExpect(jsonPath("$.weight").value("75.0"))
                .andExpect(jsonPath("$.email").value("marcin.kowalski@example.com"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidMail_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("marcin.kowalskiexample.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("email"))
                .andExpect(jsonPath("$.violations[0].message").value("must be a well-formed email address"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidType_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType(null);
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("type"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidFirstName_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName(null);
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidLastName_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName(null);
        command.setPesel("12345678910");
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("lastName"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidPesel_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel(null);
        command.setHeight(180.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("pesel"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be blank"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidHeight_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(null);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("height"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be null"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidWeight_ResultNotCreatedNewPerson() throws Exception {
        CreatePersonCommand command = new CreatePersonCommand();
        command.setType("employee");
        command.setFirstName("jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(75.0);
        command.setWeight(null);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("weight"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be null"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_ResultUpdatedPerson() throws Exception {
        Long id = employee.getId();
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(175.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pesel").value("12345678910"))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.height").value("175.0"))
                .andExpect(jsonPath("$.weight").value("75.0"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidFirstName_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("123");
        command.setLastName("Kowalski");
        command.setPesel("12345678910");
        command.setHeight(175.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_[A-Z][a-z]{1,19}"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidLastName_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("123");
        command.setPesel("12345678910");
        command.setHeight(175.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("lastName"))
                .andExpect(jsonPath("$.violations[0].message").value("PATTERN_MISMATCH_^[A-Z][a-z]{2,19}(?:-[A-Z][a-z]{2,19})?$"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidPeselLength_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("Kowalewski");
        command.setPesel("123456789101");
        command.setHeight(175.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("pesel"))
                .andExpect(jsonPath("$.violations[0].message").value("Pesel have to have exactly 11 digits"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidPeselLetters_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("Kowalewski");
        command.setPesel("qwertyuiopq");
        command.setHeight(175.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("pesel"))
                .andExpect(jsonPath("$.violations[0].message").value("Pesel can have only digits"));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidHeight_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("Kowalewski");
        command.setPesel("12345678901");
        command.setHeight(0.0);
        command.setWeight(75.0);
        command.setEmail("jan.kowalski@example.com");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("height"))
                .andExpect(jsonPath("$.violations[0].message").value("MIN_VALUE_1.0"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidWeight_ResultNotUpdatedPerson() throws Exception {
        UpdatePersonCommand command = new UpdatePersonCommand();
        command.setFirstName("Jan");
        command.setLastName("Kowalewski");
        command.setPesel("12345678901");
        command.setHeight(10.0);
        command.setWeight(1.0);
        command.setEmail("jan.kowalski@example.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("validation errors"))
                .andExpect(jsonPath("$.violations[0].field").value("weight"))
                .andExpect(jsonPath("$.violations[0].message").value("MIN_VALUE_1.0"));
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

        mockMvc.perform(post("/api/people/employees/"+empId+"/positions")
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

        mockMvc.perform(post("/api/people/employees/"+empId+"/positions")
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

        mockMvc.perform(post("/api/people/employees/"+empId+"/positions")
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

        mockMvc.perform(post("/api/people/employees/"+empId+"/positions")
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

        mockMvc.perform(post("/api/people/employees/"+empId+"/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The start date of the position cannot be later than the end date."));

    }
    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testGetImportStatus_ReturnsStatus() throws Exception {
        ImportStatus testStatus = createAndPersistTestImportStatus();

        mockMvc.perform(get("/api/people/importstatus/" + testStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testStatus.getId()))
                .andExpect(jsonPath("$.status").value(testStatus.getStatus().toString()))
                .andExpect(jsonPath("$.filename").value(testStatus.getFilename()));

    }

    private ImportStatus createAndPersistTestImportStatus() {
        ImportStatus status = new ImportStatus();
        status.setFilename("testFile.csv");
        status.setStatus(StatusFile.PENDING);
        status.setStartTime(LocalDateTime.now());
        return importStatusRepository.saveAndFlush(status);
    }

    @Test
    void testWhenSearchByStudentType_thenReturnMatchingPersonsTypeStudent() throws Exception {
        mockMvc.perform(get("/api/people/search")
                        .param("type", "student"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("student"))))
                .andExpect(jsonPath("$.content[0].pesel").value(student.getPesel()))
                .andExpect(jsonPath("$.content[0].firstName").value(student.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(student.getLastName()))
                .andExpect(jsonPath("$.content[0].height").value(student.getHeight()))
                .andExpect(jsonPath("$.content[0].weight").value(student.getWeight()))
                .andExpect(jsonPath("$.content[0].email").value(student.getEmail()))
                .andExpect(jsonPath("$.content[0].universityName").value(student.getUniversityName()))
                .andExpect(jsonPath("$.content[0].yearOfStudy").value(student.getYearOfStudy()))
                .andExpect(jsonPath("$.content[0].studyField").value(student.getStudyField()))
                .andExpect(jsonPath("$.content[0].scholarship").value(student.getScholarship()))
        ;
    }

    @Test
    void testWhenSearchByEmployeeType_thenReturnMatchingPersonsTypeEmployee() throws Exception {
        mockMvc.perform(get("/api/people/search")
                        .param("type", "employee"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("employee"))))
                .andExpect(jsonPath("$.content[0].pesel").value(employee.getPesel()))
                .andExpect(jsonPath("$.content[0].firstName").value(employee.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(employee.getLastName()))
                .andExpect(jsonPath("$.content[0].height").value(employee.getHeight()))
                .andExpect(jsonPath("$.content[0].weight").value(employee.getWeight()))
                .andExpect(jsonPath("$.content[0].email").value(employee.getEmail()))
                .andExpect(jsonPath("$.content[0].position").value(employee.getPosition()))
                .andExpect(jsonPath("$.content[0].salary").value(employee.getSalary()))
        ;
    }
    @Test
    void testWhenSearchByName_thenReturnMatchingPersons() throws Exception {
        mockMvc.perform(get("/api/people/search")
                        .param("name", "Testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].firstName", everyItem(is("Testing"))));
    }

    @Test
    void testWhenSearchByHeightRange_thenReturnZeroMatchingPersons() throws Exception {
        mockMvc.perform(get("/api/people/search")
                        .param("heightFrom", "170")
                        .param("heightTo", "179"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content", hasSize(0)));
     }

    @Test
    void testWhenSearchByHeightRange_thenReturnMatchingPersons() throws Exception {
        mockMvc.perform(get("/api/people/search")
                        .param("heightFrom", "161")
                        .param("heightTo", "182"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void testWhenRequestPageExceedsDefaultSize_thenLimitToDefaultSize() throws Exception {
        int defaultPageSize = properties.getDefaultPageSize();

        mockMvc.perform(get("/api/people/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(defaultPageSize)));
    }

}