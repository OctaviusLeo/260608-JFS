package com.theblind.todo.Entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "task")
public class task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Integer id;

    @Column(name = "task_content", length = 50)
    private String taskContent;

    // Self-referencing FK: a task can have a parent task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private task parentTask;

    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    private List<task> subtask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private user user;

    @Column(name = "is_complete")
    private Boolean isComplete = false;

    @Column(name = "task_creation", updatable = false)
    private LocalDateTime taskCreation = LocalDateTime.now();
}
