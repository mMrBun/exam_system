import java.sql.*;
import java.util.*;

import static java.sql.DriverManager.println;


public class Mysql_connection {


    /*
    * 链接函数
    */
    public static Connection connection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://119.45.127.124:3306/exam_system?characterEncoding=utf8&useSSL=false";
        String user= "root";
        String passwd= "123456";
        return DriverManager.getConnection(url,user,passwd);
    }
    /*
     *执行查询操作返回Result结果集
     * 1:sql 需要执行的sql语句
     */
    public static ResultSet select(String sql) throws Exception {
        Connection con=connection();
        ResultSet rs = null;
        if(con!=null)
        {
            Statement state=con.createStatement();
            rs=state.executeQuery(sql);
        }
        return rs;
    }
    /*
     *执行数据插入删除更新操作，返回受影响行数(int)
     * 1:sql 需要执行的sql语句
     */
    public static int update(String sql)throws Exception{
        Connection con=connection();
        int result=0;
        if(con!=null)
        {
            Statement state=con.createStatement();
            result=state.executeUpdate(sql);
        }
        return result;
    }


    public static void createQuestion(List s_answer,List answer,  Scanner input) throws Exception {
        int index=0,ques_count=0,i,number=0,last_number=0;
        Random ran =new Random();
        ResultSet resultSet,res_option,res_answer;
        String ques_sql="SELECT ques_count FROM config";
        resultSet=Mysql_connection.select(ques_sql);
        while(resultSet.next())
        {
            ques_count=Integer.parseInt(resultSet.getString(1));
        }
        for(i=0;i<ques_count;i++)
        {
            number=ran.nextInt(35)+1;
            if(number==last_number)
            {
                i--;
                continue;
            }
            String sql="SELECT q.topic\n" +
                    "from question q " +
                    "where id="+number+"";
            resultSet= Mysql_connection.select(sql);
            while(resultSet.next())
            {
                System.out.println(""+(i+1)+"."+resultSet.getString(1));
                String val="SELECT o.`options`,o.val\n" +
                        "from `option` o\n" +
                        "where o.id="+number+"";
                res_option=select(val);
                while(res_option.next())
                {
                    System.out.println(res_option.getString(2)+":"+res_option.getString(1));
                }
                String ans_sql="select ans.`key`\n" +
                        "from answer ans\n" +
                        "where id="+number+"";
                res_answer=select(ans_sql);
                while (res_answer.next())
                {
                    answer.add(res_answer.getString(1));
                }
                System.out.println("请输入你的答案:");
                s_answer.add(input.next().toUpperCase(Locale.ROOT));
            }
            last_number=number;
        }
    }
    public static int record_Score(String name,List s_answer,List answer ) throws Exception {
        int count = 0;
        for(int i=0;i<s_answer.size();i++)
        {
            if(s_answer.get(i).equals(answer.get(i)))
            {
                count++;
            }
        }
        String sql="insert into s_score(name,score) values(?,?)";
        PreparedStatement tool=connection().prepareStatement(sql);
        tool.setString(1,name);
        tool.setFloat(2,(float)(count/s_answer.size()*100));
        System.out.println("分数:"+(float)(count/s_answer.size()*100));
        return tool.executeUpdate();
    }
    public static void Login(String name,String pwd) throws Exception {
        String sql="select count(name), flag from user_info where name=? and pwd=? group by id";
        PreparedStatement tool=connection().prepareStatement(sql);
        tool.setString(1,name);
        tool.setString(2,pwd);
        ResultSet resultSet=tool.executeQuery();
        while(resultSet.next())
        {
            Main.count=resultSet.getInt(1);
            Main.flag=resultSet.getInt(2);
        }
        if(Main.count>0)
        {
            if (Main.flag == 0) {
                System.out.println("登陆成功！欢迎您，" + name);

            } else {
                System.out.println("登陆成功！" + name + "，考试马上开始，请答题：");
            }
        }
        else {
            System.out.println("用户名或密码错误");
        }
    }
    public static int Update_question(Scanner input) throws Exception {
        String index,topic,option,answer,topic_type;
        int topic_id = 0,inner_result=0;
        PreparedStatement pst;
        ResultSet res;
        show_all_questions();
        System.out.println("请选择操作:\n" +
                "1.增加题目\n" +
                "2.删除题目\n" +
                "3.更改题目\n");
        index=input.next();
        switch (index)
        {
            case"1":
                System.out.println("请输入题目:");
                topic=input.next();
                System.out.println("请输入题目类型(1:单选题2:多选题)");
                topic_type=input.next();
                String inner_sql="insert into question(topic,type) values(?,?)";
                pst=connection().prepareStatement(inner_sql);
                pst.setString(1,topic);
                pst.setInt(2,Integer.parseInt(topic_type));
                inner_result+=pst.executeUpdate();
                res=select("select id from question where topic="+"'"+topic+"'");
                while(res.next())
                {
                    topic_id=res.getInt(1);
                }
                for(int i=0;i<4;i++)
                {
                    System.out.println("请输入选项");
                    option=input.next();
                    pst=connection().prepareStatement("insert into `option` VALUES(?,?,?)");
                    pst.setInt(1,topic_id);
                    pst.setString(2,option);
                    if(i==0) pst.setString(3,"A");
                    if(i==1) pst.setString(3,"B");
                    if(i==2) pst.setString(3,"C");
                    if(i==3) pst.setString(3,"D");
                    inner_result+=pst.executeUpdate();
                }
                System.out.println("请输入答案(多选用,隔开):");
                answer=input.next();
                pst=connection().prepareStatement("insert into `answer` VALUES(?,?)");
                pst.setInt(1,topic_id);
                pst.setString(2,answer.toUpperCase(Locale.ROOT));
                inner_result+=pst.executeUpdate();
                if(inner_result>2) System.out.println("添加成功");
                inner_result=0;
                break;
            case"2":
                System.out.println("请输入要删除的题号:");
                pst=connection().prepareStatement("delete from question where id=?");
                pst.setInt(1,input.nextInt());
                inner_result=pst.executeUpdate();
                pst=connection().prepareStatement("ALTER table question  AUTO_INCREMENT=1;");
                pst.executeUpdate();
                if(inner_result>0)System.out.println("删除成功");
                inner_result=0;
                break;
            case"3":
                System.out.println("请输入要修改的题号");
                topic_id=input.nextInt();
                System.out.println("请输入题目:");
                topic=input.next();
                System.out.println("请输入题目类型(1:单选题2:多选题)");
                topic_type=input.next();
                pst=connection().prepareStatement("update question set topic=?,type=? where id=?");
                pst.setString(1,topic);
                pst.setInt(2,Integer.parseInt(topic_type));
                pst.setInt(3,topic_id);
                inner_result+=pst.executeUpdate();
                for(int i=0;i<4;i++)
                {
                    System.out.println("请输入选项");
                    option=input.next();
                    pst=connection().prepareStatement("update `option` set options=?,val=? where id=?");
                    pst.setString(1,option);
                    if(i==0) pst.setString(2,"A");
                    if(i==1) pst.setString(2,"B");
                    if(i==2) pst.setString(2,"C");
                    if(i==3) pst.setString(2,"D");
                    pst.setInt(3,topic_id);
                    inner_result+=pst.executeUpdate();
                }
                System.out.println("请输入答案(多选用,隔开):");
                answer=input.next();
                pst=connection().prepareStatement("update answer set `key`=? where id=?");
                pst.setString(1,answer.toUpperCase(Locale.ROOT));
                pst.setInt(2,topic_id);
                inner_result+=pst.executeUpdate();
                if(inner_result>2) System.out.println("修改成功");
                inner_result=0;
                break;
        }
        return 0;
    }
    public static void change_ques_count(Scanner input) throws Exception {
        System.out.println("请输入考试题目数量");
        String sql="update config set ques_count=?";
        PreparedStatement pst=connection().prepareStatement(sql);
        pst.setInt(1,input.nextInt());
        if(pst.executeUpdate()>0)
            System.out.println("更改成功");
    }
    public static void Select_score(Scanner input) throws Exception {
        System.out.println("请输入考生姓名");
        PreparedStatement pst=connection().prepareStatement("select `name`,score from s_score where `name`=?");
        pst.setString(1,input.next());
        ResultSet res=pst.executeQuery();
        while(res.next())
        {
            System.out.print(res.getString(1)+"    ");
            System.out.println(res.getFloat(2));
        }
    }
    public static void statistics() throws Exception {
        int count = 0;
        PreparedStatement pst=connection().prepareStatement("select `name`,score from s_score");
        ResultSet res=pst.executeQuery();
        while(res.next())
        {
            System.out.print(res.getString(1)+"    ");
            System.out.println(res.getFloat(2));
        }
        pst=connection().prepareStatement("select max(score)from s_score");
        res=pst.executeQuery();
        while(res.next())
        {
            System.out.println("最高分为:"+res.getFloat(1));
        }
        pst=connection().prepareStatement("select min(score)from s_score");
        res=pst.executeQuery();
        while(res.next())
        {
            System.out.println("最低分为:"+res.getFloat(1));
        }
        pst=connection().prepareStatement("select avg(score)from s_score");
        res=pst.executeQuery();
        while(res.next())
        {
            System.out.println("平均分为:"+res.getFloat(1));
        }
        pst=connection().prepareStatement("select count(`name`) from s_score");
        res=pst.executeQuery();
        while(res.next())
        {
            count=res.getInt(1);
        }
        pst=connection().prepareStatement("select \n" +
                "sum(case when score between 90 and 100 then 1.0 else 0.0 end) as A,\n" +
                "sum(case when score between 80 and 89 then 1.0 else 0.0 end)\n" +
                " as B,\n" +
                "sum(case when score between 70 and 79 then 1.0 else 0.0 end) as C,\n" +
                "sum(case when score between 60 and 69 then 1.0 else 0.0 end) as D,\n" +
                "sum(case when score<60 then 1.0 else 0.0 end) as E\n" +
                "from s_score");
        res=pst.executeQuery();
        while(res.next())
        {
            System.out.println("90-100:"+res.getFloat(1)/(float)count*100+"%");
            System.out.println("80-89:"+res.getFloat(2)/(float)count*100+"%");
            System.out.println("70-79:"+res.getFloat(3)/(float)count*100+"%");
            System.out.println("60-69:"+res.getFloat(4)/(float)count*100+"%");
            System.out.println("0-59:"+res.getFloat(5)/(float)count*100+"%");
        }

    }
    public static void show_all_questions() throws Exception {
        String all_sql="SELECT id,topic FROM `question`";
        ResultSet resultSet=select(all_sql);
        while (resultSet.next())
        {
            System.out.print(resultSet.getString(1));
            System.out.println(resultSet.getString(2));
        }
    }
    public static void main(String[] args) throws Exception {
//        Scanner input=new Scanner(System.in);
//        Select_score(input);
        statistics();

    }

}
