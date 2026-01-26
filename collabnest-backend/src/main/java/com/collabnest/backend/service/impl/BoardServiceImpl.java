package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Board;
import com.collabnest.backend.repository.BoardRepository;
import com.collabnest.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public Board createBoard(UUID workspaceId, String name, Integer position) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public Board getBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
    }

    @Override
    public List<Board> getWorkspaceBoards(UUID workspaceId) {
        return boardRepository.findByWorkspaceIdOrderByPositionAsc(workspaceId);
    }

    @Override
    public Board updateBoard(UUID boardId, String name, Integer position) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }

    @Override
    public void deleteBoard(UUID boardId) {
        throw new UnsupportedOperationException("Implemented in Step 6");
    }
}
