import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { DashboardPage } from './dashboard-page';
import { Task, TaskNode } from '../../models/task.model';

describe('DashboardPage', () => {
  const BASE = '/api/tasks/current_user';
  let fixture: ComponentFixture<DashboardPage>;
  let component: DashboardPage;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardPage);
    component = fixture.componentInstance;
  });

  afterEach(() => httpMock.verify());

  function flushTasks(tasks: Task[]): void {
    fixture.detectChanges(); // triggers ngOnInit -> loadTasks
    httpMock.expectOne(BASE).flush(tasks);
  }

  it('loads tasks on init and builds a nested tree from the flat list', async () => {
    flushTasks([
      { id: 'root', taskContent: 'Parent', parent_task_id: null, isComplete: false },
      { id: 'child', taskContent: 'Child', parent_task_id: 'root', isComplete: false },
      { id: 'grandchild', taskContent: 'Grandchild', parent_task_id: 'child', isComplete: false },
    ]);

    const tree = (component as any).tasks() as TaskNode[];
    expect(tree.length).toBe(1);
    expect(tree[0].id).toBe('root');
    expect(tree[0].children.length).toBe(1);
    expect(tree[0].children[0].id).toBe('child');
    expect(tree[0].children[0].children[0].id).toBe('grandchild');
  });

  it('shows an empty-state message when there are no tasks', async () => {
    flushTasks([]);
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No tasks yet');
  });

  it('renders a top-level app-task-item per root task', async () => {
    flushTasks([
      { id: 'r1', taskContent: 'One', parent_task_id: null, isComplete: false },
      { id: 'r2', taskContent: 'Two', parent_task_id: null, isComplete: false },
    ]);
    await fixture.whenStable();

    const roots = (fixture.nativeElement as HTMLElement).querySelectorAll(
      'ul.task-tree > app-task-item',
    );
    expect(roots.length).toBe(2);
  });

  it('onAddTask POSTs a root task then reloads the list', async () => {
    flushTasks([]);

    (component as any).newTaskContent = 'Write report';
    (component as any).onAddTask();

    const post = httpMock.expectOne(BASE);
    expect(post.request.method).toBe('POST');
    expect(post.request.body).toEqual({ taskContent: 'Write report', parent_task_id: null });
    post.flush({ id: 'new', taskContent: 'Write report', parent_task_id: null, isComplete: false });

    // reload after a successful create
    httpMock
      .expectOne(BASE)
      .flush([{ id: 'new', taskContent: 'Write report', parent_task_id: null, isComplete: false }]);

    expect((component as any).tasks().length).toBe(1);
    expect((component as any).newTaskContent).toBe('');
  });

  it('does not POST when the new task content is blank', async () => {
    flushTasks([]);

    (component as any).newTaskContent = '   ';
    (component as any).onAddTask();

    httpMock.expectNone(BASE);
  });
});
