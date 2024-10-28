package com.yfckevin.common.utils;

public class MemberContext {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    public static void setMember(String memberId) {
        tl.set(memberId);
    }

    public static String getMember(){
        return tl.get();
    }

    public static void removeMember (){
        tl.remove();
    }
}
