package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/bookmarks")
    public String bookmarks(@LoginUser Long loginUserId, Model model) {
        model.addAttribute("places", bookmarkService.getBookmarkedPlaces(loginUserId));
        return "bookmarks";
    }

    @PostMapping("/bookmarks/{placeId}/toggle")
    public String toggle(@LoginUser Long loginUserId,
                         @PathVariable Long placeId,
                         @RequestParam(defaultValue = "") String redirectTo,
                         RedirectAttributes redirectAttributes) {
        boolean bookmarked = bookmarkService.toggle(loginUserId, placeId);
        redirectAttributes.addFlashAttribute("bookmarkUpdated", true);
        redirectAttributes.addFlashAttribute("bookmarked", bookmarked);

        return "redirect:" + safeRedirect(redirectTo, placeId);
    }

    private String safeRedirect(String redirectTo, Long placeId) {
        if (redirectTo != null
                && redirectTo.startsWith("/")
                && !redirectTo.startsWith("//")
                && !redirectTo.startsWith("/\\")) {
            return redirectTo;
        }
        return "/places/" + placeId;
    }
}
