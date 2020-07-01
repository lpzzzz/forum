package com.cn.com.cqucc.forum.entity;

import lombok.Data;

/**
 * 封装分页相关的信息
 *
 * 难点 ：
 * 计算从第几行开始
 * 计算总页数
 * 实现页码的分段显示
 */
@Data
public class Page {
    //当前页码
    private int currentPage = 1;
    // 每页显示条数
    private int limit = 10;
    // 数据总数（用于计算总页数）
    private int rows;
    // 查询路径（用于复用分页链接）
    private String path;

    public void setCurrentPage(int currentPage) {
        if (currentPage >= 1) {
            this.currentPage = currentPage;
        }
    }

    public void setLimit(int limit) {

        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {

        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getLimit() {
        return limit;
    }

    public int getRows() {
        return rows;
    }

    public String getPath() {
        return path;
    }

    /**
     * 获取当前页的起始行
     *
     * @return
     */
    public int getOffset() {
        return (currentPage - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        return (rows % limit ==0) ? rows/limit : (rows / limit) +1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom() {
        return (currentPage - 2) < 1 ? 1 : (currentPage -2);
    }


    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        return (currentPage + 2) > getTotal() ? getTotal() : (currentPage + 2);
    }
}
