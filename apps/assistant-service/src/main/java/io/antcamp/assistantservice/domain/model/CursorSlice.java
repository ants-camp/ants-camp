package io.antcamp.assistantservice.domain.model;

import java.util.List;

public record CursorSlice<Item, Cursor>(List<Item> items, boolean hasNext, Cursor nextCursor) {

    public static <Item, Cursor> CursorSlice<Item, Cursor> empty() {
        return new CursorSlice<>(List.of(), false, null);
    }
}