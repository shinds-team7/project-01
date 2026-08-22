package com.example.petnow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewReplyRequest {

    @NotBlank(message = "답글 내용을 입력해주세요.")
    @Size(max = 300, message = "답글은 300자 이하로 입력해주세요.")
    private String content;

}
