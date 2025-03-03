# Day03知识点总结

## 1.Spring IOC

Spring IOC (Inversion of Control) 是一种设计模式，核心思想是**控制反转**。在以前，对象是由程序员手动创建并且调用的，但是在Spring IOC 中，对象的创建和依赖关系由 Spring 容器管理，将对象的控制权交给 Spring 管理。

DI (Dependency Injection) ：依赖注入是 IOC 的具体实现方式。当一个类内部需要用到其他类的实例对象时，就称作这个类依赖其他类，通过依赖注入，不需要手动创建依赖类的对象，在程序运行时会将所依赖的对象自动注入。依赖注入需要使用到**@Autowired**注解。

#### 什么是 Bean？

Bean 是 Spring 容器中管理的对象，即被 Spring 管理和实例化的 Java 类的实例对象。

Bean 由 Spring 创建、管理、初始化、销毁。

Bean 默认是单例模式，也就是一个程序中只能存在一个 唯一的Bean，不能存在两个相同的 Bean。

给一个类添加 **@Component** 注解，这个类在程序加载时就会被 Spring 自动创建实例对象，这个实例对象就是一个 Bean，这个 Bean 会交给 Spring 容器管理。Bean 其实也是有名字的，默认是类名首字母小写，当然，也可以在使用 **@Component**注解时指定 Bean 的名字，比如：`@Service(value = "bean")`

除了 **@Component** 注解，还有其他注解也能实现同样的效果：

- @Controller：添加在控制层类上（该注解被@Component修饰）
- @Service：添加在服务层类上（该注解被@Component修饰）
- @Mapper：添加在持久层类上（该注解是 MyBatis 提供的，没有显式被@Component修饰。MyBatis 提供了专门的机制来注册 Mapper，而不需要依赖 Spring 传统的 @Component 方式）

如果有其他类，不属于**三层结构**，但是也想要被 Spring 管理，那么就使用 @Component 注解修饰。

#### 每个 Bean 都会生效吗？

并不是没有 Bean 都能够被 Spring 管理，这个问题涉及 Bean 的组件扫描。

一个 Bean 想要生效，前提是要被**@ComponentScan**注解扫描到，这个注解被包含在启动类的`@SpringBootApplication`注解中，默认扫描的范围是启动类所在包及其子包。如果有 Bean 的位置放在了启动类所在包的包外，则无法被扫描到，也无法自动创建对象，交给 Spring 管理。

如果你的 Bean 在启动类包外（com.other.service），你可以在 **@ComponentScan** 中手动指定额外的扫描路径。

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example", "com.other.service"})
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```



## 2.Spring AOP

AOP (Aspect-Oriented Programming，面向切面编程) 是 Spring 提供的一种编程思想，它可以在不修改原始代码的情况下，增强方法的功能，动态代理是实现 AOP 的主流技术。

AOP 的核心概念：

- Advice 通知：额外添加的代码
- JoinPoint 连接点：可以被 AOP 拦截的方法
- PointCut 切入点：一种规则，满足这种规则的方法可以被拦截
- Target 目标对象：被 AOP 增强的原始对象
- Proxy 代理对象：Spring 生成的代理类
- Weaving 编织：把切面逻辑插入到目标对象的方法中

以下是苍穹外卖的一个切面类：

```java
@Aspect // 声明是一个切面类
@Component // 将这个类交给 Spring 管理
@Slf4j // 记录日志
public class AutoFillAspect {

    /**
     * 设置切入点，在com.sky.mapper下的所有类的所有方法 && 该方法添加了@AutoFill注解都会被增强
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    // 前置插入
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("现在进行切面编程");
        // 获取目标方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);

        // 获取注解传入的参数
        OperationType value = annotation.value();
        // 获取目标方法的参数，第一个参数就是数据对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.info("autoFill:没有参数传入");
            return;
        }
        Object entity = args[0];

        // 准备要使用的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 判断该注解是插入操作还是更新操作
        if (value == OperationType.INSERT) {
            try {
                // 获取设置时间的方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                // 获取设置用户的方法
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);


                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);

                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

- @Aspect：声明是一个切面类
  @Component：将这个类交给 Spring 管理
  @Slf4j：记录日志

- @Pointcut("execution(* com.sky.mapper.\*.*(..)) && @annotation(com.sky.annotation.AutoFill)")：指定切入点。

  常见的切入点表达式有两种：**execution()**和**@annotation()**

  execution(权限修饰符 返回值 包名.类名.方法名(方法参数) throws 异常?)，其实就是将整个方法头都表示出来，用来确定方法。

  - 权限修饰符可以省略，这个很少使用
  - 包名.类名一般不省略，因为只要靠完整的包名才能找到对应的类
  - throws抛出的异常也可以省略，也没有谁会把一整个方法抛出的异常都一个一个写上去吧
  - \* com.sky.mapper.\*.\*(..)：\* 匹配一个单个部分，.. 匹配多个部分。这里的表达式是：任何返回值(\*)，在 com.sky.mapper 下的任何类(\*)，任何方法(\*)，任何形参(..) 都能够匹配上
  - @annotation(com.sky.annotation.AutoFill)：需要指定使用的注解。这两个表达式组合起来，可以切入 com.sky.mapper 包下任何添加了 @AutoFill 的方法

- 除了 @Pointcut 注解可以使用切入点表达式定位切入方法，还可以使用其他注解：

  - @Around：在目标方法执行前，和目标方法执行后执行
  - @Before：在目标方法执行前执行
  - @After：在目标方法执行后执行，哪怕该方法出现异常也会执行
  - @AfterReturning：在目标方法顺利执行并且返回后，再执行通知
  - @AfterThrowimg：当目标方法抛出异常后执行通知，若目标方法顺利执行，则通知不会执行

```java
@Component
@Aspect
public class AOP {
    @Before("execution(* com.example.service.*.*(..))")
    public void a1(JoinPoint joinPoint) {
        System.out.println(1);
    }

    // 因为@Around在目标方法执行前后都有逻辑需要执行，所以必须手动调用ProceedingJoinPoint.proceed()让原始方法执行
    // 形参类型必须是ProceedingJoinPoint，JoinPoint是ProceedingJoinPoint的父类
	// 返回值类型必须是Object
    @Around("execution(* com.example.service.*.*(..))")
    public Object a2(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(2.1);
        Object result = joinPoint.proceed();

        System.out.println(2.2);
        return result;
    }

    @After("execution(* com.example.service.*.*(..))")
    public void a3(JoinPoint joinPoint) {
        System.out.println(3);
    }

    @AfterReturning("execution(* com.example.service.*.*(..))")
    public void a4(JoinPoint joinPoint) {
        System.out.println(4);
    }

    @AfterThrowing("execution(* com.example.service.*.*(..))")
    public void a5(JoinPoint joinPoint) {
        System.out.println(5);
    }
}

/*===================================================================*/

@Component
@Aspect
public class AOP {
    // @Pointcut注解的作用：将公共的切点表达式抽取出来
    @Pointcut("execution(* com.example.service.*.*(..))")
    private void jiu(JoinPoint joinPoint) {
    }

    // 其他注解直接使用公共的切点表达式即可
    @Before("jiu()")
    public void a1(JoinPoint joinPoint) {
        System.out.println(1);
    }

    @Around("jiu()")
    public Object a2(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(2.1);
        Object result = joinPoint.proceed();

        System.out.println(2.2);
        return result;
    }

    @After("jiu()")
    public void a3(JoinPoint joinPoint) {
        System.out.println(3);
    }

    @AfterReturning("jiu()")
    public void a4(JoinPoint joinPoint) {
        System.out.println(4);
    }

    @AfterThrowing("jiu()")
    public void a5(JoinPoint joinPoint) {
        System.out.println(5);
    }
}
```

#### AOP 通知执行顺序

当多个切面类针对同一个方法的通知优先级相同，那么这些通知的执行顺序是怎么样的？

```java
@Component
@Aspect
public class AOP1 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}

@Component
@Aspect
class AOP2 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}

@Component
@Aspect
class AOP3 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}
```

@Before 修饰的通知方法默认按照类名**自然排序**，类名越靠前，越先被执行

因为 AOP1 > AOP2 > AOP3，所以先打印1，再打印2，最后打印3

@After 修饰的通知方法默认按照类名**反自然排序**，类名越靠前，越后被执行；类名越靠后，越先被执行

因为 AOP1 < AOP2 < AOP3，所以先打印3after，再打印2after，最后打印1after

当然，只靠类名的自然排序，非常不可控，可以利用**@Order()**设置优先级

```java
@Component
@Aspect
@Order(1)
public class AOP1 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}
@Component
@Aspect
@Order(2)
class AOP2 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}

@Component
@Aspect
@Order(3)
class AOP3 {
    @Before("execution(* com.example.service.*.*(..))")
    public void before(){
        System.out.println(1);
    }

    @After("execution(* com.example.service.*.*(..))")
    public void after(){
        System.out.println("1after");
    }
}
```

对 @Before 方法来说，数字小的先执行
对 @After 方法来说，数字大的先执行



## 3.阿里云OSS





