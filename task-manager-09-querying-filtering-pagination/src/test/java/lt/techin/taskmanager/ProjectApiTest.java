package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonBoolean;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonInt;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonText;
import static lt.techin.taskmanager.rest.TestResponseAssertions.expectStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createListGetUpdatePatchAndDeleteWorkForProjects() throws Exception {
        MvcResult createResult = expectStatus(mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spring Course",
                                  "description": "Main training project"
                                }
                                """)),
                201,
                "POST /api/projects should create a project and return 201 Created."
        );
        assertJsonInt(createResult, "/id", 1, "Created project response should include the generated id.");
        assertJsonText(createResult, "/name", "Spring Course", "Created project response should include the submitted name.");
        assertJsonBoolean(createResult, "/archived", false, "New projects should start as archived=false.");

        MvcResult listResult = expectStatus(
                mockMvc.perform(get("/api/projects")),
                200,
                "GET /api/projects should return 200 OK."
        );
        assertJsonText(listResult, "/0/name", "Spring Course", "GET /api/projects should list the created project.");

        MvcResult getResult = expectStatus(
                mockMvc.perform(get("/api/projects/1")),
                200,
                "GET /api/projects/{id} should return 200 OK for an existing project."
        );
        assertJsonText(getResult, "/description", "Main training project", "GET /api/projects/{id} should return the stored project description.");

        MvcResult updateResult = expectStatus(mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spring Course Updated",
                                  "description": "Updated project description",
                                  "archived": false
                                }
                                """)),
                200,
                "PUT /api/projects/{id} should update the project and return 200 OK."
        );
        assertJsonText(updateResult, "/name", "Spring Course Updated", "Updated project response should show the new name.");
        assertJsonBoolean(updateResult, "/archived", false, "Full project update should preserve archived=false when sent in the request.");

        MvcResult patchResult = expectStatus(mockMvc.perform(patch("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "archived": true
                                }
                                """)),
                200,
                "PATCH /api/projects/{id} should update archived state and return 200 OK."
        );
        assertJsonBoolean(patchResult, "/archived", true, "PATCH /api/projects/{id} should update archived to true.");

        expectStatus(mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Disposable Project",
                                  "description": "Delete me"
                                }
                                """)),
                201,
                "POST /api/projects should allow creating another project for delete testing."
        );

        expectStatus(
                mockMvc.perform(delete("/api/projects/2")),
                204,
                "DELETE /api/projects/{id} should return 204 No Content for an existing project."
        );
    }
}
