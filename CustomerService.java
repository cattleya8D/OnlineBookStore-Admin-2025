package com.bookstore.service;

import com.bookstore.dao.CustomerDao;
import com.bookstore.entity.Customer;

import java.util.List;

public class CustomerService {
    private final CustomerDao customerDao = new CustomerDao();

    public boolean addCustomer(Customer customer) {
        return customerDao.addCustomer(customer);
    }

    public boolean deleteCustomer(long customerId) {
        return customerDao.deleteCustomer(customerId);
    }

    public boolean updateCustomer(Customer customer) {
        return customerDao.updateCustomer(customer);
    }

    public Customer getCustomerById(long customerId) {
        return customerDao.getCustomerById(customerId);
    }

    public List<Customer> searchCustomers(String keyword, int page, int pageSize) {
        return customerDao.searchCustomers(keyword, page, pageSize);
    }

    public int getTotalPages(String keyword, int pageSize) {
        int total = customerDao.countCustomers(keyword);
        return (total + pageSize - 1) / pageSize;
    }
}