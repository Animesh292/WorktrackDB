package com.myapp.worktrack_backend.registration.repository;

import com.myapp.worktrack_backend.registration.entity.EmpWorkspace;
import com.myapp.worktrack_backend.registration.entity.EmpWorkspaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeWorkspaceRepo extends JpaRepository<EmpWorkspace, EmpWorkspaceId> {
    @Query(value = "select count(*) from emp_workspace where emp_id=:targetId and work_id in (select work_id from emp_workspace where emp_id=:actorId)", nativeQuery = true)
    long isScopeValid(@Param("targetId") String targetId,@Param("actorId") String actorId);
    @Query(value = "select count(*) from emp_workspace ew inner join employee e on e.emp_id=ew.emp_id where ew.dept_id=:deptId and ew.work_id=:workId and e.employee_status in ('Active', 'Suspended')", nativeQuery = true)
    long countWorkDeptEmployees(@Param("deptId") String deptId, @Param("workId") int workId);
    @Query(value = "select count(*) from emp_workspace where emp_id=:deptHeadId and work_id=:workId", nativeQuery = true)
    long getCountOfDeptHeadWorkspaces(@Param("deptHeadId") String deptHeadId, @Param("workId") int workId);

    @Query(value = "select count(*) from emp_workspace where emp_id=:empId and work_id=:workId", nativeQuery = true)
    long isAssignedToWorkspace(@Param("empId") String empId, @Param("workId") int workId);

    @Query(value = "select count(*) from emp_workspace where emp_id=:empId and work_id=:workId and dept_id=:deptId", nativeQuery = true)
    long isAssignedToWorkspaceAndDepartment(@Param("empId") String empId, @Param("workId") int workId, @Param("deptId") String deptId);

}

// query for scope -> select count(*) from emp_workspace where targetId=7 and work_id in (select work_id from emp_workspace where ActorId=3);
