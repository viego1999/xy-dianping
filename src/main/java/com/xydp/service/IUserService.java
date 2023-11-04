package com.xydp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xydp.dto.LoginFormDTO;
import com.xydp.dto.Result;
import com.xydp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  用户服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
