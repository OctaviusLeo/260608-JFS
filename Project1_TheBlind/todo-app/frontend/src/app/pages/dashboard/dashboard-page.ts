import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TaskService } from '../../services/task.service';
import { TokenStorage } from '../../core/auth/token-storage.service';
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
  private readonly tokenStorage = inject(TokenStorage);
  private readonly router = inject(Router);

  protected readonly tasks = signal<TaskNode[]>([]);
  protected readonly loading = signal(false);
  // Surfaces load/create failures in the UI instead of silently swallowing them.
  protected readonly errorMessage = signal<string | null>(null);
  protected newTaskContent = '';

  // Tracks whether the first load has happened, so reloads after add or delete
  // do not blank the list with the loading message.
  private hasLoaded = false;

  ngOnInit(): void {
    this.loadTasks();
  }

  /**
   * Log out: clear the stored JWT and return to the login page. Once the token
   * is gone, the authGuard on /dashboard will reject any attempt to navigate
   * (or type the URL) back into the dashboard until the user logs in again.
   */
  protected onLogout(): void {
    this.tokenStorage.clear();
    this.router.navigate(['/login']);
  }

  /** Fetch the flat task list and rebuild the rendered tree. */
  protected loadTasks(): void {
    if (!this.hasLoaded) {
      this.loading.set(true);
    }
    this.taskService.getTasksByCurrentUser().subscribe({
      next: (data) => {
        const flat = this.toTaskArray(data);
        this.tasks.set(this.buildTree(flat));
        this.errorMessage.set(null);
        this.loading.set(false);
        this.hasLoaded = true;
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.hasLoaded = true;
        if (!this.redirectIfUnauthorized(err)) {
          this.errorMessage.set('Could not load tasks. Please try again.');
        }
      },
    });
  }

  /**
   * Normalize the tasks response into a flat array. The endpoint should return
   * a JSON array, but we defensively unwrap common shapes (a Spring `Page` with
   * a `content` array, or a single task object) so an unexpected payload can't
   * crash the tree build. Anything else becomes an empty list and is logged.
   */
  private toTaskArray(data: unknown): Task[] {
    if (Array.isArray(data)) {
      return data as Task[];
    }
    if (data && typeof data === 'object') {
      const record = data as Record<string, unknown>;
      if (Array.isArray(record['content'])) {
        return record['content'] as Task[];
      }
      if (typeof record['id'] === 'string') {
        // A single task object — wrap it so it still renders.
        return [data as Task];
      }
    }
    console.warn('Unexpected /api/tasks response shape (expected an array):', data);
    return [];
  }

  /** Create a new top-level task. */
  protected onAddTask(): void {
    const content = this.newTaskContent.trim();
    if (!content) {
      return;
    }
    // Clear synchronously (within the click event) so the input resets right away.
    this.newTaskContent = '';
    this.errorMessage.set(null);
    this.taskService.createTask({ taskContent: content, parent_task_id: null }).subscribe({
      next: () => {
        this.loadTasks();
      },
      error: (err: HttpErrorResponse) => {
        if (!this.redirectIfUnauthorized(err)) {
          this.errorMessage.set('Could not add the task. Please try again.');
          // Put the text back so the user doesn't lose what they typed.
          this.newTaskContent = content;
        }
      },
    });
  }

  /**
   * If the server rejected the request because the session is missing or
   * expired (401/403), clear the stale token and send the user to login.
   * Returns true when a redirect was triggered.
   */
  private redirectIfUnauthorized(err: HttpErrorResponse): boolean {
    if (err.status === 401 || err.status === 403) {
      this.tokenStorage.clear();
      this.router.navigate(['/login']);
      return true;
    }
    return false;
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
