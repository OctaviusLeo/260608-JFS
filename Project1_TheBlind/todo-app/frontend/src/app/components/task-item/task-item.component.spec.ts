import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { TaskItemComponent } from './task-item.component';
import { TaskNode } from '../../models/task.model';

function node(partial: Partial<TaskNode> & { id: string }): TaskNode {
  return {
    taskContent: 'Task',
    parent_task_id: null,
    isComplete: false,
    children: [],
    ...partial,
  };
}

describe('TaskItemComponent', () => {
  const BASE = '/api/tasks';
  let fixture: ComponentFixture<TaskItemComponent>;
  let component: TaskItemComponent;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskItemComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(TaskItemComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => httpMock.verify());

  it('renders the task content and completion state', async () => {
    fixture.componentRef.setInput(
      'task',
      node({ id: '1', taskContent: 'Buy milk', isComplete: true }),
    );
    await fixture.whenStable();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.task-content')?.textContent).toContain('Buy milk');
    expect(el.querySelector<HTMLInputElement>('.task-checkbox')?.checked).toBe(true);
  });

  it('renders nested subtasks recursively', async () => {
    fixture.componentRef.setInput(
      'task',
      node({
        id: 'p',
        taskContent: 'Parent',
        children: [node({ id: 'c', taskContent: 'Child', parent_task_id: 'p' })],
      }),
    );
    await fixture.whenStable();

    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('app-task-item');
    expect(items.length).toBe(1); // the nested child renders as another app-task-item
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Child');
  });

  it('toggling complete dispatches a PATCH with current content + new state and emits refresh', async () => {
    fixture.componentRef.setInput(
      'task',
      node({ id: 't1', taskContent: 'Walk dog', isComplete: false }),
    );
    await fixture.whenStable();

    let refreshed = false;
    component.refresh.subscribe(() => (refreshed = true));

    const checkbox = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(
      '.task-checkbox',
    )!;
    checkbox.checked = true;
    checkbox.dispatchEvent(new Event('change'));

    const req = httpMock.expectOne(`${BASE}/t1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ taskContent: 'Walk dog', isComplete: true });
    req.flush({ id: 't1', taskContent: 'Walk dog', parent_task_id: null, isComplete: true });

    expect(refreshed).toBe(true);
  });

  it('adding a subtask POSTs with parent_task_id and emits refresh', async () => {
    fixture.componentRef.setInput('task', node({ id: 'parent-9', taskContent: 'Groceries' }));
    await fixture.whenStable();

    let refreshed = false;
    component.refresh.subscribe(() => (refreshed = true));

    (component as any).newSubtaskContent = 'Eggs';
    (component as any).onAddSubtask();

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ taskContent: 'Eggs', parent_task_id: 'parent-9' });
    req.flush({ id: 'sub', taskContent: 'Eggs', parent_task_id: 'parent-9', isComplete: false });

    expect(refreshed).toBe(true);
  });

  it('does not POST when the subtask content is blank', async () => {
    fixture.componentRef.setInput('task', node({ id: 'parent-9' }));
    await fixture.whenStable();

    (component as any).newSubtaskContent = '   ';
    (component as any).onAddSubtask();

    httpMock.expectNone(BASE);
  });

  it('delete dispatches DELETE and emits refresh', async () => {
    fixture.componentRef.setInput('task', node({ id: 'del-1', taskContent: 'Old task' }));
    await fixture.whenStable();

    let refreshed = false;
    component.refresh.subscribe(() => (refreshed = true));

    (component as any).onDelete();

    const req = httpMock.expectOne(`${BASE}/del-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(refreshed).toBe(true);
  });
});
