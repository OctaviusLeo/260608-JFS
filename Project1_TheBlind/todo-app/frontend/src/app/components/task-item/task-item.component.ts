import { Component, inject, input, linkedSignal, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TaskService } from '../../services/task.service';
import { TaskNode } from '../../models/task.model';

/**
 * TaskItemComponent (Task Management UI Engineer).
 *
 * Renders a single task and, recursively, its subtasks (a standalone component
 * may reference its own selector in its template, so no self import is needed).
 *
 * Simple edits (toggling completion and renaming) update this component in
 * place and save in the background, so the tree never has to reload. Only
 * structural changes (adding a subtask or deleting) emit `refresh` to ask the
 * dashboard to rebuild the tree.
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

  /** Emitted after a structural change (add or delete) so the dashboard reloads the tree. */
  readonly refresh = output<void>();

  private readonly taskService = inject(TaskService);

  // Local copies of the editable fields. They re-sync automatically if the
  // dashboard reloads and passes a fresh task in, but can be updated optimistically.
  protected readonly isComplete = linkedSignal(() => this.task().isComplete);
  protected readonly content = linkedSignal(() => this.task().taskContent);

  protected readonly showAddSubtask = signal(false);
  protected readonly isEditing = signal(false);
  protected newSubtaskContent = '';
  protected editContent = '';

  protected toggleAddSubtask(): void {
    this.showAddSubtask.update((open) => !open);
  }

  /** Toggle completion. Updates the view straight away, then saves in the background. */
  protected onToggleComplete(isComplete: boolean): void {
    const previous = this.isComplete();
    this.isComplete.set(isComplete);

    this.taskService
      .updateTask(this.task().id, {
        // The backend validates content on every update, so send the current text too.
        taskContent: this.content(),
        isComplete,
      })
      .subscribe({
        // Put the checkbox back if the save fails.
        error: () => this.isComplete.set(previous),
        // NOTE (Authentication UI Engineer): surface update or auth (401/403) errors here.
      });
  }

  /** Switch the task content into an editable input. */
  protected startEdit(): void {
    this.editContent = this.content();
    this.isEditing.set(true);
  }

  protected cancelEdit(): void {
    this.isEditing.set(false);
  }

  /** Save a renamed task in place. No tree reload, since the shape is unchanged. */
  protected onRename(): void {
    const next = this.editContent.trim();
    this.isEditing.set(false);

    // Nothing to do when the text is blank or unchanged.
    if (!next || next === this.content()) {
      return;
    }

    const previous = this.content();
    this.content.set(next);

    this.taskService
      .updateTask(this.task().id, { taskContent: next, isComplete: this.isComplete() })
      .subscribe({
        // Restore the old name if the save fails.
        error: () => this.content.set(previous),
        // NOTE (Authentication UI Engineer): surface update or auth errors here.
      });
  }

  /** Create a child task. This changes the tree shape, so ask for a reload. */
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
        // NOTE (Authentication UI Engineer): surface create or auth errors here.
      });
  }

  /** Delete this task; the backend cascades to all descendants. Ask for a reload. */
  protected onDelete(): void {
    this.taskService.deleteTask(this.task().id).subscribe({
      next: () => this.refresh.emit(),
      // NOTE (Authentication UI Engineer): surface delete or auth errors here.
    });
  }
}
