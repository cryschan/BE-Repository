package io.github.cryschan.berepository.domain.inquiry.entity.Inquiry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    FEATURE, //기능문의
    PAYMENT, //결제및환불
    ACCOUNT, //계정문의
    ETC; //기타

    //글자 틀리는 경우 방지 코드 추가
}
