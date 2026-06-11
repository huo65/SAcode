package com.DB.DBmarket.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  //get&set方法
@NoArgsConstructor//无参构造
@AllArgsConstructor//有参构造
public class Result {
    //状态码
    private int code;
    //返回数据
    private Object data;
    //响应信息
    private String msg;

    //成功响应的四种形式
    public  static Result  success(){
            return new Result(1,null,"Success!");
    }
    public static Result success(Object data){
        return new Result(1,data,"Success");
    }
    public static Result success(String msg){
        return new Result(1,null,msg);
    }
    public static Result success(Object data,String msg){
        return new Result(1,data,msg);
    }

    //失败响应
    //要求必须给出错误信息
    public static Result error(String msg){
        return new Result(0,null,msg);
    }
    //没有权限返回的错误信息
    public static Result noAuthorization(){
        String msg="请登录后查看!";
        return new Result(-1,null,msg);
    }
}
