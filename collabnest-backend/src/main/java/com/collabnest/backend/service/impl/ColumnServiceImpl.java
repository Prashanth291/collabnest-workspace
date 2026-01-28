package com.collabnest.backend.service.impl;

import com.collabnest.backend.domain.entity.Board;
import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.repository.BoardColumnRepository;
import com.collabnest.backend.repository.BoardRepository;
import com.collabnest.backend.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColumnServiceImpl implements ColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public BoardColumn createColumn(UUID boardId, String name, Integer position) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        
        BoardColumn column = BoardColumn.builder()
                .board(board)
                .name(name)
                .position(position)
                .build();
        
        return columnRepository.save(column);
    }

    @Override
    public BoardColumn getColumn(UUID columnId) {
        return columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));
    }

    @Override
    public List<BoardColumn> getBoardColumns(UUID boardId) {
        return columnRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    @Override
    @Transactional
    public BoardColumn updateColumn(UUID columnId, String name, Integer position) {
        BoardColumn column = getColumn(columnId);
        
        if (name != null) {
            column.setName(name);
        }
        if (position != null) {
            column.setPosition(position);
        }
        
        return columnRepository.save(column);
    }

    @Override
    @Transactional
    public void deleteColumn(UUID columnId) {
        BoardColumn column = getColumn(columnId);
        columnRepository.delete(column);
    }
}
