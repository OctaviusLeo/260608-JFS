import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Task, CreateTaskPayload, UpdateTaskPayload } from '../models/task.model';

/**
 * TaskService (Task Management UI Engineer).
 *
 * Wraps the backend task endpoints as RxJS Observables. Authentication headers
 * are intentionally NOT set here: attaching `Authorization: Bearer <token>` is
 * handled globally by the JWT HttpInterceptor (Frontend Infrastructure Lead).
 *
 * NOTE: the running backend (TaskController) maps these routes under
 * `/api/tasks`. If the team aligns the contract to `/api/task`, change
 * `BASE_URL` below - it is the single source of truth for the path.
 *
 * Requires `provideHttpClient(...)` to be registered in app.config.ts
 * (Frontend Infrastructure Lead).
 */
@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  private readonly BASE_URL = '/api/tasks';

  /** GET /api/tasks - fetch all of the current user's tasks (flat list). */
  getTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(this.BASE_URL);
  }

  /** GET /api/tasks/{id} - fetch a single task. */
  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.BASE_URL}/${id}`);
  }

  /** POST /api/tasks - create a task (or a subtask when parent_task_id is set). */
  createTask(payload: CreateTaskPayload): Observable<Task> {
    return this.http.post<Task>(this.BASE_URL, payload);
  }

  /** PATCH /api/tasks/{id} - update content and/or completion state. */
  updateTask(id: string, payload: UpdateTaskPayload): Observable<Task> {
    return this.http.patch<Task>(`${this.BASE_URL}/${id}`, payload);
  }

  /** DELETE /api/tasks/{id} - delete a task; the backend cascades to subtasks. */
  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${id}`);
  }
}
