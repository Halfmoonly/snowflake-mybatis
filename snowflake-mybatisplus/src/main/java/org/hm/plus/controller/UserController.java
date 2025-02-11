package org.hm.plus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.hm.plus.entity.form.UserForm;
import org.hm.plus.entity.param.UserParam;
import org.hm.plus.entity.po.base.UserPo;
import org.hm.plus.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ly
 * @Date: 2024/6/18 21:56
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @RequestMapping(path = "/get/{userId}", method = RequestMethod.GET)
    public UserPo getUser(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @RequestMapping(path = "/update", method = RequestMethod.PUT)
    public Boolean updateUser(@RequestBody UserForm param) {
        UserPo userPo = new UserPo();
        BeanUtils.copyProperties(param, userPo);
        return userService.updateById(userPo);
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public Boolean addUser(@RequestBody UserForm param) {
        UserPo userPo = new UserPo();
        BeanUtils.copyProperties(param, userPo);
        return userService.save(userPo);
    }

    @RequestMapping(path = "/page", method = RequestMethod.POST)
    public IPage<UserPo> page(@RequestBody UserParam param) {
        return userService.pageSearch(param.getPage(), param);
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.DELETE)
    public Boolean delete(@PathVariable Long id) {
        return userService.delete(id);
    }

}
