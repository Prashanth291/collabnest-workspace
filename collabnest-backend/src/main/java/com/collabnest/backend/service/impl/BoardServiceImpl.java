package com.collabnest.backend.service.impl;

import com.collabnest.backend.activity.ActivityLogService;
import com.collabnest.backend.domain.entity.Board;
import com.collabnest.backend.domain.entity.Workspace;
import com.collabnest.backend.domain.enums.ActivityType;
import com.collabnest.backend.exception.ResourceNotFoundException;
import com.collabnest.backend.repository.BoardRepository;
import com.collabnest.backend.repository.WorkspaceRepository;
import com.collabnest.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public Board createBoard(UUID workspaceId, String name, Integer position) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        Board board = Board.builder()
                .workspace(workspace)
                .name(name)
                .position(position)
                .build();

        return boardRepository.save(board);
    }

    @Override
    public Board getBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }

    @Override
    public List<Board> getWorkspaceBoards(UUID workspaceId) {
        return boardRepository.findByWorkspaceIdOrderByPositionAsc(workspaceId);
    }

    @Override
    @Transactional
    public Board updateBoard(UUID boardId, String name, Integer position) {
        Board board = getBoard(boardId);

        if (name != null) {
            board.setName(name);
        }
        if (position != null) {
            board.setPosition(position);
        }

        return boardRepository.save(board);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID boardId) {
        Board board = getBoard(boardId);
        UUID workspaceId = board.getWorkspace().getId();
        String boardName = board.getName();
        boardRepository.delete(board);

        activityLogService.logActivity(
                workspaceId,
                workspaceId, // system-level delete
                ActivityType.BOARD_DELETED,
                "BOARD",
                boardId,
                boardName,
                String.format("Deleted board: %s", boardName));
    }
}
