package org.hm.plus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.hm.plus.entity.vo.LoginUserVo;
import org.hm.plus.holder.UserContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: lyflexi
 * @project: mybatis-plus-practice
 * @Date: 2024/9/10 21:29
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUserVo context = UserContextHolder.getInstance().getContext();
        log.info("开始插入填充...");
        this.strictInsertFill(metaObject, "addTime", LocalDateTime.class, LocalDateTime.now())
                .strictInsertFill(metaObject, "editTime", LocalDateTime.class, LocalDateTime.now())
                .strictInsertFill(metaObject, "addUserCode", String.class, context.getUserCode())
                .strictInsertFill(metaObject, "addUserName", String.class, context.getUserName())
                .strictInsertFill(metaObject, "editUserCode", String.class, context.getUserCode())
                .strictInsertFill(metaObject, "editUserName", String.class, context.getUserName());
        log.info("插入填充完成...");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUserVo context = UserContextHolder.getInstance().getContext();
        log.info("开始更新填充...");
        this.strictUpdateFill(metaObject, "editTime", LocalDateTime.class, LocalDateTime.now())
                .strictUpdateFill(metaObject, "editUserCode", String.class, context.getUserCode())
                .strictUpdateFill(metaObject, "editUserName", String.class, context.getUserName());
        log.info("更新填充完成...");
    }
}