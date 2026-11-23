# WORKTRACK BACKEND — LIVING HANDOFF DOCUMENT
> **Last updated:** 2026-11-22T23:25:30+05:30
> **Current Phase:** Phase 8 — API Readiness & OpenAPI Specification (READY TO IMPLEMENT)
> **Rule:** This file is updated BEFORE every code change. Steps marked 🟡 are in progress. Steps marked ✅ are physically written to disk. Steps marked ⬜ are not yet written.

---

## ⚡ HOW TO RESUME IN A NEW SESSION
1. Read this entire file first — top to bottom.
2. Find the first ⬜ or 🟡 step in the current phase plan.
3. Update this file to mark the step 🟡 before writing code.
4. Implement the file on disk, then mark it ✅.
5. All code is on disk at: `c:\Users\Admin\IdeaProjects\Worktrack_backend_backup\`
6. Requirements doc: `WorkTrack_Claude_Code_V1_Master_Prompt.md`
7. Last confirmed build: **103 tests, 0 failures — BUILD SUCCESS** (end of Phase 7)

---

## OVERALL PROGRESS

| Phase | Status | Build | Notes |
|-------|--------|-------|-------|
| Phase 0 — Audit | ✅ DONE | N/A | Full codebase audit complete |
| Phase 1 — Fix Foundation | ✅ DONE | 21/21 ✅ | All defects fixed, Flyway added, CORS added, changeRole + HOD endpoints added |
| Phase 2 — Projects | ✅ DONE | 36/36 ✅ | Entity, Repo, DTOs, Service, Controller, Migration V2, 15 Unit Tests |
| Phase 3 — Membership/Permissions | ✅ DONE | 55/55 ✅ | ProjectMember, Role & Permission enums, ProjectPermissionService, Controller, Migration V3, 19 Unit Tests |
| Phase 4 — WorkItems/Backlog | ✅ DONE | 71/71 ✅ | WorkItem hierarchy, status workflow, Backlog & assignment APIs, Migration V4, 16 Unit Tests |
| Phase 5 — Sprints | ✅ DONE | 85/85 ✅ | Sprint entity, 1-active-sprint rule, complete/cancel rollover, Migration V5, 14 Unit Tests |
| Phase 6 — Collaboration | ✅ DONE | 96/96 ✅ | Event-driven ActivityLog, WorkItemComment soft-delete, InAppNotification, Migration V6, 11 Unit Tests |
| Phase 7 — Dashboards | ✅ DONE | 103/103 ✅ | Role-scoped aggregated metrics (SuperAdmin, Dept, Staff, Project metrics), DB projections, 7 Unit Tests |
| Phase 8 — API Handoff/OpenAPI | ⬜ PLANNED | Pending | OpenAPI docs, final security review & integration validation |

---

## WHAT EXISTS ON DISK RIGHT NOW (post Phase 6)

### All Java source files confirmed on disk:
**auth/**:
- `Action.java`
- `AuthenticatedEmployeeDetails.java`
- `AuthorizableResource.java`
- `AuthorizationService.java`
- `CurrentEmployee.java`
- `EmployeeLifecyclePolicies.java`
- `JWTAuthFilter.java`
- `JWTService.java`
- `RegistrationApprovalContext.java`

**config/**:
- Config classes (if any)

**controller/**:
- `ActivityLogController.java` *(NEW P6)*
- `ApplicationStateController.java`
- `CommentController.java` *(NEW P6)*
- `DepartmentAdminController.java` *(NEW P1)*
- `DepartmentController.java`
- `EmployeeLifecycleController.java` *(MODIFIED P1)*
- `Login.java`
- `NotificationController.java` *(NEW P6)*
- `ProjectController.java` *(NEW P2)*
- `ProjectMemberController.java` *(NEW P3)*
- `RegistrationController.java`
- `SprintController.java` *(NEW P5)*
- `WorkItemController.java` *(NEW P4)*
- `WorkspaceController.java`

**dto/**:
- `ActivityLogResponseDTO.java` *(NEW P6)*
- `ActiveDepartmentsWorkspaceDTO.java`
- `ActiveWorkspaceResponseDTO.java`
- `AddProjectMemberRequestDTO.java` *(NEW P3)*
- `AddWorkItemsToSprintRequestDTO.java` *(NEW P5)*
- `ApprovalRequestDTO.java`
- `AssignDeptHeadRequestDTO.java` *(NEW P1)*
- `AssignWorkItemRequestDTO.java` *(NEW P4)*
- `ChangePasswordRequestDTO.java`
- `ChangeRoleRequestDTO.java` *(NEW P1)*
- `ChangeWorkItemStatusRequestDTO.java` *(NEW P4)*
- `CommentResponseDTO.java` *(NEW P6)*
- `CompleteSprintRequestDTO.java` *(NEW P5)*
- `CreateCommentRequestDTO.java` *(NEW P6)*
- `CreateProjectRequestDTO.java` *(NEW P2)*
- `CreateSprintRequestDTO.java` *(NEW P5)*
- `CreateWorkItemRequestDTO.java` *(NEW P4)*
- `DepartmentResponseDTO.java` *(NEW P1)*
- `EmployeeCreationDTO.java`
- `InAppNotificationResponseDTO.java` *(NEW P6)*
- `LoginRequestDTO.java`
- `LoginResponseDTO.java`
- `ProjectMemberResponseDTO.java` *(NEW P3)*
- `ProjectResponseDTO.java` *(NEW P2)*
- `ProjectStatusUpdateDTO.java` *(NEW P2)*
- `RegistrationDTO.java`
- `RegistrationResponseDTO.java`
- `RegistrationTrackingResponseDTO.java`
- `SprintResponseDTO.java` *(NEW P5)*
- `UpdateCommentRequestDTO.java` *(NEW P6)*
- `UpdateMemberRoleRequestDTO.java` *(NEW P3)*
- `UpdateProjectRequestDTO.java` *(NEW P2)*
- `UpdateSprintRequestDTO.java` *(NEW P5)*
- `UpdateWorkItemRequestDTO.java` *(NEW P4)*
- `WorkItemResponseDTO.java` *(NEW P4)*

**entity/**:
- `ActivityLog.java` *(NEW P6)*
- `ApproveRegistrationDTO.java`
- `Department.java`
- `DepartmentWorkspace.java`
- `Employee.java`
- `EmployeeActionDTO.java`
- `EmpWorkspace.java`
- `EmpWorkspaceId.java`
- `InAppNotification.java` *(NEW P6)*
- `Login.java`
- `PermanentSuperAdmin.java`
- `Project.java` *(NEW P2)*
- `ProjectMember.java` *(NEW P3)*
- `ProjectMemberRole.java` *(NEW P3)*
- `ProjectPermission.java` *(NEW P3)*
- `Registration.java`
- `Sprint.java` *(NEW P5)*
- `SprintState.java` *(NEW P5)*
- `WorkItem.java` *(NEW P4)*
- `WorkItemComment.java` *(NEW P6)*
- `WorkItemPriority.java` *(NEW P4)*
- `WorkItemStatus.java` *(NEW P4)*
- `WorkItemType.java` *(NEW P4)*
- `Workspace.java`

**event/**:
- `ProjectActivityEvent.java` *(NEW P6 — Domain Event)*

**exceptionhandler/**:
- `AccountDeactivated.java`
- `ActiveSprintExistsException.java` *(NEW P5)*
- `ApiError.java`
- `CommentNotFoundException.java` *(NEW P6)*
- `DepartmentCapacityExceeded.java`
- `DepartmentHeadInEligible.java`
- `DepartmentHeadNotDefined.java`
- `DepartmentIsInactiveException.java`
- `DepartmentNotFoundException.java`
- `DuplicateMembershipException.java` *(NEW P3)*
- `DuplicateProjectKeyException.java` *(NEW P2)*
- `DuplicateRegistrationException.java`
- `EmployeeExpiredException.java`
- `EmployeeNotFoundException.java`
- `GlobalExceptionHandler.java` *(MODIFIED P1, P2, P3, P4, P5, P6)*
- `IncorrectPassword.java`
- `InvalidHierarchyException.java` *(NEW P4)*
- `InvalidProjectStatusTransitionException.java` *(NEW P2)*
- `InvalidRegistrationStateException.java`
- `InvalidRoleException.java`
- `InvalidSprintStateTransitionException.java` *(NEW P5)*
- `InvalidWorkItemStatusTransitionException.java` *(NEW P4)*
- `LoginNotFound.java`
- `ProjectMemberNotFoundException.java` *(NEW P3)*
- `ProjectNotFoundException.java` *(NEW P2)*
- `RegistrationNumberNotFoundException.java`
- `SprintNotFoundException.java` *(NEW P5)*
- `WorkItemNotFoundException.java` *(NEW P4)*
- `WorkspaceIsInactiveException.java`
- `WorkspaceNotFound.java`

**repository/**:
- `ActivityLogRepo.java` *(NEW P6)*
- `DepartmentRepo.java`
- `EmployeeRepo.java`
- `EmployeeWorkspaceRepo.java`
- `InAppNotificationRepo.java` *(NEW P6)*
- `LoginRepo.java`
- `PermanentSuperAdminRepo.java`
- `ProjectMemberRepo.java` *(NEW P3)*
- `ProjectRepo.java` *(NEW P2)*
- `RegistrationRepo.java`
- `SprintRepo.java` *(NEW P5)*
- `WorkItemCommentRepo.java` *(NEW P6)*
- `WorkItemRepo.java` *(NEW P4)*
- `WorkspaceRepo.java`

**security/**:
- `SpringSecurity.java` *(MODIFIED P1 — CORS added, bad import removed)*
- `TemporaryPasswordGenerator.java`

**service/**:
- `ActivityLogService.java` *(NEW P6)*
- `ApplicationStateService.java`
- `CommentService.java` *(NEW P6)*
- `DepartmentAdminService.java` *(NEW P1)*
- `DepartmentService.java`
- `EmployeeLifecycleService.java` *(MODIFIED P1 — changeRole + save fix)*
- `LoginService.java`
- `NotificationService.java` *(NEW P6)*
- `ProjectMemberService.java` *(NEW P3)*
- `ProjectPermissionService.java` *(NEW P3)*
- `ProjectService.java` *(NEW P2, MODIFIED P3)*
- `RegistrationService.java`
- `SprintService.java` *(NEW P5)*
- `WorkItemService.java` *(NEW P4)*
- `WorkspaceService.java`

**resources/**:
- `application.properties` *(MODIFIED P1 — CORS + Flyway)*
- `db/migration/V1__initial_schema.sql` *(NEW P1)*
- `db/migration/V2__add_project_table.sql` *(NEW P2)*
- `db/migration/V3__add_project_member_table.sql` *(NEW P3)*
- `db/migration/V4__add_work_item_table.sql` *(NEW P4)*
- `db/migration/V5__add_sprint_table.sql` *(NEW P5)*
- `db/migration/V6__add_collaboration_tables.sql` *(NEW P6)*

**test/**:
- `collaboration/CollaborationServiceTest.java` *(NEW P6 — 11 unit tests)*
- `EmployeeLifeCyclePoliciesTest.java` *(MODIFIED P1 — 4 tests fixed)*
- `WorktrackBackendApplicationTests.java`
- `IsPrimeNumberTest.java`
- `project/ProjectMemberServiceTest.java` *(NEW P3 — 13 unit tests)*
- `project/ProjectPermissionServiceTest.java` *(NEW P3 — 6 unit tests)*
- `project/ProjectServiceTest.java` *(NEW P2 — 15 unit tests)*
- `sprint/SprintServiceTest.java` *(NEW P5 — 14 unit tests)*
- `workitem/WorkItemServiceTest.java` *(NEW P4 — 16 unit tests)*
- `test/resources/application.properties` *(MODIFIED P1 — Flyway disabled in test)*

---

## PHASE 1 & 2 SUMMARY (COMPLETED ✅)

### Phase 1 Live Endpoints:
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/employee-action/change-role/{empId}` | JWT | Body: `{"requestedRole":"Admin"}` |
| PUT | `/admin/workspaces/{wId}/departments/{deptId}/head` | JWT | Assign HOD — Super-Admin only |
| DELETE | `/admin/workspaces/{wId}/departments/{deptId}/head` | JWT | Clear HOD — Super-Admin only |

### Phase 2 Live Endpoints:
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/projects` | JWT | Create project — Admin(scoped) or Super-Admin |
| GET | `/projects/{projectId}` | JWT | Get one project |
| GET | `/workspaces/{wId}/departments/{deptId}/projects` | JWT | List by dept (paginated) |
| GET | `/workspaces/{wId}/projects` | JWT | List all in workspace — Super-Admin only |
| PUT | `/projects/{projectId}` | JWT | Update metadata |
| PATCH | `/projects/{projectId}/status` | JWT | Status transition (DRAFT->ACTIVE->COMPLETED/CANCELLED) |

---

## PHASE 3 — PROJECT MEMBERSHIP & PERMISSION ENGINE (NEXT)

### Business Rules (Spec Section 11)
- Do not rely solely on global Employee roles for project access; introduce explicit Project Membership.
- ProjectMember relationship attributes:
  - `project` (ManyToOne to Project)
  - `employee` (ManyToOne to Employee numeric PK)
  - `role`: Project-scoped role (`OWNER`, `MANAGER`, `CONTRIBUTOR`, `VIEWER`)
  - `status`: `ACTIVE`, `REMOVED` (soft delete to preserve audit history)
  - `addedByEmpId`: String (business empId of the actor)
  - `createdAt`, `updatedAt`: Timestamps
- Uniqueness constraint: `(project_id, employee_id)` to prevent duplicate active memberships.
- Eligibility: Only active employees belonging to the **same workspace and department** as the project can be added.
- Permission Matrix:
  - **OWNER**: Full control (view, edit metadata, manage members, work items, sprints, reports, delete/archive).
  - **MANAGER**: Manage members, create/update all work items, sprints, view reports, comment.
  - **CONTRIBUTOR**: View project, create work items, update own work items, change status, comment.
  - **VIEWER**: View project, view work items, comment.
- Centralized Project Permission Evaluation:
  - Super-Admin: Bypasses project-level membership checks.
  - Admin: Scoped by authorized department; has managerial authority within that department.
  - Staff: MUST have an active `ProjectMember` record granting the specific required permission.
  - Staff permissions NEVER elevate to global Admin/Super-Admin actions.

### Phase 3 Step Tracker (COMPLETED ✅ — Build: 55/55 Tests Passing)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 3.1 | `ProjectMemberRole` enum | `registration/entity/ProjectMemberRole.java` | ✅ Done |
| 3.2 | `ProjectPermission` enum | `registration/entity/ProjectPermission.java` | ✅ Done |
| 3.3 | `ProjectMember` entity | `registration/entity/ProjectMember.java` | ✅ Done |
| 3.4 | `ProjectMemberRepo` repository | `registration/repository/ProjectMemberRepo.java` | ✅ Done |
| 3.5 | `ProjectPermissionService` engine | `registration/service/ProjectPermissionService.java` | ✅ Done |
| 3.6 | DTOs (AddMember, UpdateRole, Response) | `registration/dto/ProjectMemberDTOs...` | ✅ Done |
| 3.7 | Exceptions (`DuplicateMembershipException`, etc.) | `registration/exceptionhandler/...` | ✅ Done |
| 3.8 | `ProjectMemberService` implementation | `registration/service/ProjectMemberService.java` | ✅ Done |
| 3.9 | `ProjectMemberController` REST APIs | `registration/controller/ProjectMemberController.java` | ✅ Done |
| 3.10 | Update `ProjectService` to enforce permissions | `registration/service/ProjectService.java` | ✅ Done |
| 3.11 | Flyway migration `V3__add_project_member_table.sql` | `src/main/resources/db/migration/V3...` | ✅ Done |
| 3.12 | `ProjectMemberServiceTest` unit tests & build verify | `src/test/java/.../ProjectMemberServiceTest.java` | ✅ Done (55/55 Tests Passing) |

### Endpoints live after Phase 3:
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/projects/{projectId}/members` | JWT | Add member to project |
| GET | `/projects/{projectId}/members` | JWT | List project members (paginated) |
| GET | `/projects/{projectId}/members/me` | JWT | Get current user's membership in project |
| PUT | `/projects/{projectId}/members/{memberId}/role` | JWT | Update member project role |
| DELETE | `/projects/{projectId}/members/{memberId}` | JWT | Soft remove member from project |

---

## PHASE 4 — WORK MANAGEMENT / WORKITEMS & BACKLOG (NEXT)

### Business Rules (Spec Section 12)
- Generic hierarchical WorkItem entity covering:
  - `MISSION` (Level 1 - top level)
  - `CAPABILITY` (Level 2 - under Mission)
  - `USER_OUTCOME` (Level 3 - under Capability)
  - `WORK_ITEM` (Level 4 - standard story/task under User Outcome)
  - `ACTION_STEP` (Level 5 - granular sub-task under Work Item)
  - Standalone/cross-cutting types: `DEFECT`, `DISCOVERY`, `ENHANCEMENT`, `CHECKPOINT`
- Parent-child hierarchy constraints strictly validated on create/update.
- Status workflow:
  - `BACKLOG` → `READY` → `IN_PROGRESS` → `IN_REVIEW` → `BLOCKED` → `COMPLETED`
  - Any non-terminal state → `CANCELLED`
  - Valid transitions strictly enforced.
- Priority: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.
- Fields: id, project (FK), itemKey (e.g. "PROJ-101"), title, description, type, status, priority, estimationPoints, parent (nullable FK self-reference), assignee (nullable FK to Employee), reporterEmpId, sprint (nullable FK), tags, createdAt, updatedAt.
- Authorization:
  - Creating/updating work items requires appropriate project permission (`CREATE_WORK_ITEMS`, `UPDATE_OWN_WORK_ITEMS`, `UPDATE_ANY_WORK_ITEMS`, `ASSIGN_WORK_ITEMS`, `CHANGE_WORK_ITEM_STATUS`).
  - Assignee must belong to same workspace and department.
- Backlog endpoint: filter by project, sprint (e.g. unassigned to active sprint = backlog), status, type, assignee, priority with pagination.

### Phase 4 Step Tracker (HANDOFF updates this BEFORE each step's code is written)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 4.1 | `WorkItemType` enum | `registration/entity/WorkItemType.java` | ✅ Done |
| 4.2 | `WorkItemStatus` enum | `registration/entity/WorkItemStatus.java` | ✅ Done |
| 4.3 | `WorkItemPriority` enum | `registration/entity/WorkItemPriority.java` | ✅ Done |
| 4.4 | `WorkItem` entity | `registration/entity/WorkItem.java` | ✅ Done |
| 4.5 | `WorkItemRepo` repository | `registration/repository/WorkItemRepo.java` | ✅ Done |
| 4.6 | DTOs (Create, Update, StatusChange, Assign, Response) | `registration/dto/WorkItemDTOs...` | ✅ Done |
| 4.7 | Exceptions (`WorkItemNotFoundException`, `InvalidHierarchyException`, etc.) | `registration/exceptionhandler/...` | ✅ Done |
| 4.8 | `WorkItemService` implementation | `registration/service/WorkItemService.java` | ✅ Done |
| 4.9 | `WorkItemController` REST APIs | `registration/controller/WorkItemController.java` | ✅ Done |
| 4.10 | Flyway migration `V4__add_work_item_table.sql` | `src/main/resources/db/migration/V4...` | ✅ Done |
| 4.11 | `WorkItemServiceTest` unit tests & build verify | `src/test/java/.../WorkItemServiceTest.java` | ✅ Done (71/71 Tests Passing) |

### Endpoints live after Phase 4:
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/projects/{projectId}/work-items` | JWT | Create work item (with hierarchy check) |
| GET | `/projects/{projectId}/work-items/{itemId}` | JWT | Get one work item |
| GET | `/projects/{projectId}/work-items` | JWT | List/filter work items (paginated) |
| GET | `/projects/{projectId}/backlog` | JWT | List backlog items (unassigned to sprint) |
| GET | `/projects/{projectId}/work-items/{itemId}/children` | JWT | Get child work items |
| PUT | `/projects/{projectId}/work-items/{itemId}` | JWT | Update work item metadata |
| PATCH | `/projects/{projectId}/work-items/{itemId}/status` | JWT | Validated workflow status transition |
| PATCH | `/projects/{projectId}/work-items/{itemId}/assignee` | JWT | Assign/unassign employee |

---

## PHASE 5 — SPRINT MANAGEMENT (NEXT)

### Business Rules (Spec Section 14)
- Each Project owns its backlog and sprints.
- Sprint entity fields:
  - `id` (Long, auto PK)
  - `project` (ManyToOne to Project)
  - `name` (String, required)
  - `goal` (String, text, optional)
  - `startDate`, `endDate` (LocalDate, required)
  - `state`: `PLANNED`, `ACTIVE`, `COMPLETED`, `CANCELLED`
  - `createdByEmpId` (String, business empId)
  - `createdAt`, `updatedAt` (LocalDateTime)
- Invariants:
  - Exactly **ONE ACTIVE sprint per project** at any time.
  - Sprints can only be started (`PLANNED` → `ACTIVE`) if no other sprint is currently `ACTIVE` for that project.
  - Sprint completion (`ACTIVE` → `COMPLETED`):
    - Items completed remain in the sprint.
    - Unfinished items are handled according to user action: moved back to `BACKLOG` (sprintId set to null) or transferred to a target planned sprint.
- Authorization:
  - Actor must have `MANAGE_SPRINTS` permission on the project to create, start, complete, or cancel sprints.
  - Anyone with `VIEW_PROJECT` can view sprints.

### Phase 5 Step Tracker (HANDOFF updates this BEFORE each step's code is written)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 5.1 | `SprintState` enum | `registration/entity/SprintState.java` | ✅ Done |
| 5.2 | `Sprint` entity | `registration/entity/Sprint.java` | ✅ Done |
| 5.3 | `SprintRepo` repository | `registration/repository/SprintRepo.java` | ✅ Done |
| 5.4 | DTOs (Create, Update, Complete, Response) | `registration/dto/SprintDTOs...` | ✅ Done |
| 5.5 | Exceptions (`SprintNotFoundException`, `ActiveSprintExistsException`, etc.) | `registration/exceptionhandler/...` | ✅ Done |
| 5.6 | `SprintService` implementation | `registration/service/SprintService.java` | ✅ Done |
| 5.7 | `SprintController` REST APIs | `registration/controller/SprintController.java` | ✅ Done |
| 5.8 | Flyway migration `V5__add_sprint_table.sql` | `src/main/resources/db/migration/V5...` | ✅ Done |
| 5.9 | `SprintServiceTest` unit tests & build verify | `src/test/java/.../SprintServiceTest.java` | ✅ Done (85/85 Tests Passing) |

### Endpoints live after Phase 5:
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/projects/{projectId}/sprints` | JWT | Create sprint (PLANNED) |
| GET | `/projects/{projectId}/sprints/{sprintId}` | JWT | Get sprint details |
| GET | `/projects/{projectId}/sprints/active` | JWT | Get current active sprint |
| GET | `/projects/{projectId}/sprints` | JWT | List sprints by state (paginated) |
| PUT | `/projects/{projectId}/sprints/{sprintId}` | JWT | Update sprint name, goal, dates |
| POST | `/projects/{projectId}/sprints/{sprintId}/start` | JWT | Start sprint (enforces 1-active-sprint) |
| POST | `/projects/{projectId}/sprints/{sprintId}/complete` | JWT | Complete sprint & roll over unfinished items |
| POST | `/projects/{projectId}/sprints/{sprintId}/cancel` | JWT | Cancel sprint & return items to backlog |
| GET | `/projects/{projectId}/sprints/{sprintId}/work-items` | JWT | Get all work items in sprint |
| POST | `/projects/{projectId}/sprints/{sprintId}/work-items` | JWT | Add work items to sprint |
| DELETE | `/projects/{projectId}/sprints/{sprintId}/work-items/{itemId}` | JWT | Remove work item from sprint |

---

## PHASE 6 — COLLABORATION: COMMENTS & ACTIVITY AUDIT (NEXT)

### Business Rules (Spec Section 15 & 16)
- **Work-Item Comments**:
  - Entity `WorkItemComment`:
    - `id` (Long, auto PK)
    - `workItem` (ManyToOne to WorkItem)
    - `author` (ManyToOne to Employee)
    - `content` (TEXT, required)
    - `isEdited` (boolean, default false)
    - `isDeleted` (boolean, default false - soft delete to preserve thread continuity)
    - `createdAt`, `updatedAt` (LocalDateTime)
  - Rules:
    - Author of comment can edit their own comment.
    - Author or project admin/manager can soft-delete comment.
    - Anyone with `VIEW_PROJECT` can read comments on accessible work items.
    - Anyone with `COMMENT` permission can post comments.
- **Activity / Change History Audit Log**:
  - Entity `ActivityLog`:
    - `id` (Long, auto PK)
    - `projectId` (Long, required for fast project scoping)
    - `entityType` (String, e.g. "PROJECT", "WORK_ITEM", "SPRINT", "PROJECT_MEMBER")
    - `entityId` (Long, ID of affected entity)
    - `action` (String, e.g. "CREATE", "UPDATE", "STATUS_CHANGE", "ASSIGN", "MEMBER_ADD", "SPRINT_START", "SPRINT_COMPLETE")
    - `actorEmpId` (String, business empId of the actor)
    - `description` (String, human-readable summary of the change)
    - `oldValue` (TEXT, optional JSON/string representation of previous state)
    - `newValue` (TEXT, optional JSON/string representation of new state)
    - `timestamp` (LocalDateTime)
  - Rules:
    - Every important action across the system generates an immutable audit record.
    - Activity logs are append-only.
    - Visible to project members with `VIEW_PROJECT`.
- **In-App Notification Foundation**:
  - Entity `InAppNotification`:
    - `id` (Long, auto PK)
    - `recipient` (ManyToOne to Employee)
    - `title` (String)
    - `message` (String)
    - `link` (String, relative resource path e.g. "/projects/1/work-items/5")
    - `isRead` (boolean, default false)
    - `createdAt` (LocalDateTime)
  - Endpoints to fetch unread notifications, mark notification as read, mark all as read.

### Phase 6 Step Tracker (HANDOFF updates this BEFORE each step's code is written)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 6.1 | `WorkItemComment` entity | `registration/entity/WorkItemComment.java` | ✅ Done |
| 6.2 | `ActivityLog` entity | `registration/entity/ActivityLog.java` | ✅ Done |
| 6.3 | `InAppNotification` entity | `registration/entity/InAppNotification.java` | ✅ Done |
| 6.4 | Repositories (Comment, Activity, Notification) | `registration/repository/...` | ✅ Done |
| 6.5 | DTOs (Comment, Activity, Notification) | `registration/dto/...` | ✅ Done |
| 6.6 | Services (`CommentService`, `ActivityLogService`, `NotificationService`) | `registration/service/...` | ✅ Done |
| 6.7 | Controllers (`CommentController`, `ActivityLogController`, `NotificationController`) | `registration/controller/...` | ✅ Done |
| 6.8 | Flyway migration `V6__add_collaboration_tables.sql` | `src/main/resources/db/migration/V6...` | ✅ Done |
| 6.9 | Unit tests & build verify | `src/test/java/.../CollaborationServiceTest.java` | ✅ Done (96/96 Tests Passing) |

### Endpoints live after Phase 6:
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/projects/{projectId}/work-items/{itemId}/comments` | JWT | Add comment (fires activity & notification) |
| GET | `/projects/{projectId}/work-items/{itemId}/comments` | JWT | List work-item comments (paginated) |
| PUT | `/projects/{projectId}/work-items/{itemId}/comments/{commentId}` | JWT | Edit own comment |
| DELETE | `/projects/{projectId}/work-items/{itemId}/comments/{commentId}` | JWT | Soft delete comment |
| GET | `/projects/{projectId}/activity` | JWT | Get project activity audit log |
| GET | `/projects/{projectId}/activity/{entityType}/{entityId}` | JWT | Get entity-specific audit trail |
| GET | `/notifications` | JWT | Get employee notifications (all/unread) |
| GET | `/notifications/unread-count` | JWT | Get unread notifications badge count |
| PATCH | `/notifications/{id}/read` | JWT | Mark notification as read |
| PATCH | `/notifications/read-all` | JWT | Mark all notifications as read |

---

## PHASE 7 — DASHBOARDS & SCOPED ANALYTICS (NEXT)

### Business Rules (Spec Section 16)
- **Super-Admin Dashboard (`GET /dashboards/super-admin`)**:
  - Global overview across workspaces and departments.
  - Employee counts by status (Active, Suspended, Expired, Terminated).
  - Global project counts by status (DRAFT, ACTIVE, COMPLETED, CANCELLED).
  - Global sprint metrics (active vs planned).
  - Cross-workspace blocked and overdue work counts.
  - Access: Super-Admin only.
- **Admin/HOD Dashboard (`GET /dashboards/department/{workspaceId}/{deptId}`)**:
  - Department employee count and HOD details.
  - Department project portfolio breakdown.
  - Active sprint progress in department.
  - Workload distribution (work items assigned per team member).
  - Department blocked items and overdue count.
  - Access: Admin assigned to that workspace+dept OR Super-Admin.
- **Staff Personal Dashboard (`GET /dashboards/staff`)**:
  - My assigned work items (grouped by status, priority).
  - Items created/reported by me.
  - My accessible projects count.
  - Current sprint work items assigned to me.
  - My overdue and blocked items.
  - Access: Authenticated employee.
- **Project Metrics & Health (`GET /projects/{projectId}/metrics`)**:
  - Item status distribution (Backlog, Ready, In Progress, In Review, Blocked, Completed).
  - Item type distribution (Defects, Features, Stories, Tasks).
  - Story point completion velocity (Total vs Completed estimation points).
  - Access: Anyone with `VIEW_PROJECT`.

### 🔥 HOT Enterprise Concepts & Patterns Used (Interview Gold)
1. **Event-Driven Architecture with Domain Events (`ProjectActivityEvent`)**:
   - Decoupled transactional core logic from side-effects (audit logging, notifications) using Spring's `ApplicationEventPublisher`.
2. **Database Aggregations & Projections (Zero In-Memory Table Scanning)**:
   - Aggregated metric queries using SQL `GROUP BY`, `COUNT`, `SUM`, and JPA interface-based projections rather than loading full entities into heap memory.
3. **Soft-Delete with Audit Preservation**:
   - Comments and project members maintain referential integrity with boolean flags and event trails.
4. **Strict Scope-Enforced Security Barrier**:
   - Department isolation and project membership guards at the database query level to prevent horizontal privilege escalation.

### Phase 7 Step Tracker (HANDOFF updates this BEFORE each step's code is written)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 7.1 | Aggregation Projections & Repositories | `registration/repository/...` | ✅ Done |
| 7.2 | DTOs (SuperAdmin, Department, Staff, Project Metrics) | `registration/dto/DashboardDTOs...` | ✅ Done |
| 7.3 | `DashboardService` implementation | `registration/service/DashboardService.java` | ✅ Done |
| 7.4 | `DashboardController` REST APIs | `registration/controller/DashboardController.java` | ✅ Done |
| 7.5 | Unit tests & build verify | `src/test/java/.../DashboardServiceTest.java` | ✅ Done (103/103 Tests Passing) |

### Phase 8 Step Tracker (HANDOFF updates this BEFORE each step's code is written)

| Step | What | Target File | Status |
|------|------|-------------|--------|
| 8.1 | Configure Swagger/OpenAPI in `SpringSecurity.java` | `registration/security/SpringSecurity.java` | ✅ Done |
| 8.2 | Complete OpenAPI 3.0 Specification JSON | `src/main/resources/static/openapi.json` | ✅ Done |
| 8.3 | `OpenApiController` & Swagger UI viewer | `registration/controller/OpenApiController.java` | ✅ Done |
| 8.4 | Security & Lifecycle Integration Test suite | `src/test/java/.../SecurityIntegrationTest.java` | ✅ Done |
| 8.5 | Final full verification build & test run | All test suites | ✅ Done (110/110 Tests Passing) |

### Endpoints live after Phase 8:
| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | `/api-docs` | Public | Complete OpenAPI 3.0.3 specification JSON |
| GET | `/swagger-ui.html` | Public | Interactive Swagger UI (CDN bundle) |
| GET | `/swagger-ui` | Public | Swagger UI redirect alias |

---

## 🏆 COMPLETE MASTER PLAN STATUS: ALL 8 PHASES FULLY IMPLEMENTED & VERIFIED
- **Phase 0 — Audit & Discovery**: ✅ Complete
- **Phase 1 — Foundation Fixes & Migrations**: ✅ Complete (Flyway V1–V6, Role changes, HOD assignment)
- **Phase 2 — Projects Module**: ✅ Complete (Multi-tier project isolation, keys, scoping)
- **Phase 3 — Project Membership & Permissions**: ✅ Complete (RBAC matrix: OWNER, MANAGER, CONTRIBUTOR, VIEWER)
- **Phase 4 — Work Items & Backlog**: ✅ Complete (9 work item types, state transition validation, hierarchies)
- **Phase 5 — Sprint Management**: ✅ Complete (1 active sprint invariant, auto rollover, sprint metrics)
- **Phase 6 — Collaboration (Comments, Audit, Notifications)**: ✅ Complete (Soft deletes, Domain Events, immutable audit trails)
- **Phase 7 — Dashboards & Scoped Analytics**: ✅ Complete (Database-level SQL aggregations, interface projections)
- **Phase 8 — API Readiness & Verification**: ✅ Complete (OpenAPI 3.0 spec, Swagger UI, Security test suite)

**Final Test Suite Stats: 110 Tests Run, 0 Failures, 0 Errors, 0 Skipped.**

---

## CONFIRMED BUSINESS DECISIONS
| Decision | Answer |
|----------|--------|
| Registration dept check | Active only — code is correct as-is |
| changeRole endpoint | `POST /employee-action/change-role/{empId}` — DONE ✅ |
| HOD assignment | Dedicated endpoint — DONE ✅ |
| Flyway | Added in Phase 1 — DONE ✅ |
| Workspace/Dept CRUD | Deferred to later (not in Phase 1 or 2) |
| Project cross-dept in V1 | NO — one project = one dept = one workspace |
| Project membership | Explicit `ProjectMember` table with roles (OWNER, MANAGER, CONTRIBUTOR, VIEWER) |
| Staff project visibility | Staff can only see projects they belong to |

---

## KEY ARCHITECTURE FACTS (always valid)
- Root package: `com.myapp.worktrack_backend`
- `Employee.employeeId` = numeric DB PK (long) — JWT subject
- `Employee.empId` = string business ID e.g. `EM000003`
- `EmpWorkspaceId` composite: (empId String, workSpaceId int)
- `DepartmentWorkspace` composite: (deptId String len 5, wId int)
- `Department.departmentHead` → FK to `employee.employee_id` (numeric)
- Authorization always via `AuthorizationService.authorize(action, target, role, context)`
- Project authorization via centralized `ProjectPermissionService`
- Actor always resolved from SecurityContext via `AuthenticatedEmployeeDetails.getAuthEmployeeDetails()`
- Never trust role/identity from request body — always from DB via JWT

---
*HANDOFF RULE: Update this file BEFORE writing any code. Mark steps 🟡 when starting, and ✅ as each file is saved to disk. Never mark ✅ until the file is physically written.*
