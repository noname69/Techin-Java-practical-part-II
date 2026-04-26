package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonInt;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonPresent;
import static lt.techin.taskmanager.rest.TestResponseAssertions.expectStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userValidationRejectsBlankNameAndInvalidEmail() throws Exception {
        MvcResult result = expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "email": "not-an-email"
                                }
                                """)),
                400,
                "POST /api/users should return 400 Bad Request when name is blank and email is invalid."
        );
        assertJsonInt(result, "/status", 400, "Validation error response should include status 400.");
        assertJsonPresent(result, "/fieldErrors/0/field", "Validation error response should include fieldErrors entries so students can see which input failed.");
    }

    @Test
    void taskCreateAndUpdateKeepValidationRules() throws Exception {
        expectStatus(mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Validation Project",
                                  "description": "Project for task validation"
                                }
                                """)),
                201,
                "Project setup for validation tests should succeed before task validation is checked."
        );

        MvcResult blankTitleResult = expectStatus(mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Invalid title",
                                  "dueDate": "2099-05-01"
                                }
                                """)),
                400,
                "POST /api/projects/{id}/tasks should return 400 when title is blank."
        );
        assertJsonInt(blankTitleResult, "/status", 400, "Blank task title validation response should include status 400.");
        assertJsonPresent(blankTitleResult, "/fieldErrors/0/field", "Blank task title validation response should include fieldErrors.");

        MvcResult missingDueDateResult = expectStatus(mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid task",
                                  "description": "Missing due date"
                                }
                                """)),
                400,
                "POST /api/projects/{id}/tasks should return 400 when dueDate is missing."
        );
        assertJsonInt(missingDueDateResult, "/status", 400, "Missing dueDate validation response should include status 400.");
        assertJsonPresent(missingDueDateResult, "/fieldErrors/0/field", "Missing dueDate validation response should include fieldErrors.");

        expectStatus(mockMvc.perform(post("/api/projects/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Existing task",
                                  "description": "Ready for update",
                                  "dueDate": "2099-05-01"
                                }
                                """)),
                201,
                "Valid task creation should succeed before task update validation is checked."
        );

        MvcResult missingStatusResult = expectStatus(mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated task",
                                  "description": "Status is missing",
                                  "dueDate": "2099-05-02",
                                  "projectId": 1
                                }
                                """)),
                400,
                "PUT /api/tasks/{id} should return 400 when status is missing from the full update request."
        );
        assertJsonInt(missingStatusResult, "/status", 400, "Missing task status validation response should include status 400.");
        assertJsonPresent(missingStatusResult, "/fieldErrors/0/field", "Missing task status validation response should include fieldErrors.");

        MvcResult shortTitlePastDateResult = expectStatus(mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Hi",
                                  "description": "Too short title",
                                  "status": "TODO",
                                  "dueDate": "2000-01-01",
                                  "projectId": 1
                                }
                                """)),
                400,
                "PUT /api/tasks/{id} should return 400 when title is too short or dueDate is in the past."
        );
        assertJsonInt(shortTitlePastDateResult, "/status", 400, "Invalid task update response should include status 400.");
        assertJsonPresent(shortTitlePastDateResult, "/fieldErrors/0/field", "Invalid task update response should include fieldErrors.");
    }
}
