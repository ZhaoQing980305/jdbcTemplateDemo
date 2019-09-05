package com.assiainfo.controller;

import com.assiainfo.entity.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class jdbcHandler {
    @Autowired
    private JdbcTemplate template;

    /**
     * 导入数据至数据库
     * @return
     * @throws Exception
     */
    @GetMapping("/input")
    public Object input() throws Exception {

        String sql = "INSERT INTO dept VALUES (?,?)";
        //读取已有文件中的数据加入数据库表中
        InputStream inputStream = new FileInputStream("D:\\桌面\\testNumber\\test.txt");
        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader bufferReader = new BufferedReader(inputReader);
        // 读取一行
        String line;
        //开始时间
        Long startTime = System.currentTimeMillis();
        Test test = new Test();
        final List<Test> list = new ArrayList<Test>();
        while ((line = bufferReader.readLine()) != null) {
            // 按照相应规则截取字符串
            String a[] = line.split("\t");
            String s = "";
            for (int i = 1; i < a.length; i++) {
                s += a[i] + " ";
            }
            // 去掉字符串开头和结尾的空格
            final String ss = s.trim();
            test.setId(Integer.parseInt(a[0]));
            test.setValue(Integer.parseInt(ss));
            list.add(new Test(test.getId(), test.getValue()));
        }
        template.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1, list.get(i).getId());
                preparedStatement.setInt(2, list.get(i).getValue());
            }
            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        //结束时间
        Long endTime = System.currentTimeMillis();
        System.out.println("导入完成！");
        return "导入数据" + list.size() + "条,用时：" + (endTime - startTime) + "毫秒";
    }

    /**
     * 从数据库中导出数据
     * @return
     * @throws Exception
     */
    @RequestMapping("/output")
    public String output() throws Exception {
        //sql语句
        String sql = "select * from test600";
        //导出数据所在位置及文件名
        File file = new File("D:\\桌面\\testNumber\\testnew.txt");
        //开始时间
        Long startTime = System.currentTimeMillis();
        //判断文件是否存在
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileWriter fileWriter = new FileWriter(file);
        //查询结果并封装结果集
        List list = template.query(sql,
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Test test = new Test();
                        test.setId(rs.getInt("id"));
                        test.setValue(rs.getInt("value"));
                        String result = test.getId() + "\t" + test.getValue()+"\r\n";
                        try {
                            //将结果导出到指定文件
                            fileWriter.write(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }
                });
        fileWriter.flush();
        //结束时间
        Long endTime = System.currentTimeMillis();
        System.out.println("用时：" + (endTime - startTime) + "毫秒");
        return "已导出："+list.size()+"条，用时：" + (endTime - startTime) + "毫秒";
    }
}
