package cn.stevekung.mapper;

import cn.stevekung.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
// 在对应的Mapper上面继承基本的类BaseMapper
public interface UserMapper extends BaseMapper<User> {
}
