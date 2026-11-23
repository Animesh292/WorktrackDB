# WORKTRACK V1 — CLAUDE CODE MASTER IMPLEMENTATION PROMPT

## 0. Your role and mission

Act as a senior Java/Spring Boot backend engineer and repository-aware implementation agent for **WorkTrack**.

You are working in an existing local repository. This is not a greenfield project. A substantial foundation has already been written manually. Your job is to understand it, preserve its valid architecture and business rules, identify defects and incomplete flows, and complete the V1 backend incrementally.

Do not start by generating a new application, replacing the existing architecture, or rewriting functioning modules to match your personal preferences.

The existing Lovable frontend will be integrated only after the backend and API contract are ready. For this phase, work on the backend only.

## 1. Source-of-truth and working rules

Use this priority order:

1. The current source code, entities, repository queries, controllers, services, tests and configuration in this repository establish what is actually implemented.
2. The business requirements in this document establish the intended V1 behaviour.
3. If the code and requirements conflict, do not silently choose one. Document the conflict, explain the impact, recommend a safe resolution and request a decision where the business rule is material.
4. Comments, old compiled classes, `target/`, stale test reports and commented-out code are not proof that a feature works.

Before coding, inspect the entire repository and trace important workflows end-to-end. Search for all callers of methods before changing their signatures.

Do not create a new package structure or rename existing packages just for stylistic reasons. Follow the current conventions unless the audit identifies a concrete reason to improve them.

Do not expose JPA entities directly from controllers. Keep request/response DTOs separate from entities and internal authorization contexts.

Do not commit, push, deploy, run destructive database operations, or connect to a production database. Work only in the local development repository and the configured development/test database. Never log passwords, JWTs, signing secrets or sensitive personal data.

## 2. Repository facts to verify against the current checkout

The uploaded baseline showed:

- Root package: `com.myapp.worktrack_backend`.
- Business code primarily under `registration` with `auth`, `controller`, `dto`, `entity`, `exceptionhandler`, `repository`, `security` and `service` packages.
- Java 17, Spring Boot 4.1.1, Maven, Spring Security, Spring Data JPA, MySQL, JWT (`jjwt`), Lombok and Bean Validation dependencies in `pom.xml`.
- Existing entities: `Workspace`, `Department`, `DepartmentWorkspace`, `Employee`, `EmpWorkspace`, `EmpWorkspaceId`, `Registration`, `Login` and `PermanentSuperAdmin`.
- Existing foundation: workspace/department lookup, registration/tracking, login/JWT, employee lifecycle operations, central `AuthorizationService`, `EmployeeLifecyclePolicies`, application approval/provisioning and exception handling.
- No project, project membership, work item, backlog, sprint, comment, notification or project reporting module was present in the uploaded source snapshot.
- Only a small test suite was present. No database migration directory or migration scripts were present in the uploaded snapshot.

Re-check all of these against the live local checkout. The uploaded ZIP may not reflect later local edits.

## 3. Existing domain model and identity rules

Preserve and understand the current identifier distinction:

- `Employee.employeeId` is the numeric database primary key (`employee_id`).
- `Employee.empId` is the string business identifier (for example `EM000003`).
- JWT generation in the uploaded baseline used the numeric `employeeId` as the subject; the JWT filter therefore looked up Employee using the numeric primary key.
- `EmpWorkspaceId` uses the string business `emp_id` plus `work_id` as a composite identifier.
- `EmpWorkspace.dept_id` is a regular column, not part of the current composite ID.
- `DepartmentWorkspace` is a composite ID made from `dept_id` and `w_id`.
- `Department` maps its composite workspace ID using `@EmbeddedId` / `@MapsId` and has a `ManyToOne` relationship to `Workspace`.
- `Department.departmentHead` refers to `Employee.employee_id`, not the string `emp_id`.
- `Login` has a one-to-one relationship with Employee and contains the password hash and `mustChangePassword` flag.
- `PermanentSuperAdmin` has a separate one-to-one relationship to Employee.

Do not mix numeric employee PKs and string employee business IDs in native queries or Java calls. Verify every join against entity mappings and the actual database schema.

The current `EmpWorkspaceId` is `(emp_id, work_id)`, with one `dept_id` column on the assignment row. This means the existing model appears to allow one department assignment per employee per workspace. Confirm whether that is the intended V1 rule. Do not silently change the key or allow multiple department assignments without assessing all foreign keys, queries and business implications.

Workspace stores location/HQ fields. Do not duplicate workspace location on Department unless a confirmed business rule requires it.

## 4. Organizational hierarchy and authority model

There are three ordinary system roles:

- `Staff`
- `Admin`
- `Super-Admin`

There is **no ordinary system role above Super-Admin**. However, Super-Admin has two authority classes:

### 4.1 Staff

Staff are normal employees. They carry out permitted work within departments and projects. They do not receive organization-wide administrative authority.

### 4.2 Admin (delegated administrator)

Admin is a delegated management role. The purpose is to distribute operational work so Super-Admins do not have to perform every lower-level action.

Admin authority is limited to authorized organizational scope. Do not grant an Admin global access merely because their system role is Admin.

The exact distinction between workspace scope and department scope must be verified against the current policies and business rules. For registration approval, an Admin's scope must be checked against the **approver-selected final workspace and department**. For existing employee lifecycle operations, inspect the existing shared-workspace query and report whether the intended rule is workspace-only or workspace-plus-department before changing it.

### 4.3 Temporary/Elected Super-Admin

A Temporary/Elected Super-Admin has the `Super-Admin` system role and global operational access across workspaces and departments. They can manage Admins and all lower roles, and perform the elevated cross-workspace operations allowed by policy.

A Temporary/Elected Super-Admin is **not** automatically allowed to perform protected actions against another Super-Admin.

### 4.4 Permanent Super-Admin

Permanent Super-Admin is an additional, separately recorded authority status—not a fourth ordinary role and not automatically implied by `employee.role = 'Super-Admin'`.

The uploaded source implements this using:

- `PermanentSuperAdmin` entity/table.
- A unique one-to-one relationship to Employee.
- `PermanentSuperAdminRepo.existsByEmployeeEmployeeId(employeeId)`.

Permanent Super-Admins are fixed/trusted authorities. Their additional privilege is to perform protected operations involving Temporary/Elected Super-Admins, who hold the ordinary `Super-Admin` role but do not have permanent status.

Preserve this separation:

- A Temporary/Elected Super-Admin can manage Staff and Admins across workspaces according to the policy.
- A Temporary/Elected Super-Admin cannot manage another Super-Admin in protected role/lifecycle/registration operations unless the policy explicitly grants it.
- A Permanent Super-Admin (who is also a Super-Admin employee) can perform the protected operations against another Super-Admin where the existing policy requires permanent authority.
- Keep self-action restrictions unless the current confirmed policy explicitly says otherwise.
- Never allow an ordinary Admin, Staff member or Temporary/Elected Super-Admin to add themselves to the PermanentSuperAdmin table or grant themselves permanent status.

The uploaded source contains the entity and repository but no complete dedicated Permanent Super-Admin management/bootstrap workflow. Audit how permanent rows are provisioned and document a safe, controlled approach. Do not expose an ordinary endpoint that lets a Super-Admin self-promote to permanent authority. If management of this fixed list is not explicitly specified, preserve it as a controlled bootstrap/administrative process and ask before creating add/remove APIs.

## 5. Department Head / HOD rules

Each Department can have a designated Department Head (HOD).

The existing Department entity has a `departmentHead` relationship to Employee through `dept_head_employee_id` referencing the numeric Employee PK.

Intended eligibility rules:

- The HOD must be an existing Employee.
- The employee must be `Active`.
- The employee must have the `Admin` system role.
- The employee must belong to the same workspace as the Department.
- A Super-Admin selects/appoints the eligible HOD.
- HOD appointment is department-specific. Being an Admin does not automatically make the employee HOD of every department.
- An absent, inactive, non-Admin or wrong-workspace HOD must not satisfy approval/provisioning prerequisites.

The HOD needs departmental oversight of the department's employees, projects and work activity, subject to explicit policies.

Audit whether current validation checks same workspace only or also department membership. Preserve the confirmed rule; do not invent a stricter or looser eligibility rule without reporting it.

## 6. Existing central authorization design — preserve and complete

The uploaded baseline uses one central method with four arguments:

`authorize(Action action, AuthorizableResource target, String requestedRole, RegistrationApprovalContext registrationApprovalContext)`

Preserve this single `AuthorizationService` entry point unless you first document a concrete issue and get approval to change it.

Current action enum includes:

- `activate_employee`
- `suspend_employee`
- `terminate_employee`
- `approve_registration`
- `reject_registration`
- `change_role`

The dispatcher validates target types and delegates to `EmployeeLifecyclePolicies`.

Keep the distinct role inputs straight:

- `requestedRole` is the role proposed by the actor for an **existing employee** in the Change Role use case.
- `RegistrationApprovalContext.role()` is the final role selected by the authorized approver for a **registration applicant**.
- `RegistrationApprovalContext.departmentId()` and `workspaceId()` are the proposed final assignment selected by the approver.
- The authenticated actor and actor role must always be obtained from the SecurityContext/authenticated-employee service/database, never from request body fields.

Do not conflate the actor's current role with the target's proposed role. Do not force one request parameter into a different use case.

A shared role-assignment validator may be introduced only after the existing policy behavior is fully audited and tested. Do not perform a broad authorization rewrite as a preliminary cleanup.

## 7. Required EmployeePolicy rules to audit and test

The following expresses the intended policy model discussed for WorkTrack. Compare each rule to the actual source and tests. Produce an explicit matrix of implemented / incomplete / missing / conflicting behavior before changing anything.

### 7.1 Common actor requirements

- Derive actor identity from the SecurityContext using the numeric Employee PK stored as JWT subject in the uploaded implementation.
- Resolve the Employee record from the database.
- Actor must be `Active` to exercise administrative authorization. The uploaded `AuthenticatedEmployeeDetails.getAuthEmployeeDetails()` performs this check; preserve or improve it consistently.
- Validate persisted role values against the supported set: Staff, Admin, Super-Admin.
- Prevent self-actions for lifecycle and Change Role operations unless a confirmed rule states otherwise.

### 7.2 Employee lifecycle (activate, suspend, terminate)

- Only an Active actor can perform an allowed lifecycle action.
- Staff cannot activate, suspend or terminate employees.
- Target must be in a valid state for the requested transition: suspension expects Active; activation expects Suspended; termination must reject an already Terminated employee.
- Actor cannot act on themselves under the current policy.
- An Admin cannot action another Admin of equal role.
- An Admin may action Staff only within the Admin's authorized scope.
- A Temporary/Elected Super-Admin may action Admin and Staff across workspaces, subject to role hierarchy.
- A Super-Admin target requires the actor to be a Super-Admin and to pass the PermanentSuperAdmin membership check.
- Permanent authority must be checked against the authenticated actor's numeric Employee PK.
- Preserve historical EmployeeWorkspace rows when an employee is terminated. Termination changes employment status; it must not delete assignment history.

### 7.3 Change Role

- Keep `requestedRole` as the proposed role for an existing target Employee.
- Validate actor role, target role and requested role against the supported role set.
- Prevent self-role changes and no-op role changes according to current policy.
- Staff cannot change roles.
- Admin can promote an eligible Staff employee to Admin only within the allowed scope. Admin cannot promote/demote another Admin or manage a Super-Admin.
- Temporary/Elected Super-Admin may promote/demote Staff and Admin across workspaces.
- Changing the role of a Super-Admin target requires the acting Super-Admin to be Permanent Super-Admin.
- Ensure scope query semantics are correct. In the uploaded source, the Admin branch appears to deny when `isScopeValid(...) != 0`, which appears inverted relative to the query's count semantics. Verify and correct with tests.
- Do not rely on the method name alone; inspect the actual SQL and its return type.

### 7.4 Registration approval

A Registration is not yet an Employee. Do not require an applicant EmployeeWorkspace row before approval.

- Registration must be Pending.
- Actor must be Active and permitted by role hierarchy.
- Staff cannot approve.
- Admin can approve an effective Staff assignment only when the approver-selected workspace and department are in the Admin's authorized scope.
- Admin cannot approve an effective Admin assignment (same role) or a Super-Admin assignment (higher role).
- Temporary/Elected Super-Admin can approve Staff/Admin assignments across workspaces.
- Approving a Super-Admin applicant/assignment requires the acting Super-Admin to be in the PermanentSuperAdmin table.
- For an Admin, validate the exact proposed destination from `RegistrationApprovalContext` against the actor's employee-workspace assignment; do not check against the applicant's original preference if the approver is overriding it.
- Super-Admin global authority must not be accidentally restricted by ordinary Admin scope checks.
- Perform authorization before provisioning, and perform workspace/department/capacity/HOD validations before creating Employee, Login and EmployeeWorkspace records.

### 7.5 Applicant preference vs approved assignment

In the uploaded snapshot:

- `RegistrationDTO` contains workspace and department identifiers but does not contain a requested role field.
- `RegistrationService` sets `Registration.role` to null.
- `ApprovalRequestDTO` carries role, departmentId and workspaceId selected for approval.
- `ApplicationStateService.employeeApproval()` builds `RegistrationApprovalContext` from that approval request and uses the approval request's workspace/department for subsequent business validation and EmployeeWorkspace creation.

Preserve this distinction. The approval request is the approver's proposed final assignment; it may override the applicant's original registered workspace/department.

Do not silently add an applicant role preference or assume one exists. If V1 should capture an applicant role preference, identify this as a business/API change and propose the schema/DTO impact before implementing it.

### 7.6 Registration rejection

The uploaded source has a gap:

- `EmployeeLifecyclePolicies.rejectRegistration()` exists and delegates to `authorizeRegistration(...)`.
- `ApplicationStateService.registrationRejection()` currently only checks Pending, sets Rejected and saves; it does not call AuthorizationService.
- The current rejection controller does not accept an approval context/request body.

This must be corrected: rejection must be authorized before changing registration status.

However, rejection does not create an employee or persist a final assignment. The applicant's current registration workspace/department are the known resource scope. The uploaded Registration does not persist applicant role preference (`role` is set null). Do not invent an applicant target role or trust a client-supplied fake role just to reuse approval logic.

During the audit, propose and document a clean rejection policy that:

- Checks authenticated actor status and role.
- Checks registration Pending state.
- Checks Admin scope against the registered workspace/department resource unless an explicitly confirmed alternative exists.
- Applies Permanent Super-Admin restrictions only where the relevant target/operation is genuinely a protected Super-Admin action.
- Does not execute approval-only capacity checks, HOD checks or employee provisioning.

If a role-based rejection rule depends on an applicant role that is not stored, report the data-model gap and ask for a business decision rather than fabricating the role.

## 8. Workspace, Department and employee lifecycle business rules

Preserve and verify these rules:

- Workspace stores office/HQ location fields; Departments belong to a Workspace.
- Department is identified using composite `(dept_id, w_id)`.
- Employee has numeric `employee_id` PK and string `emp_id` business identifier.
- EmployeeWorkspace identifies an employee's workspace assignment and stores department ID.
- Workspace must be Active for new registration and approval.
- Registration may allow the user to select a Draft department if that remains the established registration workflow; approval requires the destination department to be Active.
- Department capacity is checked during approval.
- Active and Suspended employees count against department capacity; Terminated employees do not.
- Department must have a configured positive capacity if capacity is mandatory for that department.
- Department must have an eligible active Admin HOD in the same workspace before approval/provisioning where required.
- Employee termination must preserve historical EmployeeWorkspace data.
- Deactivating a workspace blocks new registration/approval but should not erase historical records.
- Deactivating a department should block new approval/assignment according to the business rules.

Capacity enforcement must be safe under concurrent approvals. The current uploaded implementation counts capacity but does not visibly lock the department before checking. Design an appropriate transaction/locking mechanism (for example, pessimistic locking of the department row) so simultaneous approvals cannot overfill capacity. Explain lock scope and transaction behavior, and test it.

## 9. Verified issues from the uploaded source snapshot — inspect current local files before acting

The uploaded source showed these concerns. They may have been fixed in the user's current local checkout, so re-check first:

1. `EmployeeLifecyclePolicies.authorizeRegistration(...)` contained an incomplete Admin scope repository expression (`employeeWorkspaceRepo.is...`). Verify current file and complete the scope check using the approver-selected workspace/department.
2. `ApplicationStateService.registrationRejection(...)` did not invoke `AuthorizationService`, despite the policy method existing.
3. `EmployeeLifecyclePolicies.changeRole(...)` appeared to invert the Admin shared-workspace count condition (`!= 0` denied). Verify query semantics and fix only with passing policy tests.
4. `EmployeeWorkspaceRepo.countWorkDeptEmployees(...)` joined `employee.employee_id` to `emp_workspace.emp_id`, while entity mappings show `emp_workspace.emp_id` is the Employee business string ID. Verify actual DDL and correct the join if confirmed.
5. `DepartmentService.getActiveDepartmentsbyWorkspace(...)` queried status `Draft` even though the method and comments refer to active departments. Registration allowed Draft by a commented-out status check. Preserve the actual intended dropdown/registration rule and make method names/status filters consistent.
6. `RegistrationRepo.isScopeValidRegistration(...)` had an incomplete query string (`Select `). Determine whether it is used; remove or implement it correctly.
7. Tests in the uploaded source called old one-argument registration policy signatures and stubbed a `long` query return with booleans. Update tests to the current APIs and query types.
8. Existing `target/` class files and Surefire reports may be stale. Never use them as proof that current source passes.
9. The uploaded `.gitignore` did not exclude `application.properties`; the uploaded properties file contains datasource and JWT configuration keys. Move secrets to environment variables or a safely ignored local profile/configuration. Do not expose any credential values in logs or generated documentation.
10. The uploaded repository had no migration scripts/framework visible. Propose a safe migration strategy and get approval before adding a migration dependency or applying schema changes.

The above is a verification checklist, not permission to perform unreviewed destructive refactors.

## 10. Project Management module (new V1 functionality)

Implement Projects as a core domain feature.

A Project belongs to one Workspace and one Department. V1 should be department-oriented. Unless a confirmed requirement says otherwise, do not allow one project to span multiple departments/workspaces.

Project data should include:

- Project ID and human-readable project key (unique within the agreed scope).
- Name and description.
- Workspace and composite Department association.
- Creator/owner.
- Status/lifecycle.
- Optional start date, target end date and actual completion date.
- Created/updated timestamps and audit metadata.

Department Admin/HOD should be able to manage and monitor projects in their authorized department scope. Super-Admin has cross-workspace oversight. Staff can see only projects to which they are authorized members or have explicit access.

Enforce this on the backend, not just in UI visibility.

## 11. Project membership and project-level permissions (mandatory)

Do not rely only on global Employee roles for project access. Introduce explicit Project Membership and project-level authorization.

A ProjectMember relationship should capture at least:

- Project.
- Employee.
- Project-scoped role or permission assignment.
- Membership status.
- Added-by actor.
- Created/updated timestamps.

Add a database uniqueness constraint to prevent duplicate active membership for the same employee/project. Only eligible Active employees can be added.

By default, a member must belong to the same workspace and department as the project. Treat cross-department membership as out of V1 unless explicitly approved. Confirm this against the actual EmployeeWorkspace cardinality model before designing the FK/constraint.

Suggested project-scoped roles (do not confuse with system roles):

- Project Owner.
- Project Manager.
- Contributor.
- Viewer.

Design a maintainable permission model for at least:

- View project.
- Update project metadata.
- Manage project members.
- Create work items.
- Update work items.
- Assign and reassign work items.
- Change work-item status.
- Manage backlog.
- Create, plan, start and complete sprints.
- Review/accept completed work.
- View project reports.
- Comment.
- Upload/view attachments.

Define and document the default permission matrix for each project role. A permission must never elevate a Staff employee to global Admin/Super-Admin authority.

Department HOD/Admin has departmental oversight; project-scoped membership controls ordinary project collaboration. A Staff member may view permitted teammate progress inside a project but must not automatically be allowed to edit or reassign all teammates' work.

Implement centralized project permission evaluation in the existing authorization architecture (or a clearly justified extension). Avoid duplicating slightly different permission checks in each controller.

## 12. Work management module

Create a generic, extensible WorkItem model rather than one JPA entity per type.

Use these proposed WorkTrack names:

- **Mission** — large business objective (epic-like).
- **Capability** — major feature or functional area.
- **User Outcome** — user-facing requirement/outcome (story-like).
- **Work Item** — concrete unit of development or operational work.
- **Action Step** — smaller child unit/subtask.
- **Defect** — bug or faulty behavior.
- **Discovery** — research, exploration or technical investigation.
- **Enhancement** — improvement to existing functionality.
- **Checkpoint** — milestone or delivery target.

Names are product vocabulary; keep the type model configurable/extensible. Do not create a separate entity/table for each type without demonstrating a real need.

Support:

- Unique project-scoped work-item key/number.
- Project association.
- Parent/child relationship where valid.
- Type, title, description.
- Reporter/creator and assignee.
- Priority and status.
- Due date and optional start date.
- Estimate/story points if appropriate.
- Acceptance criteria where applicable.
- Sprint association.
- Created/updated timestamps and change history.

Keep independent concepts separate:

- Work-item type = what kind of work it is.
- Status = workflow position.
- Priority = importance/urgency.
- Sprint = iteration in which work is planned.

Define permitted parent-child type combinations and validate them in the service/domain layer.

## 13. Backlog and work-item workflow

Implement project backlog operations and a controlled workflow. Initial suggested statuses:

- Backlog.
- Ready.
- In Progress.
- In Review.
- Blocked.
- Completed.
- Cancelled.

Adapt names if the code/product vocabulary calls for it, but enforce valid transitions and prevent arbitrary status changes.

Support search, filtering, sorting and pagination by type, status, priority, assignee and sprint. Support work-item detail, parent/child navigation, assignment/reassignment, comments and activity history.

Staff may create permitted items in projects where they have access. Editing/reassigning other employees' work requires explicit project permission.

## 14. Sprint management

Each Project owns its backlog and sprints.

Sprint data should include:

- Sprint ID and project association.
- Name and goal.
- Start/end dates.
- State (for example Planned, Active, Completed, Cancelled).
- Created-by and timestamps.

Support:

- Create and update planned sprints.
- Add eligible backlog items to a sprint.
- Start a sprint.
- View sprint board/progress.
- Move items within permitted states.
- Remove/return items to backlog under valid rules.
- Complete a sprint.
- Handle unfinished work items explicitly.
- Retain sprint history.
- Enforce the agreed rule about active sprints per project (recommend one active sprint per project for V1; report if a different business rule is needed).

Only users with the appropriate project permissions can manage sprint planning/execution.

## 15. Collaboration, history and notifications

Implement:

- Work-item comments with author and timestamps.
- Activity history for important changes: create, edit, status, assignee, priority, sprint membership, project membership and key project settings.
- In-app notification foundation for assignments, reassignment, mentions/comments, review requests and sprint changes.
- Secure attachment metadata/storage abstraction if attachments are included in V1; never expose raw server file paths or permit arbitrary file reads.

Every activity entry must identify actor, affected resource, action, timestamp and relevant changed fields. Authorization must control visibility of comments, activities and attachments.

Do not introduce email, SMS, push or third-party notification providers without approval. Build provider-independent in-app notifications first.

## 16. Dashboards and reporting APIs

### Super-Admin

- Overview across workspaces and departments.
- Employee status summaries.
- Admin/HOD oversight.
- Project and sprint activity across workspaces.
- Cross-workspace progress and exception reporting.

### Admin/HOD

- Department employee overview.
- Department project portfolio.
- Active sprint progress.
- Workload distribution.
- Overdue and blocked work.
- Pending reviews.
- Project activity and employee progress.

### Staff

- Assigned work.
- Work created by the employee.
- Accessible projects.
- Current sprint items.
- Due dates and overdue/blocked work.
- Relevant team activity within projects they can access.

All dashboard results must respect workspace, department, project membership and permission scope. Use efficient repository queries, projections and pagination. Do not load entire tables into application memory to calculate summaries.

## 17. API and Lovable frontend integration readiness

The future Lovable UI must consume this Spring Boot backend through documented REST APIs.

Provide consistent:

- REST resource naming and HTTP methods.
- Request/response DTOs.
- Validation (`@Valid` where appropriate).
- Status codes and structured error responses.
- JWT Bearer authentication.
- Authorization responses that distinguish 401 from 403.
- Pagination, sorting and filtering.
- CORS configuration for the actual frontend origin(s), with no wildcard credential configuration.
- OpenAPI/Swagger documentation with schemas, endpoints, auth requirements, examples and error responses.

Do not add a second backend, Supabase database or alternate source of truth for Lovable. The Spring Boot API and MySQL database remain authoritative.

Before adding OpenAPI or migration dependencies, inspect compatible versions with the current Spring Boot 4.1.1 baseline and propose the dependency changes.

## 18. Database integrity, transactions and security

- Preserve the current PKs, composite IDs and relationships unless a verified defect requires a change.
- Verify native SQL joins against actual column mappings and DDL.
- Add appropriate FK constraints, unique constraints and indexes.
- Use transactions for multi-entity workflows.
- Prevent duplicate approval/provisioning and capacity races.
- Use optimistic/pessimistic locking where justified and test concurrency-sensitive code.
- Keep employee/workspace history when employees are terminated.
- Use schema migrations for future schema evolution; do not rely on destructive Hibernate DDL in production.
- Keep environment-specific credentials in environment variables or ignored local configuration.
- Do not commit secrets or real personal data.
- Use password hashing and never store/log raw passwords except the one-time credential returned in the controlled provisioning response if that remains the approved onboarding approach.
- Avoid logging Authorization headers or JWT values.
- Ensure suspended/terminated actors cannot continue using previously issued tokens for protected operations. Decide whether to reject them in the JWT filter, a shared authentication resolver or another central mechanism, and test revocation/status behavior.
- Do not swallow token parsing/database exceptions silently. Return safe external responses while retaining useful, non-sensitive server diagnostics.

## 19. Testing and definition of done

Tests are mandatory. A successful compile alone does not complete a feature.

Create/update unit, repository, MVC/security and integration tests appropriate to the feature.

At minimum cover:

- JWT subject/employee lookup and role authorities.
- Inactive, suspended and terminated actor rejection.
- Every registration approval/rejection role combination.
- Permanent versus Temporary/Elected Super-Admin operations.
- Admin in-scope and out-of-scope approval/lifecycle actions.
- Change Role permissions and prevention of escalation.
- Department/HOD eligibility.
- Draft registration selection and Active approval validation.
- Capacity boundary and concurrent approvals.
- Composite-key repository operations and native SQL queries.
- Project CRUD and workspace/department enforcement.
- Duplicate membership prevention.
- Project role permission matrix and direct API denial tests.
- Work-item parent/child constraints, assignment and transitions.
- Sprint lifecycle and unfinished items.
- Comment/activity visibility.
- Dashboard isolation between departments/workspaces/projects.
- Consistent 400/401/403/404/409 responses.

Use a dedicated test database or containerized test database. Never point tests at production.

Run the full Maven test suite and package build after every major phase. Report exact commands, test counts, failures and known gaps. Do not edit tests to simply weaken assertions or make an incorrect implementation appear green.

## 20. Required phased implementation process

### Phase 0 — Read-only audit (first task; no source changes)

Inspect the complete local repository, not just a sample of files.

Produce:

1. Package and architecture map.
2. Entity/relationship/composite-key map.
3. API endpoint inventory.
4. EmployeePolicy role/authority matrix.
5. Separate Permanent vs Temporary/Elected Super-Admin privilege matrix.
6. Registration/approval/rejection sequence diagrams or clear step descriptions.
7. Workspace/department/HOD business-rule map.
8. Existing tests and build health.
9. Confirmed defects and incomplete flows with exact file/method references.
10. Completed / partial / missing feature matrix against this V1 specification.
11. Proposed implementation sequence and database migration plan.
12. Explicit list of business decisions requiring confirmation.

Do not edit any source, configuration, test or database file during Phase 0. Do not create or modify migrations. Run only safe read-only inspections and build/test commands if prerequisites are already available. If build tools/dependencies/network are unavailable, report that limitation instead of treating old target reports as current results.

Wait for explicit approval before implementing Phase 1.

### Phase 1 — Repair and complete the existing foundation

Correct verified defects in authentication, registration, approval/rejection, EmployeePolicy, permanent authority enforcement, lifecycle, workspace/department validation, HOD validation, capacity concurrency and error handling. Preserve working logic and tests.

### Phase 2 — Project domain and project lifecycle

Create projects, department/workspace relationships, project ownership and controlled administration.

### Phase 3 — Project membership and permission engine

Add ProjectMember, project-scoped roles and permission evaluation. Secure every project API with server-side policy checks.

### Phase 4 — WorkItem and backlog

Add work-item types/hierarchy, assignment, status workflow, search/filter/pagination and backlog APIs.

### Phase 5 — Sprint management

Add sprint planning, execution, completion and sprint reports.

### Phase 6 — Collaboration

Add comments, activity history and in-app notifications; add secure attachment support if approved.

### Phase 7 — Dashboards and reports

Add role- and scope-aware reporting APIs.

### Phase 8 — API handoff and V1 verification

Generate OpenAPI documentation, verify every API, run complete tests, review security and data integrity, document environment setup and prepare Lovable integration instructions.

After each phase, report files changed, design decisions, migrations, tests executed, results, remaining risks and the next phase proposal. Do not start a subsequent phase until the current phase is reviewed/approved.

## 21. Git and execution constraints

- Work on the dedicated AI development branch.
- Do not commit, push, merge or deploy.
- Do not modify production settings or production databases.
- Do not execute DROP, TRUNCATE or destructive ALTER operations.
- Do not bulk-reformat or rename unrelated packages.
- Do not modify the Lovable frontend in this backend task.
- Do not install arbitrary libraries without explaining the reason and confirming compatibility.
- Ask before changing domain semantics, role hierarchy, PermanentSuperAdmin governance, composite keys or cross-department access rules.
- Review diffs and build/test results after each meaningful change.

## 22. First response expected from you

Start in **read-only planning/audit mode**.

Do not write or modify source files.

Return the Phase 0 audit report and implementation roadmap using the deliverables above. Be explicit about what is observed in code versus what is a business requirement in this prompt. Identify every mismatch, especially in EmployeeLifecyclePolicies, PermanentSuperAdmin checks, registration rejection, approval assignment context, native repository queries and current tests.

Wait for my approval before making implementation changes.
