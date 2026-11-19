package com.Library.mse.service;

import com.Library.mse.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BookService {

    private final Map<Integer, Book> books = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public Book addBook(Book book) {
        int id = idGenerator.incrementAndGet();
        book.setId(id);
        books.put(id, book);
        return book;
    }

    public List<Book> getAllBooks() {
        return List.copyOf(books.values());
    }

    public Book getBookById(Integer id) {
        return books.get(id);
    }

    public Book updateBook(Integer id, Book updatedBook) {
        Book existingBook = books.get(id);
        if (existingBook == null) {
            return null;
        }
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setCategory(updatedBook.getCategory());
        existingBook.setAvailableCopies(updatedBook.getAvailableCopies());
        return existingBook;
    }

    public boolean deleteBook(Integer id) {
        return books.remove(id) != null;
    }
}
