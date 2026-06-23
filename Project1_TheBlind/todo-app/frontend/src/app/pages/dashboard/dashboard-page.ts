import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TaskService } from '../../services/task.service';
import { TaskItemComponent } from '../../components/task-item/task-item.component';
import { Task, TaskNode } from '../../models/task.model';

/**
 * Dashboard (Task Management UI Engineer).
 *
 * Implemented on the existing `DashboardPage` so it stays wired to the route
 * configured by the Frontend Infrastructure Lead. Loads the user's tasks, builds a
 * nested tree in memory, and renders it with the recursive TaskItemComponent.
 */
@Component({
  selector: 'app-dashboard-page',
  imports: [FormsModule, TaskItemComponent],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage implements OnInit {
  private readonly taskService = inject(TaskService);

  protected readonly tasks = signal<TaskNode[]>([]);
  protected readonly loading = signal(false);
  protected newTaskContent = '';

  // Tracks whether the first load has happened, so reloads after add or delete
  // do not blank the list with the loading message.
  private hasLoaded = false;

  ngOnInit(): void {
    this.loadTasks();
  }

  /** Fetch the flat task list and rebuild the rendered tree. */
  protected loadTasks(): void {
    if (!this.hasLoaded) {
      this.loading.set(true);
    }
    this.taskService.getTasks().subscribe({
      next: (flat) => {
        this.tasks.set(this.buildTree(flat));
        this.loading.set(false);
        this.hasLoaded = true;
      },
      error: () => {
        this.loading.set(false);
        this.hasLoaded = true;
        // NOTE (Authentication UI Engineer): handle session/auth errors (401/403) and messaging here.
      },
    });
  }

  /** Create a new top-level task. */
  protected onAddTask(): void {
    const content = this.newTaskContent.trim();
    if (!content) {
      return;
    }
    this.taskService.createTask({ taskContent: content, parent_task_id: null }).subscribe({
      next: () => {
        this.newTaskContent = '';
        this.loadTasks();
      },
      // NOTE (Authentication UI Engineer): surface create / auth error messaging here.
    });
  }

  /**
   * Build a nested tree from the backend's flat list using parent_task_id:
   *  1. index every task by id (with an empty children array)
   *  2. attach each child to its parent; tasks with no/unknown parent are roots
   *  3. return only the roots; children hang off them by reference
   */
  private buildTree(flat: Task[]): TaskNode[] {
    const byId = new Map<string, TaskNode>();
    for (const task of flat) {
      byId.set(task.id, { ...task, children: [] });
    }

    const roots: TaskNode[] = [];
    for (const node of byId.values()) {
      const parent = node.parent_task_id ? byId.get(node.parent_task_id) : undefined;
      if (parent) {
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    }
    return roots;
  }
}
