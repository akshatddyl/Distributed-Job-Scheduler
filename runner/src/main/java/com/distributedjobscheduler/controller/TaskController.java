package com.distributedjobscheduler.controller;

import com.distributedjobscheduler.dto.DependencyRequest;
import com.distributedjobscheduler.dto.ErrorResponse;
import com.distributedjobscheduler.model.TaskStatus;
import com.distributedjobscheduler.repository.TaskRedisRepository;
import com.distributedjobscheduler.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.distributedjobscheduler.model.Task;
import com.distributedjobscheduler.dto.TaskRequest;
import com.distributedjobscheduler.dto.TaskResponse;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable String id,
            @RequestHeader("X-Tenant-ID") String tenantId) {
        Task task = taskService.getTaskById(tenantId, id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<?> submitTask(@Valid @RequestBody TaskRequest request) {
        try {
            Task task = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new TaskResponse(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("🚫 Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("❌ Internal Error", e.getMessage()));
        }
    }

    @PostMapping("/dependencies")
    public ResponseEntity<?> addDependencies(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody DependencyRequest request
    ) {
        try {
            taskService.addDependenciesByName(tenantId, request.getTaskName(), request.getDependsOn());
            return ResponseEntity.ok(Map.of("message", "✅ Dependencies added successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

}