package lt.techin.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonInt;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonLong;
import static lt.techin.taskmanager.rest.TestResponseAssertions.assertJsonText;
import static lt.techin.taskmanager.rest.TestResponseAssertions.expectStatus;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void globalTaskQueryingFilteringSortingAndPaginationWork() throws Exception {
        createProject("Alpha Project", "First project");
        createProject("Beta Project", "Second project");
        createUser("Alice User", "alice@example.com");
        createUser("Bob User", "bob@example.com");

        createTask(1, """
                {
                  "title": "Alpha first",
                  "description": "Assigned to Alice",
                  "dueDate": "2099-05-01",
                  "assigneeId": 1
                }
                """);
        createTask(1, """
                {
                  "title": "Alpha second",
                  "description": "Assigned to Bob",
                  "dueDate": "2099-05-03",
                  "assigneeId": 2
                }
                """);
        createTask(2, """
                {
                  "title": "Beta first",
                  "description": "Assigned to Alice",
                  "dueDate": "2099-05-02",
                  "assigneeId": 1
                }
                """);
        createTask(2, """
                {
                  "title": "Beta late",
                  "description": "Unassigned task",
                  "dueDate": "2099-05-20"
                }
                """);

        expectStatus(
                mockMvc.perform(patch("/api/tasks/2/status").param("value", "DONE")),
                200,
                "PATCH /api/tasks/{id}/status should still work before task queries are checked."
        );

        MvcResult pagedResult = expectStatus(mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "dueDate,asc")),
                200,
                "GET /api/tasks should return 200 OK with paging metadata."
        );
        assertJsonInt(pagedResult, "/page", 0, "GET /api/tasks should return the requested page number.");
        assertJsonInt(pagedResult, "/size", 2, "GET /api/tasks should return the requested page size.");
        assertJsonLong(pagedResult, "/totalElements", 4L, "GET /api/tasks should count all matching tasks before paging.");
        assertJsonInt(pagedResult, "/totalPages", 2, "GET /api/tasks should calculate totalPages from totalElements and size.");
        assertJsonText(pagedResult, "/content/0/title", "Alpha first", "GET /api/tasks sorted by dueDate ascending should return the earliest task first.");
        assertJsonText(pagedResult, "/content/1/title", "Beta first", "GET /api/tasks sorted by dueDate ascending should return the second earliest task next.");

        MvcResult filteredByProjectAndStatus = expectStatus(mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO")
                        .param("projectId", "1")),
                200,
                "GET /api/tasks should allow combining projectId and status filters."
        );
        assertJsonLong(filteredByProjectAndStatus, "/totalElements", 1L, "Combined projectId and status filters should narrow the result set.");
        assertJsonText(filteredByProjectAndStatus, "/content/0/title", "Alpha first", "Combined projectId and status filters should keep only matching tasks.");

        MvcResult filteredSortedResult = expectStatus(mockMvc.perform(get("/api/tasks")
                        .param("assigneeId", "1")
                        .param("dueBefore", "2099-05-10")
                        .param("sort", "title,desc")),
                200,
                "GET /api/tasks should support assigneeId, dueBefore, and sorting together."
        );
        assertJsonLong(filteredSortedResult, "/totalElements", 2L, "Assignee and dueBefore filters should return only the matching tasks.");
        assertJsonText(filteredSortedResult, "/content/0/title", "Beta first", "Sorting by title descending should place 'Beta first' before 'Alpha first'.");
        assertJsonText(filteredSortedResult, "/content/1/title", "Alpha first", "Sorting by title descending should keep the second matching task in correct order.");
    }

    @Test
    void projectScopedTaskQueryingFiltersWithinProjectOnly() throws Exception {
        createProject("Scoped Project", "Target project");
        createProject("Other Project", "Other project");
        createUser("Alice User", "alice@example.com");
        createUser("Bob User", "bob@example.com");

        createTask(1, """
                {
                  "title": "Scoped todo",
                  "description": "Alice todo",
                  "dueDate": "2099-06-01",
                  "assigneeId": 1
                }
                """);
        createTask(1, """
                {
                  "title": "Scoped done",
                  "description": "Bob done",
                  "dueDate": "2099-06-02",
                  "assigneeId": 2
                }
                """);
        createTask(2, """
                {
                  "title": "Other todo",
                  "description": "Alice in other project",
                  "dueDate": "2099-06-01",
                  "assigneeId": 1
                }
                """);

        expectStatus(
                mockMvc.perform(patch("/api/tasks/2/status").param("value", "DONE")),
                200,
                "PATCH /api/tasks/{id}/status should still allow moving a task to DONE before scoped filtering is checked."
        );

        MvcResult scopedTodoResult = expectStatus(mockMvc.perform(get("/api/projects/1/tasks")
                        .param("status", "TODO")
                        .param("assigneeId", "1")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc")),
                200,
                "GET /api/projects/{id}/tasks should support project-scoped filtering, sorting, and paging."
        );
        assertJsonLong(scopedTodoResult, "/totalElements", 1L, "Project-scoped filters should return only tasks from the selected project.");
        assertJsonText(scopedTodoResult, "/content/0/title", "Scoped todo", "Project-scoped TODO filter should keep only the matching task.");
        assertJsonInt(scopedTodoResult, "/content/0/project/id", 1, "Project-scoped task results should still belong to the requested project.");
        assertJsonInt(scopedTodoResult, "/content/0/assignee/id", 1, "Project-scoped assignee filter should keep only tasks assigned to the requested user.");

        MvcResult scopedDoneResult = expectStatus(
                mockMvc.perform(get("/api/projects/1/tasks").param("status", "DONE")),
                200,
                "GET /api/projects/{id}/tasks should allow filtering by DONE status within one project."
        );
        assertJsonLong(scopedDoneResult, "/totalElements", 1L, "Project-scoped DONE filter should return only DONE tasks in that project.");
        assertJsonText(scopedDoneResult, "/content/0/title", "Scoped done", "Project-scoped DONE filter should keep the completed task.");
    }

    private void createProject(String name, String description) throws Exception {
        expectStatus(mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "%s"
                                }
                                """.formatted(name, description))),
                201,
                "Project setup for TaskApiTest should succeed before query assertions run."
        );
    }

    private void createUser(String name, String email) throws Exception {
        expectStatus(mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s"
                                }
                                """.formatted(name, email))),
                201,
                "User setup for TaskApiTest should succeed before query assertions run."
        );
    }

    private void createTask(long projectId, String body) throws Exception {
        expectStatus(mockMvc.perform(post("/api/projects/%d/tasks".formatted(projectId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                201,
                "Task setup for TaskApiTest should succeed before query assertions run."
        );
    }
}
