package com.distributedjobscheduler.consumer;

import com.distributedjobscheduler.metrics.PrometheusMetricsCollector;
import com.distributedjobscheduler.model.Task;
import com.distributedjobscheduler.model.TaskStatus;
import com.distributedjobscheduler.notification.NotificationService;
import com.distributedjobscheduler.repository.TaskRedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TaskExecutor {
    private final TaskRedisRepository taskRedisRepository;

    private final NotificationService notificationService;
    private final PrometheusMetricsCollector metrics;
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

    public TaskExecutor(TaskRedisRepository taskRedisRepository, NotificationService notificationService, PrometheusMetricsCollector metrics) {
        this.taskRedisRepository = taskRedisRepository;
        this.notificationService = notificationService;
        this.metrics = metrics;
    }

    public void processTask(Task task) throws Exception { // let retry handler do the retry logic
        long start = System.currentTimeMillis(); // ⏱️ Track start time


        String taskId = task.getId();
        if (taskId == null && task.getName() != null) {
            String resolved = taskRedisRepository.getTaskIdByName(task.getTenantId(), task.getName());
            if (resolved != null) {
                taskId = resolved;
            }
        }
        List<String> deps = (taskId != null)
                ? taskRedisRepository.getDependencies(task.getTenantId(), taskId)
                : List.of();
        logger.info("Task ID: {}, Name: {}, Dependencies: {}", taskId, task.getName(), deps);

        String taskType = deps == null || deps.isEmpty() ? "independent" : "dag";

        try {
            task.setStatus(TaskStatus.RUNNING);
            simulateTaskExecution(task);

            task.setStatus(TaskStatus.COMPLETED);
            metrics.recordSuccess(); // ✅ Prometheus counter

        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED); // Let RetryHandler decide if DLQ is needed
            metrics.recordFailure(); // ✅ Prometheus counter
            throw e; // Let RetryHandler handle it
        } finally {
            long duration = System.currentTimeMillis() - start;
            metrics.recordExecutionTime(taskType, duration); // ⏱️ tagged with "independent" or "dag"
            notificationService.notify(task); // notify success/failure
        }
    }

    private void simulateTaskExecution(Task task) throws Exception {


        // DLQ test snippet start
        if (task.getPayload().containsKey("fail") && Boolean.TRUE.equals(task.getPayload().get("fail"))) {
            throw new RuntimeException("Forced failure for DLQ test");
        }



        System.out.println("✅ Simulated execution for task: " + task.getId());
        Thread.sleep(1000);

    }

    private void requeueTask(Task task) {
        // TODO: Implement Kafka/RabbitMQ producer logic
    }

    private void updateTaskInRedis(Task task) {
        // TODO: Implement Redis logic using Jedis or Redisson
    }

    private void log(Task task, String message) {
        task.getExecutionLogs().add(message);
        logger.info("[{}] {}", task.getId(), message);
    }

    // Complete flow of retry mechanism

    // You're almost right, but let me correct and clarify the full flow and the
    // **role of `delaySeconds`**:
    //
    // ---
    //
    // ### ✅ **Full Retry Flow (with Delay):**
    //
    // 1. **Initial Attempt:**
    //
    // * You submit a task → goes into Redis ZSET with `delaySeconds`.
    // * Scheduler picks it when time has passed → calls `processTask()`.
    // * Inside `simulateTaskExecution()`, task fails (e.g., DB connection error).
    //
    // 2. **On Failure:**
    //
    // * `retryCount++`
    // * If `retryCount < maxRetries`, set `status = PENDING`
    // * Requeue it again into Redis ZSET **with delay (e.g., 10s)**.
    //
    // 3. **Next Retry:**
    //
    // * After delay passes, scheduler again picks it → runs `processTask()` → fails
    // again → repeat.
    //
    // 4. **Final Retry (retryCount == maxRetries):**
    //
    // * On failure, task is **marked as `FAILED`**.
    // * Notification is sent.
    // * Task is **not queued again**.
    //
    // ---
    //
    // ### 🔁 **Where You’re Slightly Off:**
    //
    // * At final retry, status is set to `FAILED`, **not `PENDING`**.
    // * `PENDING` is only for retry attempts before the final one.
    //
    // ---
    //
    // ### 🕒 **Why `delaySeconds` is Important:**
    //
    // * To **throttle retries** (not retry immediately).
    // * Helps **avoid hammering a failing system** (e.g., DB down).
    // * Allows time to recover between retries.
    //
    // This is especially useful in **transient failures**.
    //
    // ---
    //
    // ### ✅ So, Final Behavior:
    //
    // * Retry 3 times → delay in between → then `FAILED`.
    // * Each time, task re-enters ZSET with the new delay → picked again →
    // processed by `processTask()`.
    //
    // Let me know if you want a visual DAG or sequence chart.

}