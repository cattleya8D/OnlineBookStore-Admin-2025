package com.bookstore.dao;

import com.bookstore.entity.Book;
import com.bookstore.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    // 1. 新增图书
    public boolean addBook(Book book) {
        String sql = "INSERT INTO Books (isbn, title, author, publisher, publish_year, price, stock, category_id, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.isbn());
            ps.setString(2, book.title());
            ps.setString(3, book.author());
            ps.setString(4, book.publisher());
            ps.setInt(5, book.publishYear());
            ps.setBigDecimal(6, book.price());
            ps.setInt(7, book.stock());
            ps.setLong(8, book.categoryId());
            ps.setString(9, book.description());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 删除图书（根据ID）
    public boolean deleteBook(long bookId) {
        String sql = "DELETE FROM Books WHERE book_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. 修改图书（全字段更新）
    public boolean updateBook(Book book) {
        String sql = "UPDATE Books SET isbn=?, title=?, author=?, publisher=?, publish_year=?, price=?, stock=?, " +
                "category_id=?, description=?, update_time=NOW() WHERE book_id=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.isbn());
            ps.setString(2, book.title());
            ps.setString(3, book.author());
            ps.setString(4, book.publisher());
            ps.setInt(5, book.publishYear());
            ps.setBigDecimal(6, book.price());
            ps.setInt(7, book.stock());
            ps.setLong(8, book.categoryId());
            ps.setString(9, book.description());
            ps.setLong(10, book.bookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. 根据ID查询单本图书
    public Book getBookById(long bookId) {
        String sql = "SELECT * FROM Books WHERE book_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getLong("book_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getInt("publish_year"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock"),
                            rs.getLong("category_id"),
                            rs.getString("description"),
                            rs.getString("cover_url"),
                            rs.getTimestamp("create_time").toLocalDateTime(),
                            rs.getTimestamp("update_time").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5. 分页模糊查询（核心业务：书名/作者/ISBN/分类）
    public List<Book> searchBooks(String keyword, Long categoryId, int page, int pageSize) {
        List<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (isbn LIKE ? OR title LIKE ? OR author LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        sql.append(" ORDER BY book_id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Book(
                            rs.getLong("book_id"),
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getInt("publish_year"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock"),
                            rs.getLong("category_id"),
                            rs.getString("description"),
                            rs.getString("cover_url"),
                            rs.getTimestamp("create_time").toLocalDateTime(),
                            rs.getTimestamp("update_time").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 6. 查询符合条件的总记录数（用于分页总页数）
    public int countBooks(String keyword, Long categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (isbn LIKE ? OR title LIKE ? OR author LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}