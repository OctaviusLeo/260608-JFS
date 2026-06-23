import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { TaskService } from './task.service';
import { Task } from '../models/task.model';

describe('TaskService', () => {
  const BASE = '/api/tasks';
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getTasks() issues GET /api/tasks', () => {
    const mock: Task[] = [
      { id: 'a', taskContent: 'Root', parent_task_id: null, isComplete: false },
    ];
    let result: Task[] | undefined;

    service.getTasks().subscribe((r) => (result = r));

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
    expect(result).toEqual(mock);
  });

  it('getTask(id) issues GET /api/tasks/{id}', () => {
    const mock: Task = { id: 'x', taskContent: 'One', parent_task_id: null, isComplete: true };

    service.getTask('x').subscribe();

    const req = httpMock.expectOne(`${BASE}/x`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('createTask() POSTs the payload to /api/tasks', () => {
    service.createTask({ taskContent: 'New', parent_task_id: null }).subscribe();

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ taskContent: 'New', parent_task_id: null });
    req.flush({ id: 'n', taskContent: 'New', parent_task_id: null, isComplete: false });
  });

  it('createTask() sends parent_task_id when creating a subtask', () => {
    service.createTask({ taskContent: 'Child', parent_task_id: 'parent-1' }).subscribe();

    const req = httpMock.expectOne(BASE);
    expect(req.request.body).toEqual({ taskContent: 'Child', parent_task_id: 'parent-1' });
    req.flush({ id: 'c', taskContent: 'Child', parent_task_id: 'parent-1', isComplete: false });
  });

  it('updateTask() PATCHes /api/tasks/{id} with content + completion', () => {
    service.updateTask('id-7', { taskContent: 'Edited', isComplete: true }).subscribe();

    const req = httpMock.expectOne(`${BASE}/id-7`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ taskContent: 'Edited', isComplete: true });
    req.flush({ id: 'id-7', taskContent: 'Edited', parent_task_id: null, isComplete: true });
  });

  it('deleteTask() issues DELETE /api/tasks/{id}', () => {
    service.deleteTask('id-9').subscribe();

    const req = httpMock.expectOne(`${BASE}/id-9`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
