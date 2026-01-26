package com.collabnest.backend.controller;

import com.collabnest.backend.domain.entity.Board;
import com.collabnest.backend.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Board controller with workspace-aware authorization.
 * 
 * Authorization strategy:
 * - Boards belong to workspaces
 * - Access is controlled by workspace membership
 * - Permission checks are delegated to workspace-level authorization
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * Create a new board in the workspace.
     * Requires at least MEMBER role in the workspace.
     */
    @PostMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Board> createBoard(
            @PathVariable UUID workspaceId,
            @RequestBody CreateBoardRequest request) {
        
        Board board = boardService.createBoard(
                workspaceId, 
                request.getName(),
                request.getPosition()
        );
        return ResponseEntity.ok(board);
    }

    /**
     * Get all boards in the workspace.
     * Requires at least VIEWER role.
     */
    @GetMapping
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<List<Board>> getBoards(@PathVariable UUID workspaceId) {
        List<Board> boards = boardService.getWorkspaceBoards(workspaceId);
        return ResponseEntity.ok(boards);
    }

    /**
     * Get a specific board.
     * Requires at least VIEWER role in the workspace.
     */
    @GetMapping("/{boardId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'VIEWER')")
    public ResponseEntity<Board> getBoard(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId) {
        
        Board board = boardService.getBoard(boardId);
        
        // Verify the board belongs to this workspace
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(board);
    }

    /**
     * Update a board.
     * Requires at least MEMBER role.
     */
    @PutMapping("/{boardId}")
    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    public ResponseEntity<Board> updateBoard(
            @PathVariable UUID workspaceId,
            @PathVariable UUID boardId,
            @RequestBody UpdateBoardRequest request) {
        
        Board board = boardService.updateBoard(
                boardId, 
                request.getName(),
                request.getPosition()
        );
        
        // Verify the board belongs to this workspace
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(board);
    }

    /**
     * Delete a board.
     * Requires at least ADMIN role.
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

    // DTOs
    public static class CreateBoardRequest {
        private String name;
        private Integer position;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }
    }

    public static class UpdateBoardRequest {
        private String name;
        private Integer position;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }
    }
}
