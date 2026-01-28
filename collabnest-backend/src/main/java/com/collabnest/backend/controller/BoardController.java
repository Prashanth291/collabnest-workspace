package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Board;
import com.collabnest.backend.domain.entity.BoardColumn;
import com.collabnest.backend.dto.board.BoardResponse;
import com.collabnest.backend.dto.board.CreateBoardRequest;
import com.collabnest.backend.dto.board.CreateColumnRequest;
import com.collabnest.backend.service.BoardService;
import com.collabnest.backend.service.ColumnService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Board controller with workspace-aware authorization.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/boards")
public class BoardController {

    private final BoardService boardService;
    private final ColumnService columnService;

    public BoardController(BoardService boardService, ColumnService columnService) {
        this.boardService = boardService;
        this.columnService = columnService;
    }

    /**
     * Create a new board in the workspace - requires MEMBER role.
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<BoardResponse> createBoard(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateBoardRequest request) {
        
        Board board = boardService.createBoard(
                workspaceId, 
                request.name(),
                request.position()
        );
        
        BoardResponse response = new BoardResponse(
                board.getId(),
                board.getWorkspace().getId(),
                board.getName(),
                board.getPosition(),
                board.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all boards in the workspace - requires VIEWER role.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<BoardResponse>> getBoards(@PathVariable UUID workspaceId) {
        List<Board> boards = boardService.getWorkspaceBoards(workspaceId);
        
        List<BoardResponse> responses = boards.stream()
                .map(board -> new BoardResponse(
                        board.getId(),
                        board.getWorkspace().getId(),
                        board.getName(),
                        board.getPosition(),
                        board.getCreatedAt()
                ))
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific board - requires VIEWER role.
     */
    @GetMapping("/{boardId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<BoardResponse> getBoard(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId) {
        
        Board board = boardService.getBoard(boardId);
        
        // Verify the board belongs to this workspace
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        BoardResponse response = new BoardResponse(
                board.getId(),
                board.getWorkspace().getId(),
                board.getName(),
                board.getPosition(),
                board.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update a board - requires MEMBER role.
     */
    @PutMapping("/{boardId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateBoardRequest request) {
        
        Board board = boardService.updateBoard(
                boardId, 
                request.name(),
                request.position()
        );
        
        // Verify the board belongs to this workspace
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        BoardResponse response = new BoardResponse(
                board.getId(),
                board.getWorkspace().getId(),
                board.getName(),
                board.getPosition(),
                board.getCreatedAt()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a board - requires ADMIN role.
     */
    @DeleteMapping("/{boardId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId) {
        
        Board board = boardService.getBoard(boardId);
        
        // Verify the board belongs to this workspace
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a column in a board - requires MEMBER role.
     */
    @PostMapping("/{boardId}/columns")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<BoardColumn> createColumn(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateColumnRequest request) {
        
        BoardColumn column = columnService.createColumn(
                boardId,
                request.name(),
                request.position()
        );
        
        return ResponseEntity.ok(column);
    }

    /**
     * Get all columns in a board - requires VIEWER role.
     */
    @GetMapping("/{boardId}/columns")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<BoardColumn>> getColumns(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId) {
        
        List<BoardColumn> columns = columnService.getBoardColumns(boardId);
        return ResponseEntity.ok(columns);
    }

    /**
     * Delete a column - requires ADMIN role.
     */
    @DeleteMapping("/{boardId}/columns/{columnId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'ADMIN')")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId,
            @PathVariable UUID columnId) {
        
        columnService.deleteColumn(columnId);
        return ResponseEntity.noContent().build();
    }
}
