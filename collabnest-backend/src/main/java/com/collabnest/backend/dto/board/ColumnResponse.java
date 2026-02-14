package com.collabnest.backend.dto.board;

import java.util.UUID;

public record ColumnResponse(
        UUID id,
        UUID boardId,
        String name,
        Integer position) {
}
