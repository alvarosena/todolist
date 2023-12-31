package br.com.alvarosena.todolist.task;

import br.com.alvarosena.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request, HttpServletResponse response) {
        var userId = request.getAttribute("user_id");

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início ou a data de término deve ser maior que a data atual");
        }

        if (taskModel.getStartAt().isBefore(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de inicio deve ser menor que a data de término");
        }

        taskModel.setUserId((UUID) userId);
        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("")
    public List<TaskModel> list(HttpServletRequest request) {
        var userId = request.getAttribute("user_id");

        var tasks = this.taskRepository.findByUserId((UUID) userId);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var userId = request.getAttribute("user_id");
        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task not found");
        }

        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User does not have permission to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var updatedTask = this.taskRepository.save(task);

        return ResponseEntity.ok().body(updatedTask);
    }
}
