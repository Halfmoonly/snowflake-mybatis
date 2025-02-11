package org.hm.plus.entity.po.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: lyflexi
 * @project: mybatis-plus-practice
 * @Date: 2024/9/10 21:34
 */
@Data
public class BasePo {
    @TableField(fill = FieldFill.INSERT)
    LocalDateTime addTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime editTime;
    @TableField(fill = FieldFill.INSERT)
    String addUserCode;
    @TableField(fill = FieldFill.INSERT)
    String addUserName;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    String editUserCode;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    String editUserName;
}
