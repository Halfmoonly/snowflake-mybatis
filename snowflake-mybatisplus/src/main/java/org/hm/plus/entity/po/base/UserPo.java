package org.hm.plus.entity.po.base;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: ly
 * @Date: 2024/6/18 21:52
 */
@Data
//application.yml中的currentSchema=lyschema没有生效，原因未知
@TableName("lyschema.user")
public class UserPo extends BasePo {
    Long id;
    String name;
    String code;
    Integer age;
    @TableLogic
    Integer dataStatus;
}
