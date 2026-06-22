import { Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TaskService } from '../../services/task.service';
import { TaskNode } from '../../models/task.model';

/**
 * TaskItemComponent (Task Management UI Engineer).
 *
 * Renders a single task and, recursively, its subtasks (a standalone component
 * may reference its own selector in its template, so no self-import is needed).
 *
 * Each mutation (toggle complete / add subtask / delete) is dispatched through
 * TaskService, then `refresh` is emitted so the dashboard re-loads the tree and
 * re-renders from authoritative server state.
 */
@Component({
  selector: 'app-task-item',
  imports: [FormsModule],
  templateUrl: './task-item.component.html',
  styleUrl: './task-item.component.css',
})
export class TaskItemComponent {
  /** The task (with resolved children) to render. */
  readonly task = input.required<TaskNode>();

  /** Emitted after any successful mutation so an ancestor can reload the tree. */
  readonly refresh = output<void>();

  private readonly taskService = inject(TaskService);

  protected readonly showAddSubtask = signal(false);
  protected newSubtaskContent = '';

  protected toggleAddSubtask(): void {
    this.showAddSubtask.update((open) => !open);
  }

  /** Toggle is_complete and dispatch a PATCH. */
  protected onToggleComplete(isComplete: boolean): void {
    this.taskService
      .updateTask(this.task().id, {
        // Resend the current content: the backend's update validates taskContent,
        // so a PATCH that omits it would be rejected.
        taskContent: this.task().taskContent,
        isComplete,
      })
      .subscribe({
        next: () => this.refresh.emit(),
        // Re-sync the checkbox with the server on failure.
        error: () => this.refresh.emit(),
        // NOTE (Authentication UI Engineer): surface update / auth (401/403) error messaging here.
      });
  }

  /** Create a child task (subtask) under this task. */
  protected onAddSubtask(): void {
    const content = this.newSubtaskContent.trim();
    if (!content) {
      return;
    }
    this.taskService
      .createTask({ taskContent: content, parent_task_id: this.task().id })
      .subscribe({
        next: () => {
          this.newSubtaskContent = '';
          this.showAddSubtask.set(false);
          this.refresh.emit();
        },
        // NOTE (Authentication UI Engineer): surface create / auth error messaging here.
      });
  }

  /** Delete this task; the backend cascades to all descendants. */
  protected onDelete(): void {
    this.taskService.deleteTask(this.task().id).subscribe({
      next: () => this.refresh.emit(),
      // NOTE (Authentication UI Engineer): surface delete / auth error messaging here.
    });
  }
}
