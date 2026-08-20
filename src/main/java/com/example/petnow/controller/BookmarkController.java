package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/bookmarks")
    public String bookmarks(@LoginUser Long loginUserId, Model model) {
        model.addAttribute("places", bookmarkService.getBookmarkedPlaces(loginUserId));
        return "bookmarks";
    }

    @GetMapping("/api/bookmarks")
    @ResponseBody
    public ResponseEntity<List<PlaceListResponse>> bookmarkedPlaces(@LoginUser Long loginUserId) {
        return ResponseEntity.ok(bookmarkService.getBookmarkedPlaces(loginUserId));
    }

    @PostMapping("/api/bookmarks/{placeId}/toggle")
    @ResponseBody
    public ResponseEntity<BookmarkToggleResponse> toggle(@LoginUser Long loginUserId,
                                                         @PathVariable Long placeId) {
        boolean bookmarked = bookmarkService.toggle(loginUserId, placeId);
        return ResponseEntity.ok(new BookmarkToggleResponse(bookmarked));
    }

    public record BookmarkToggleResponse(boolean bookmarked) {
    }
}
