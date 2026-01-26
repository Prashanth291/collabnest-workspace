package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.Board;

import java.util.List;
import java.util.UUID;

public interface BoardService {

    Board createBoard(UUID workspaceId, String name, Integer position);

    Board getBoard(UUID boardId);

    List<Board> getWorkspaceBoards(UUID workspaceId);

    Board updateBoard(UUID boardId, String name, Integer position);

    void deleteBoard(UUID boardId);
}
