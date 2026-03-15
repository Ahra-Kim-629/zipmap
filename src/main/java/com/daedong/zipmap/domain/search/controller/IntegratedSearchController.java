package com.daedong.zipmap.domain.search.controller;

import com.daedong.zipmap.domain.search.dto.IntegratedSearchResponseDTO;
import com.daedong.zipmap.domain.search.service.IntegratedSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
public class IntegratedSearchController {

    private final IntegratedSearchService integratedSearchService;

    public IntegratedSearchController(IntegratedSearchService integratedSearchService) {
        this.integratedSearchService = integratedSearchService;
    }

    @GetMapping
    public String integratedSearch(@RequestParam("q") String keyword, Model model) {

        IntegratedSearchResponseDTO result = integratedSearchService.searchAll(keyword);
        model.addAttribute("searchResult", result);

        return "search/result";
    }
}
