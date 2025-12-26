package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail(), user.getNickname())
        );
    }

    @Transactional(readOnly = true)
    public Page<TodoResponse> getTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail(), todo.getUser().getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail(), user.getNickname()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    /**
     * 검색조건에 따라 todo 목록 조회
     * Entity를 responseDto로 변환
     * @param weather
     * @param createdAt
     * @param modifiedAt
     * @return
     */
    @Transactional(readOnly = true)
    public List<TodoResponse> searchTodos(
            String weather,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        // Repository 호출
        List<Todo> todos = todoRepository.searchTodos(weather, createdAt, modifiedAt);

        // Dto 담을 리스트 생성
        List<TodoResponse> responseList = new ArrayList<>();

        // todo Entity -> TodoResponse 변환
        for (Todo todo : todos) {

            // User 정보도 받을수 있게
            User user = todo.getUser();

            // TodoResponse 객체 생성
            TodoResponse todoResponse = new TodoResponse(
                    todo.getId(),
                    todo.getTitle(),
                    todo.getContents(),
                    todo.getWeather(),
                    new UserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getNickname()
                    ),
                    todo.getCreatedAt(),
                    todo.getModifiedAt()
            );

            // 빈 리스트에 데이터 추가
            responseList.add(todoResponse);
        }

        // 반환
        return responseList;
    }
}
