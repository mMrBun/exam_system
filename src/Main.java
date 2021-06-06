import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static int flag;
    public static int count=0;
    public static String menu_index;
    private static String name;
    private static String pwd;
    private static Scanner input=new Scanner(System.in);
    private static List<String> user_answer=new ArrayList<String>();
    private static List<String> norm_answer=new ArrayList<String>();


    public static void main(String[] args) throws Exception {
       function();
    }
    public static void function() throws Exception {
        while(true)
        {
            println("欢迎进入考试系统,请输入姓名:");
            name = input.next();
            println("请输入密码:");
            pwd = input.next();
            Mysql_connection.Login(name,pwd);
            if(count>0)
            {
                count=0;
                flag:while(true)
                {
                    if(flag==1)
                    {
                        Mysql_connection.createQuestion(user_answer,norm_answer,input);
                        if(Mysql_connection.record_Score(name,user_answer,norm_answer)>0)
                        {
//                            println("记录成功");
                            break;
                        }
                    }
                    else
                    {
                        println("1.题库管理\n" +   //增删改查
                                "2.考试管理\n" +   //设置题目数量
                                "3.成绩查询\n" +   //根据姓名查询成绩
                                "4.成绩统计\n" +   //max,min,avg
                                "5.退出\n");
                        menu_index=input.next();
                        switch (menu_index)
                        {
                            case"1":
                                Mysql_connection.Update_question(input);
                                break;
                            case"2":
                                Mysql_connection.change_ques_count(input);
                                break;
                            case"3":
                                Mysql_connection.Select_score(input);
                                break;
                            case"4":
                                Mysql_connection.statistics();
                                break;
                            case"5":
                                break flag;
                            default:
                                System.out.println("输入有误");
                                break;
                        }
                    }
                }
            }
        }
    }

    public static void println(String context)
    {
        System.out.println(context);
    }
    public static void print(String context)
    {
        System.out.print(context);
    }
}
