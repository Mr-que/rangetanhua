package com.itheima.test;

import static io.vavr.API.printf;

public class Sample {


    public static void main(String[] args_) throws Exception {
//        String str = "abab";
//        HashMap<String,String> map = new HashMap<>();
//        map.put("a", "1");
//        map.put("a", "2");
//        map.put("b", "3");
//        map.put("c", "3");
//        map.put("d", null);
//        map.put(null, "4");
//        map.put(null, "6");
//        System.out.println(map);
        int i=0;
        int a[]={3,6,4,8,5,6};
        do{
            a[i]-=3;
        }while(a[i++]<4);
        for(i=0;i<6;i++)
        {
            printf("%d",a[i]);
        }
    }
}