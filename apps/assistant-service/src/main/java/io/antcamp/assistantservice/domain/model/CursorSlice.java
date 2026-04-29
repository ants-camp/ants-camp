package io.antcamp.assistantservice.domain.model;

import java.util.List;

public record CursorSlice<Item, Cursor>(List<Item> items, boolean hasNext, Cursor nextCursor) {
}