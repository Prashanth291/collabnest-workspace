package com.collabnest.backend.service;

import com.collabnest.backend.domain.entity.BoardColumn;

import java.util.List;
import java.util.UUID;

public interface ColumnService {

    BoardColumn createColumn(UUID boardId, String name, Integer position);

    BoardColumn getColumn(UUID columnId);

    List<BoardColumn> getBoardColumns(UUID boardId);

    BoardColumn updateColumn(UUID columnId, String name, Integer position);

    void deleteColumn(UUID columnId);
}
