package com.barq.interview.service;

import com.barq.interview.model.Todo;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoService {

    private static final String FILE_PATH = " src/main/resources/todos.dat"; // Path to the file where todos will be saved
    private List<Todo> todoList = new ArrayList<>();

    public TodoService() {
        loadFromFile(); // Load todos from file when the service is initialized
    }

    // Create a new Todo
    public Todo create(Todo todo) {
        todo.setId((long) (todoList.size() + 1)); // Simple ID generation logic (optional)
        todoList.add(todo);
        saveToFile(); // Save todos to file after adding a new one
        return todo;
    }

    // Retrieve all Todos
    public List<Todo> getList() {
        return todoList;
    }

    // Update an existing Todo
    public Todo update(Long id, Todo updatedTodo) {
        for (Todo todo : todoList) {
            if (todo.getId().equals(id)) {
                todo.setDescription(updatedTodo.getDescription());
                saveToFile(); // Save todos to file after updating
                return todo;
            }
        }
        return null; // Or throw an exception if the Todo is not found
    }

    // Delete a Todo by ID
    public boolean delete(Long id) {
        boolean removed = todoList.removeIf(todo -> todo.getId().equals(id));
        if (removed) {
            saveToFile(); // Save todos to file after deletion
        }
        return removed;
    }

    // Save the current list of Todos to a file
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(todoList); // Write the list of Todos to the file
        } catch (IOException e) {
            throw new RuntimeException("Error saving todos to file", e);
        }
    }

    // Load the list of Todos from the file
    private void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            todoList = (List<Todo>) ois.readObject(); // Read the list from the file
        } catch (FileNotFoundException e) {
            // If file does not exist, start with an empty list
            todoList = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading todos from file", e);
        }
    }
}
