package com.bookstore.test;

import com.bookstore.entity.Book;
import com.bookstore.service.BookService;

import java.math.BigDecimal;
import java.util.List;

public class TestBookCrud {
    public static void main(String[] args) {
        BookService service = new BookService();

        // 1. 测试新增
        Book newBook = new Book(
                null, "978-7-121-12345-6", "测试图书新增", "测试作者", "测试出版社",
                2026, new BigDecimal("88.88"), 999, 1L, "测试描述", null, null, null
        );
        boolean added = service.addBook(newBook);
        System.out.println("新增图书: " + (added ? "成功" : "失败"));

        // 2. 测试查询所有（分页第1页，每页10条）
        List<Book> list = service.searchBooks(null, null, 1, 10);
        System.out.println("查询到 " + list.size() + " 条图书:");
        for (Book b : list) {
            System.out.println(b.title() + " - " + b.price() + "元 - 库存:" + b.stock());
        }

        // 3. 测试模糊查询（书名含“Java”）
        List<Book> javaBooks = service.searchBooks("Java", null, 1, 5);
        System.out.println("含'Java'的书: " + javaBooks.size() + " 本");

        // 4. 测试分类查询（计算机类，假设categoryId=1）
        List<Book> catBooks = service.searchBooks(null, 1L, 1, 5);
        System.out.println("计算机类图书前5本: " + catBooks.size() + " 本");

        // 5. 测试总页数
        int pages = service.getTotalPages("Java", null, 5);
        System.out.println("含'Java'的图书总页数（每页5条）: " + pages);
    }
}