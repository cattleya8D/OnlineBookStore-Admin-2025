package com.bookstore.service;

import com.bookstore.dao.BookDao;
import com.bookstore.entity.Book;

import java.util.List;

public class BookService {
    private final BookDao bookDao = new BookDao();

    public boolean addBook(Book book) {
        return bookDao.addBook(book);
    }

    public boolean deleteBook(long bookId) {
        return bookDao.deleteBook(bookId);
    }

    public boolean updateBook(Book book) {
        return bookDao.updateBook(book);
    }

    public Book getBookById(long bookId) {
        return bookDao.getBookById(bookId);
    }

    public List<Book> searchBooks(String keyword, Long categoryId, int page, int pageSize) {
        return bookDao.searchBooks(keyword, categoryId, page, pageSize);
    }

    public int getTotalPages(String keyword, Long categoryId, int pageSize) {
        int total = bookDao.countBooks(keyword, categoryId);
        return (total + pageSize - 1) / pageSize;
    }
}