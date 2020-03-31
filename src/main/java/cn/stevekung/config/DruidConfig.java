package cn.stevekung.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    DataSource dataSource(){
        return new DruidDataSource();
    }

    @Bean
    public ServletRegistrationBean statViewServlet(){
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("loginUsername","root");//后台管理界面的登录账号
        initParameters.put("loginPassword","admin");//后台管理界面的登录密码
        //后台允许谁可以访问
        //initParams.put("allow", "localhost")：表示只有本机可以访问
        initParameters.put("allow", "");//为空或者为null时，表示允许所有访问
        //deny：Druid 后台拒绝谁访问
        //initParams.put("deny", "localhost");表示禁止此ip访问
        //设置初始化参数
        //这些参数可以在 com.alibaba.druid.support.http.StatViewServlet 的父类
        // com.alibaba.druid.support.http.ResourceServlet 中找到
        bean.setInitParameters(initParameters);
        return bean;
    }

}
