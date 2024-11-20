package com.barq.interview;

import com.barq.interview.model.Todo;
import com.barq.interview.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // POST - Create a new Todo
    @PostMapping("/create")
    public Todo createTodo(@RequestBody Todo todo) {
        return todoService.create(todo);
    }

    // GET - Retrieve all Todos
    @GetMapping("/list")
    public List<Todo> getAllTodos() {
        return todoService.getList();
    }

    // PUT - Update an existing Todo
    @PutMapping("/update/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        return todoService.update(id, todo);
    }

    // DELETE - Delete a Todo by ID
    @DeleteMapping("/delete/{id}")
    public String deleteTodo(@PathVariable Long id) {
        boolean isDeleted = todoService.delete(id);
        if (isDeleted) {
            return "Todo with ID " + id + " deleted successfully.";
        } else {
            return "Todo with ID " + id + " not found.";
        }
    }
}
