package cn.stevekung;

import cn.stevekung.mapper.UserMapper;
import cn.stevekung.pojo.User;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import lombok.extern.log4j.Log4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.*;

@Log4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootMybatisPlusApplicationTests {

	@Autowired
	DataSource dataSource;

	@Test
	public void contextLoads() {
		System.out.println(dataSource.getClass());
		DruidDataSource dataSource = (DruidDataSource) this.dataSource;
		System.out.println(dataSource.getMaxActive());
	}

	@Autowired
	UserMapper userMapper;

	@Test
	public void userMapper(){
		// 参数是一个Wrapper ，条件构造器，这里我们先不用null
		// 查询全部用户
		List<User> users = userMapper.selectList(null);
		// 语法糖
		users.forEach(System.out::println);
	}

	// INSERT INTO user ( id, name ) VALUES ( ?, ? )
	// 自动生成id主键 全局唯一
	// 主键生成策略 默认雪花算法 为long型
	// 需要配置主键 自增
	// 数据库字段一定要是自增！
	// pojo字段@TableId(type = IdType.AUTO)

	@Test
	public void insert(){
		User user = User.builder().name("steve06").build();
		log.info(user);
		int insert = userMapper.insert(user);
		log.info(insert);
	}

	// 通过条件自动拼接动态sql
//	UPDATE user SET name=? WHERE id=?
//	UPDATE user  SET name=?,age=?  WHERE id=?
	@Test
	public void update(){
		User user = User.builder().id(1L).name("steve").age(20).build();
		int i = userMapper.updateById(user);
		log.info(i);
	}

	// 数据库表：gmt_create、gmt_modiﬁed几乎所有的表都要配置上！而且需要自动化！

	// 测试乐观锁成功！
	/*
	乐观锁实现方式：
	取出记录时，获取当前version
	更新时，带上这个version
	执行更新时，set version = newVersion where version = oldVersion
	如果version不对，就更新失败
	 */
	// version会加一
	@Test
	public void testOptimisticLocker(){
		// 1. 查询用户信息，获取version
		User user = userMapper.selectById(1L);
		// 2、修改用户信息
		user.setAge(24);
		user.setName("gj");
		user.setAge(16);
		user.setEmail("gj@163.com");
		// 3.执行更新操作
		userMapper.updateById(user);
	}

	// 测试乐观锁失败！多线程下
	@Test
	public void testOptimisticLocker2(){
		// 线程1
		User user = userMapper.selectById(1L);
		user.setName("steve");
		user.setEmail("steve@163.com");

		// 模拟另外一个线程执行了插队操作
		User user2 = userMapper.selectById(1L);
		user.setName("steve01");
		user.setEmail("steve01@163.com");
		userMapper.updateById(user2);

		// 自旋锁来多次尝试提交！
		// 如果没有乐观锁就会覆盖插队线程的值！
		userMapper.updateById(user);

	}

	// 测试查询
	@Test
	public void selectById(){
		User user = userMapper.selectById(1L);
		System.out.println(user);
	}

	// 测试批量查询
	@Test
	public void selectByBatch(){
		List<User> user = userMapper.selectBatchIds(Arrays.asList(1, 2, 3));
		user.forEach(System.out::println);
	}

	// 测试map条件查询
	@Test
	public void selectByMap(){
		HashMap<String, Object> map = new HashMap<>();
//		map.put("name", "steve");
		map.put("age", 18);
		List<User> users = userMapper.selectByMap(map);
		users.forEach(System.out::println);
	}

	// 测试分页查询
	@Test
	public void testPage() {
		// 参数一：当前页
		// 参数二：页面大小
		// 使用了分页插件之后，所有的分页操作也变得简单

		Page<User> page = new Page<>(2, 2);
		userMapper.selectPage(page, null);

		List<User> records = page.getRecords();
		records.forEach(System.out::println);

		System.out.println(page.getTotal());
	}

	// 测试删除
	@Test
	public void deleteById(){
		userMapper.deleteById(1L);
	}
	@Test
	public void deleteByBatchId(){
		userMapper.deleteBatchIds(Arrays.asList(2L,3L));
	}
	@Test
	public void deleteByMap(){
		HashMap<String, Object> map = new HashMap<>();
		map.put("name", "steve06");
		userMapper.deleteByMap(map);
	}

	// 物理删除：从数据库中直接移除
//	逻辑删除：再数据库中没有被移除，而是通过一个变量来让他失效！deleted = 0 => deleted = 1
	// 删除数据 实际走的是更新擦操作
	// UPDATE user SET deleted=1 WHERE id=?  AND deleted=0
	// 查询的时候会自动过滤被逻辑删除的字段
	@Test
	public void deleteById1(){
		userMapper.deleteById(4L);
	}

	// 性能分析插件
	// 作用：性能分析拦截器，用于输出每条SQL 语句及其执行时间
//	mybatis-plus也提供性能分析插件，如果超过这个时间就停止运行 并抛出相应异常
	/*
	 Time：77 ms - ID：cn.stevekung.mapper.UserMapper.selectList
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
	 */
	@Test
	public void contextTest1(){
		// 条件为null时为全部查询
		List<User> users = userMapper.selectList(null);
		users.forEach(System.out::println);
	}
	// 条件构造器
//	复杂的sql就可以使用它来替代
	/*
	Time：65 ms - ID：cn.stevekung.mapper.UserMapper.selectList
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
        AND name IS NOT NULL
        AND email IS NOT NULL
        AND age >= 12
	 */
	@Test
	public void contextTest2(){
		// 查询name不为空的用户，并且邮箱不为空的用户，年龄大于等于12
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.isNotNull("name")
				.isNotNull("email")
				.ge("age", 12);
		List<User> users = userMapper.selectList(wrapper);
		users.forEach(System.out::println);


	}

	/*
	 Time：66 ms - ID：cn.stevekung.mapper.UserMapper.selectOne
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
        AND name = 'steve05'
	 */
	@Test
	public void contextTest3(){
		// 查询name
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.eq("name","steve05");
		User user = userMapper.selectOne(wrapper);
		System.out.println(user);
	}

	/*
	 Time：46 ms - ID：cn.stevekung.mapper.UserMapper.selectCount
Execute SQL：
    SELECT
        COUNT(1)
    FROM
        user
    WHERE
        deleted=0
        AND age BETWEEN 10 AND 20
	 */
	@Test
	public void contextTest4(){
		// 查询年龄在10 ~ 20 岁之间的用户
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.between("age",10, 20);
		Integer integer = userMapper.selectCount(wrapper);
		System.out.println(integer);
	}

	/*
	 Time：51 ms - ID：cn.stevekung.mapper.UserMapper.selectMaps
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
        AND name NOT LIKE '%a%'
        AND email LIKE 's%'
	 */
	@Test // 模糊查询
	public void contextTest5(){
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		//%t 左和 右 t%
		wrapper.notLike("name", "a")
				.likeRight("email", "s");
		List<Map<String, Object>> maps = userMapper.selectMaps(wrapper);
		maps.forEach(System.out::println);
	}


	// 子查询
	/*
	Time：57 ms - ID：cn.stevekung.mapper.UserMapper.selectObjs
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
        AND id IN (
            select
                id
            from
                user
            where
                id < 3
        )
	 */
	@Test
	public void contextTest6(){
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.inSql("id", "select id from user where id < 3");
		List<Object> objects = userMapper.selectObjs(wrapper);
		objects.forEach(System.out::println);
	}

	// // 通过id进行排序
	/*
	Time：48 ms - ID：cn.stevekung.mapper.UserMapper.selectList
Execute SQL：
    SELECT
        id,
        name,
        age,
        email,
        gmt_create,
        gmt_modify,
        version,
        deleted
    FROM
        user
    WHERE
        deleted=0
    ORDER BY
        id ASC
	 */
	@Test
	public void contextTest7(){
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.orderByAsc("id");
		List<User> users = userMapper.selectList(wrapper);
		users.forEach(System.out::println);
	}

	// 代码自动生成器
	/*
	AutoGenerator 是MyBatis-Plus 的代码生成器，通过AutoGenerator 可以快速生成Entity、
Mapper、Mapper XML、Service、Controller 等各个模块的代码，极大的提升了开发效率
	 */
	public void autoGenerator(){
		// 需要构建一个代码自动生成器对象
		AutoGenerator mpg = new AutoGenerator();
		// 配置策略
		//
		// 1、全局配置
		GlobalConfig gc = new GlobalConfig();
		String projectPath = System.getProperty("user.dir");
		gc.setOutputDir(projectPath + "/src/main/java");
		gc.setAuthor("steve");
		gc.setOpen(false);
		gc.setFileOverride(false); // 是否覆盖
		gc.setServiceName("%sService"); // 去Service的I前缀
		gc.setIdType(IdType.ID_WORKER);
		gc.setDateType(DateType.ONLY_DATE);
		gc.setSwagger2(true);
		mpg.setGlobalConfig(gc);

		//2、设置数据源

		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setUrl("jdbc:mysql://localhost:3306/mybatis_plus?useSSL=true&useUnicode=true&serverTimezone=GMT%2B8&characterEncoding=utf8");
		dsc.setDriverName("com.mysql.cj.jdbc.Driver");
		dsc.setUsername("root");
		dsc.setPassword("admin");
		dsc.setDbType(DbType.MYSQL);
		mpg.setDataSource(dsc);

		//3、包的配置
		PackageConfig pc = new PackageConfig();
		pc.setModuleName("user");
		pc.setParent("cn.stevekung");
		pc.setEntity("pojo");
		pc.setMapper("mapper");
		pc.setService("service");
		pc.setController("controller");
		mpg.setPackageInfo(pc);

		//4、策略配置
		StrategyConfig strategy = new StrategyConfig();
		strategy.setInclude("user");// 设置要映射的表名
		strategy.setNaming(NamingStrategy.underline_to_camel);
		strategy.setColumnNaming(NamingStrategy.underline_to_camel);
		strategy.setEntityLombokModel(true);// 自动lombok
		strategy.setLogicDeleteFieldName("deleted");
		// 自动填充配置
		TableFill gmtCreate = new TableFill("gmt_create", FieldFill.INSERT);
		TableFill gmtModify = new TableFill("gmt_modify", FieldFill.INSERT_UPDATE);
		ArrayList<TableFill> tableFills = new ArrayList<>();
		tableFills.add(gmtCreate);
		tableFills.add(gmtModify);
		strategy.setTableFillList(tableFills);
		// 乐 观 锁
		strategy.setVersionFieldName("version");
		strategy.setRestControllerStyle(true);
		strategy.setControllerMappingHyphenStyle(true);// localhost:8080/hello_id_2
		mpg.setStrategy(strategy);

		mpg.execute(); //执行
	}







}
