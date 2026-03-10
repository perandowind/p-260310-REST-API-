package com.back.domain.post.post.controller;


import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/posts")
@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    record WriteRequestForm(
            @Size(min = 2, max = 10, message = "03-title-제목은 2자 이상 10자 이하로 입력해주세요.")
            @NotBlank(message = "01-title-제목은 필수입니다.")
            String title,
            @NotBlank(message = "02-content-내용은 필수입니다.")
            @Size(min = 2, max = 100, message = "04-content-내용은 2자 이상 100자 이하로 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/write")
    @Transactional(readOnly = true)
    public String writeForm(@ModelAttribute("form") WriteRequestForm form) {
        return "write";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute("form") @Valid WriteRequestForm form,
                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "write";
        }

        Post post = postService.write(form.title, form.content);
        return "redirect:/posts/%d".formatted(post.getId()); // 주소창을 바꿔, GET 요청
    }


    record ModifyRequestForm(
            @Size(min = 2, max = 10, message = "03-title-제목은 2자 이상 10자 이하로 입력해주세요.")
            @NotBlank(message = "01-title-제목은 필수입니다.")
            String title,
            @NotBlank(message = "02-content-내용은 필수입니다.")
            @Size(min = 2, max = 100, message = "04-content-내용은 2자 이상 100자 이하로 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/{id}/modify")
    @Transactional(readOnly = true)
    public String modifyForm(@PathVariable int id, Model model) {
        Post post = postService.findById(id).get();
        ModifyRequestForm modifyRequestForm = new ModifyRequestForm(post.getTitle(), post.getContent());
        model.addAttribute("form", modifyRequestForm);
        model.addAttribute("post", post);

        return "modify";
    }

    @PutMapping("/{id}")
    @Transactional
    public String modify(@PathVariable int id,
                         @ModelAttribute("form") @Valid WriteRequestForm form,
                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "modify";
        }

        Post post = postService.modify(id, form.title, form.content);
        return "redirect:/posts/%d".formatted(post.getId()); // 주소창을 바꿔, GET 요청
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        postService.deleteById(id);
        return "redirect:/posts";
    }


    @GetMapping("")
    @Transactional(readOnly = true)
    public String list(Model model) {

        model.addAttribute("posts", postService.findAll());
        return "list";
    }

    // 상세'조회 -> GET요청'
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable int id, Model model) {
        Post post = postService.findById(id).get();
        model.addAttribute("post", post);

        return "detail";
    }


}