package com.mastertest.lasttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.persons.Employee;
import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.model.persons.Retiree;
import com.mastertest.lasttest.model.persons.Student;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.repository.EmployeePositionRepository;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.employee.EmployeePositionService;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    private Student student;
    private Retiree retiree;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        createPerson(employee, "12345678901", "employee");
        employee.setSalary(5000.0);
        employee.setPosition("Tester");
        employee.setEmploymentDate(Date.valueOf("2022-01-01"));
        personRepository.save(employee);

        student = new Student();
        createPerson(student, "12345678902", "student");
        student.setScholarship(1000.0);
        student.setStudyField("Science");
        student.setUniversityName("University");
        student.setYearOfStudy(1);
        personRepository.save(student);

        retiree = new Retiree();
        createPerson(retiree, "12345678903", "retiree");
        retiree.setHeight(160.0);
        retiree.setYearsWorked(10);
        retiree.setPensionAmount(5000.0);
        personRepository.save(retiree);

    }


    private void createPerson(Person person, String pesel, String type) {
        person.setLastName("Test");
        person.setFirstName("Testing");
        person.setPesel(pesel);
        person.setType(type);
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
        StudentDto studentDetails = new StudentDto();
        studentDetails.setPesel("91923412341");
        studentDetails.setFirstName("Pani");
        studentDetails.setLastName("Babcia");
        studentDetails.setHeight(180.0);
        studentDetails.setWeight(50.0);
        studentDetails.setScholarship(1000.0);
        studentDetails.setEmail("pani.babcia@test.com");
        studentDetails.setUniversityName("Univerek");
        studentDetails.setStudyField("Testowanie");
        studentDetails.setYearOfStudy(1);

        CreatePersonCommand<StudentDto> command = new CreatePersonCommand<>("student", studentDetails);


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pesel").value("91923412341"))
                .andExpect(jsonPath("$.firstName").value("Pani"))
                .andExpect(jsonPath("$.lastName").value("Babcia"))
                .andExpect(jsonPath("$.height").value("180.0"))
                .andExpect(jsonPath("$.weight").value("50.0"))
                .andExpect(jsonPath("$.email").value("pani.babcia@test.com"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidData_ResultNewPersonDoNotCreated() throws Exception {
        StudentDto studentDetails = new StudentDto();
        studentDetails.setPesel("91923412341123");
        studentDetails.setFirstName("Pani");
        studentDetails.setLastName("Babcia");
        studentDetails.setHeight(180.0);
        studentDetails.setWeight(50.0);
        studentDetails.setScholarship(1000.0);
        studentDetails.setEmail("pani.babcia@test.com");
        studentDetails.setUniversityName("Univerek");
        studentDetails.setStudyField("Testowanie");
        studentDetails.setYearOfStudy(1);

        CreatePersonCommand<StudentDto> command = new CreatePersonCommand<>("student", studentDetails);


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("pesel: PESEL must be exactly 11 characters long\n"));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidDataEmail_ResultNewPersonDoNotCreated() throws Exception {
        StudentDto studentDetails = new StudentDto();
        studentDetails.setPesel("99999999999");
        studentDetails.setFirstName("Pani");
        studentDetails.setLastName("Babcia");
        studentDetails.setHeight(180.0);
        studentDetails.setWeight(50.0);
        studentDetails.setScholarship(1000.0);
        studentDetails.setEmail("pani.babciatest.com");
        studentDetails.setUniversityName("Univerek");
        studentDetails.setStudyField("Testowanie");
        studentDetails.setYearOfStudy(1);

        CreatePersonCommand<StudentDto> command = new CreatePersonCommand<>("student", studentDetails);


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("email: Invalid email format\n"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testAddPerson_InvalidDataNoStudyFieldBadEmail_ResultNewPersonDoNotCreated() throws Exception {
        StudentDto studentDetails = new StudentDto();
        studentDetails.setPesel("99999999999");
        studentDetails.setFirstName("Pani");
        studentDetails.setLastName("Babcia");
        studentDetails.setHeight(180.0);
        studentDetails.setWeight(50.0);
        studentDetails.setScholarship(1000.0);
        studentDetails.setEmail("pani.babcia@test.com");
        studentDetails.setUniversityName("Univerek");
        studentDetails.setYearOfStudy(1);

        CreatePersonCommand<StudentDto> command = new CreatePersonCommand<>("student", studentDetails);


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/api/people/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("studyField: Study field cannot be blank\n"));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_Successful() throws Exception {
        String personId = student.getPesel();
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("firstName", "Updated");
        updateFields.put("lastName", "Updatedlast");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updateFields);

        mockMvc.perform(post("/api/people/"+ personId +"/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Updatedlast"));

    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_NotExistingPerson_ResultNotSuccessful() throws Exception {
        Long personId = 999L;
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("firstName", "Updated");
        updateFields.put("lastName", "Updated");


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updateFields);

        mockMvc.perform(post("/api/people/"+ personId+"/update" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("person with pesel=999 not found"));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidEmail_ResultNotSuccessful() throws Exception {
        String personId = student.getPesel();
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("email", "wrong.mail.com");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updateFields);

        mockMvc.perform(post("/api/people/"+ personId+" /update" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testUpdatePerson_InvalidPesel_ResultNotSuccessful() throws Exception {
        String peselPerson
                = student.getPesel();
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("pesel", "123");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updateFields);

        mockMvc.perform(post("/api/people/"+ peselPerson +"/update" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ResultAllPersonsReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByPesel_ResultPersonReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("pesel", "12345678901"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(1));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByType_ResultPersonReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("type", "employee"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByWeight_ResultPersonReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("weight", "1,100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByHeight_ResultZeroPersonReturned() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("height", "50,170"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByHeight_ResultPersonReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("height", "100,200"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(3));
    }


    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByHeightAndType_ResultPersonReturnedSuccessful() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("height", "100,200")
                        .param("type", "employee"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByHeightAndName_ResultZeroPersonReturned() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("height", "100,200")
                        .param("firstName", "Jan"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_InvalidParamProvided_ResultZeroPersonReturned() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("test", "100,200")
                        .param("firstName", "Test"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "adminpassword", roles = "ADMIN")
    void testSearch_ByNameAndHeight_ResultPersonsReturned() throws Exception {

        mockMvc.perform(get("/api/people/search")
                        .param("height", "100,200")
                        .param("firstName", "Test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(properties.getDefaultPageSize()))))
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}