package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.*;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //将用户输入的密码进行MD5加密，然后将加密后的密码和数据库的密码进行对比
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!employee.getPassword().equals(password)) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增用户实现方法
     *
     * @param employeeDTO
     */
    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        // 检查用户名是否重复
        String userName = employeeDTO.getName();
        if (employeeMapper.getByUsername(userName) != null)
            throw new UserNameRepeatException("该用户名已被占用");

        // 创建Employee实例对象，将DTO的属性赋值给Employee实例对象
        Employee employee = new Employee();
        // 使用BeanUtils进行快速属性传递
        BeanUtils.copyProperties(employeeDTO, employee);

        // 前端已经检查电话号码是否正确以及检查身份证号是否正确
        // 设置默认密码：123456，需要进行加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置账号状态：1-启用；0-锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置创建时间，更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置创建用户，更新用户
        // 从ThreadLocal中获取用户ID
        Long id = BaseContext.getCurrentId();
        employee.setCreateUser(id);
        employee.setCreateUser(id);
        // 从ThreadLocal中删除用户ID
        BaseContext.removeCurrentId();

        // 执行插入操作
        employeeMapper.addEmployee(employee);
    }

    /**
     * 员工分页方法实现
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult getEmployeePageInfo(EmployeePageQueryDTO employeePageQueryDTO) {
        // 使用PageHelper插件
        // 第一步：传递页码和每页数量
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 第二步：通过Mapper方法获取记录数（在xml中不需要写limit，PageHelper会根据传递的页码自动分页）
        // 返回值固定是：Page<要查询的实体类>
        // Page<E> extends ArrayList<E>，其实Page本身就是一个集合
        Page<Employee> page = employeeMapper.getEmployeeRecords(employeePageQueryDTO);

        // 获取总数、获取pageSize条记录
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 启用/禁用员工
     *
     * @param id
     * @param status
     */
    @Override
    public void changeStatus(Long id, Integer status) {
        // 创建employee实例对象
        Employee employee = new Employee();
        // 设置要更新的值
        employee.setStatus(status);
        employee.setId(id);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        // 调用updateAll方法
        // 因为对员工的修改除了status，还有其他可以修改的内容
        // 与其分成多个方法，不如都写成一个update方法
        employeeMapper.update(employee);
    }

    /**
     * 编辑员工信息-方法实现
     *
     * @param employeeDTO
     */
    @Override
    public void editEmployee(EmployeeDTO employeeDTO) {
        // 使用mapper的update方法
        // 创建实例对象，传递属性
        Employee employee = new Employee().builder()
                .id(employeeDTO.getId())
                .idNumber(employeeDTO.getIdNumber())
                .name(employeeDTO.getName())
                .phone(employeeDTO.getPhone())
                .sex(employeeDTO.getSex())
                .username(employeeDTO.getUsername())
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 根据id获取员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getEmployeeInfoById(Long id) {
        return employeeMapper.getById(id);
    }
}
