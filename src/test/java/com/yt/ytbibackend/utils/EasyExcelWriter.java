package com.yt.ytbibackend.utils;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.EasyExcel;
import java.util.ArrayList;
import java.util.List;

public class EasyExcelWriter {
    public static void main(String[] args) {
        // 创建数据
        List<ParentClass> data = new ArrayList<>();
        data.add(new ParentClass("Parent1", new ChildClass("Child1")));
        data.add(new ParentClass("Parent2", new ChildClass("Child2")));

        // 写入Excel
        String fileName = "d://output.xlsx";
        EasyExcel.write(fileName, ParentClass.class).sheet("Sheet1").doWrite(data);
    }

    // 定义嵌套类
    public static class ChildClass {
        @ExcelProperty("Child Name")
        private String childName;

        public ChildClass(String childName) {
            this.childName = childName;
        }

        // Getter and Setter
    }

    // 定义父类
    public static class ParentClass {
        @ExcelProperty("Parent Name")
        private String parentName;

        @ExcelIgnore // 忽略嵌套类字段的导出
        private ChildClass child;

        public ParentClass(String parentName, ChildClass child) {
            this.parentName = parentName;
            this.child = child;
        }

        // Getter and Setter
    }
}
