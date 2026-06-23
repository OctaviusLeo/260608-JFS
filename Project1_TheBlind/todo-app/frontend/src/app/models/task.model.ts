/**
 * Task domain models (Task Management UI Engineer).
 *
 * Field names match the JSON actually produced by the Spring Boot backend
 * (Lombok @Data getters -> camelCase), e.g. `taskContent` / `isComplete`,
 * while the self-referencing FK is serialized as `parent_task_id`.
 */

/** A task exactly as it arrives (flat) from the backend. */
export interface Task {
  id: string;
  taskContent: string;
  /** Self-referencing FK. `null` for a top-level (root) task. */
  parent_task_id: string | null;
  isComplete: boolean;
  /** Present in some backend responses; the UI builds its own tree instead. */
  subtask?: Task[];
  taskCreation?: string;
}

/** A task enriched with its resolved children, ready for recursive rendering. */
export interface TaskNode extends Task {
  children: TaskNode[];
}

/** Body for POST /api/tasks. Omit/null `parent_task_id` for a root task. */
export interface CreateTaskPayload {
  taskContent: string;
  parent_task_id?: string | null;
}

/** Body for PATCH /api/tasks/{id}. */
export interface UpdateTaskPayload {
  taskContent?: string;
  isComplete?: boolean;
}
