package com.tanhua.server.interceptor;

import com.tanhua.model.domain.User;

public class UserHolder {

    private static ThreadLocal<User> tl = new ThreadLocal<>();

    //将用户对象，存入ThreadLocal
    public static void set(User user) {
        tl.set(user);
    }

    //从ThreadLocal中将用户对象取出
    public static User get() {
        return tl.get();
    }

    public static Long getUserId() {
        return tl.get().getId();
    }

    public static String getUserMobile() {
        return tl.get().getMobile();
    }

    public static void remove() {
        tl.remove();
    }
}
