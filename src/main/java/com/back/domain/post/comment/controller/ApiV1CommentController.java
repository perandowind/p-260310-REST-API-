package com.back.domain.post.comment.controller;

import com.back.domain.post.comment.dto.CommentDto;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "ApiV1CommentController", description = "댓글 API")
public class ApiV1CommentController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "다건 조회")
    public List<CommentDto> list(
            @PathVariable int postId
    ) {
        Post post = postService.findById(postId).get();
        List<Comment> comments = post.getComments();

        List<CommentDto> commentDtoList = comments.reversed().stream()
                .map(CommentDto::new)
                .toList();

        return commentDtoList;
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "단건 조회")
    public CommentDto detail(@PathVariable int postId, @PathVariable int commentId) {
        Post post = postService.findById(postId).get();
        Comment comment = post.findCommentById(commentId).get();

        return new CommentDto(comment);
    }

    record CommentWriteReqBody(
            @NotBlank(message = "02-content-내용은 필수입니다.")
            @Size(min = 2, max = 100, message = "04-content-내용은 2자 이상 100자 이하로 입력해주세요.")
            String content
    ){}

    record CommentWriteResBody(
            CommentDto commentDto
    ){}

    @PostMapping
    @Transactional
    @Operation(summary = "댓글 작성")
    public RsData<CommentWriteResBody> write(
            @PathVariable int postId,
            @RequestBody @Valid CommentWriteReqBody reqBody) {

        Post post = postService.findById(postId).get();     //select
        Comment comment = post.addComment(reqBody.content); //insert

        // =========== 이 시점에서 comment id가 필요해 =============
        postService.flush(); // flush: 현 시점에서 DB에 반영, insert가 실행 -> id값 정해짐

        return new RsData<>(
                "%d번 댓글이 생성되었습니다.".formatted(comment.getId()), //id가 존재해서 에러X
                "201-1",
                new CommentWriteResBody(
                        new CommentDto(comment)
                )
        );

    }

    @DeleteMapping("/{commentId}")
    @Transactional
    @Operation(summary = "댓글 삭제")
    public RsData<CommentDto> delete(
            @PathVariable int postId,
            @PathVariable int commentId
    ) {
        Post post = postService.findById(postId).get();
        Comment comment = post.findCommentById(commentId).get();
        post.deleteComment(commentId);

        return new RsData<>(
                "%d번 댓글이 삭제되었습니다.".formatted(commentId),
                "200-1",
                new CommentDto(comment)
        );
    }

    record CommentModifyReqBody(
            String content
    ){}

    @PutMapping("{commentId}")
    @Transactional
    @Operation(summary = "댓글 수정")
    public RsData<Void> modify(
            @PathVariable int postId,
            @PathVariable int commentId,
            @RequestBody CommentModifyReqBody reqBody
    ) {
        Post post = postService.findById(postId).get();
        post.modifyComment(commentId, reqBody.content);

        return new RsData<>(
                "%d번 댓글이 수정되었습니다.".formatted(commentId),
                "200-1"
        );
    }
}