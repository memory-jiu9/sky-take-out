package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.result.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增用户
     * @param employee
     * @return
     */
    @AutoFill(OperationType.INSERT)
    Integer addEmployee(Employee employee);

    /**
     * 根据用户输入条件查询记录
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> getEmployeeRecords(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 更新用户信息
     * @param employee
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据用户id查询用户信息
     * @param id
     * @return
     */
    Employee getById(Long id);
}
