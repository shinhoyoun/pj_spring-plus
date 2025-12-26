package org.example.expert.domain.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class TodoController {

//    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;
    private final TodoRepository todoRepository;

    @PostMapping("/todos")
    public ResponseEntity<TodoSaveResponse> saveTodo(
            @Auth AuthUser authUser,
            @Valid @RequestBody TodoSaveRequest todoSaveRequest
    ) {
        return ResponseEntity.ok(todoService.saveTodo(authUser, todoSaveRequest));
    }

    @GetMapping("/todos")
    public ResponseEntity<Page<TodoResponse>> getTodos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(todoService.getTodos(page, size));
    }

    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
        return ResponseEntity.ok(todoService.getTodo(todoId));
    }

    /**
     * 클라이언트 요청 받는 컨트롤러다
     * 검색 조검을 파라미터로 받고
     * weather, createdAt, modifiedAt (없어도됨)
     * 그 후에 서비스에 위임
     */
    @GetMapping("/todos/search")
    public ResponseEntity<List<TodoResponse>> searchTodos(
            // 날씨 조건 , 업으면 null
            @RequestParam(required = false) String weather,
            // 생성일 날짜, 없으면 null
            @RequestParam(required = false) LocalDateTime createdAt,
            // 수정일 날짜, 없으면 null
            @RequestParam(required = false) LocalDateTime modifiedAt
    ) {

        log.info("TodoController - searchTodos - ");

        // service 호출 -> 받은 데이터 그대로 전달
        List<TodoResponse> result = todoService.searchTodos(weather, createdAt, modifiedAt);

        // Http 200 과 데이터 반환
        return ResponseEntity.ok(result);

    }
}
