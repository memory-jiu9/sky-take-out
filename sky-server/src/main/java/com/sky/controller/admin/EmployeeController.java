package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工接口-EmployeeController")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录方法-login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出登录方法-logout")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增用户
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增用户方法-addEmployee")
    public Result addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增用户信息：{}", employeeDTO);
        employeeService.addEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页方法
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页方法-getEmployeePageInfo")
    public Result<PageResult> getEmployeePageInfo(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("前端传递的参数为：{}", employeePageQueryDTO);
        return Result.success(employeeService.getEmployeePageInfo(employeePageQueryDTO));
    }

    /**
     * 启用/禁用员工
     *
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用员工-changeStatus")
    public Result changeStatus(Long id, @PathVariable Integer status) {
        log.info("员工id：{}；要将状态修改成：{}", id, status);
        employeeService.changeStatus(id, status);
        return Result.success();
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息-editEmployee")
    public Result editEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("要修改的员工信息是：{}", employeeDTO);
        employeeService.editEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 根据id获取员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id获取员工信息-getEmployeeInfoById")
    public Result<Employee> getEmployeeInfoById(@PathVariable Long id) {
        log.info("要查询的员工id是：{}", id);
        Employee employee = employeeService.getEmployeeInfoById(id);
        return Result.success(employee);
    }

}
