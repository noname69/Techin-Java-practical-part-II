package lt.techin.taskmanager.repository;

import jakarta.persistence.criteria.Predicate;
import lt.techin.taskmanager.model.Task;
import lt.techin.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecifications {

    public static Specification<Task> withFilters(

            Long projectId,
            TaskStatus status,
            Long assigneeId,
            LocalDate dueBefore
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }

            if (dueBefore != null) {
                predicates.add(cb.lessThan(root.get("dueDate"), dueBefore));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}